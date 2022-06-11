// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.signon.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.talend.signon.util.listener.SignOnEventListener;

public class SignOnClientListener implements Runnable{

    private static Logger LOGGER = Logger.getLogger(SignOnClientListener.class);

    private static final String STUDIO_SSO_CLIENT_MAX_WAITING_KEY = "talend.sso.client.max.waiting";

    private static final String STUDIO_AUTH_CODE_KEY = "code";

    private static final String STUDIO_AUTH_STATE_KEY = "state";

    private static final String STUDIO_CALLBACK_PREFIX = "studioCallback:";

    private static final SignOnClientListener instance = new SignOnClientListener();

    private static int listenPort = -1;

    private static volatile long lastStartTime = -1l;

    private static int maxWaitingTime = 3600 * 1000; // Max waiting time 60 minute

    private static volatile boolean isRunning = false;
    
    private static SignOnClientExec signOnClientExec;

    private List<SignOnEventListener> listenerList = new ArrayList<SignOnEventListener>();

    public static SignOnClientListener getInscance() {
        return instance;
    }

    private SignOnClientListener() {

    }

    private void processData(String msg) {
        if (msg.startsWith(STUDIO_CALLBACK_PREFIX)) {
            msg = msg.substring(STUDIO_CALLBACK_PREFIX.length());
        }
        Map<String, String> data = decodeMsg(msg);
        String code = data.get(STUDIO_AUTH_CODE_KEY);
        String state = data.get(STUDIO_AUTH_STATE_KEY);

        String[] splits = state.split(",");
        if (splits.length == 2) {
            fireLoginStop(code, splits[1]);
        } else {
            LOGGER.error("Invalid state value, it should be contain \",\" :" + state);
        }
    }

    private Map<String, String> decodeMsg(String data) {
        Map<String, String> map = new HashMap<String, String>();
        if (data.startsWith(STUDIO_CALLBACK_PREFIX)) {
            data = data.substring(STUDIO_CALLBACK_PREFIX.length());
        }
        if (data.startsWith(CloudSignOnUtil.STUDIO_REDIRECT_URL)) {
            data = data.substring(CloudSignOnUtil.STUDIO_REDIRECT_URL.length());
        }
        if (data.startsWith("?")) {
            data = data.substring("?".length());
        }

        String[] splits = data.split("&");
        for (int i = 0; i < splits.length; i++) {
            String str = splits[i];
            String[] spls = str.split("=");
            if (spls.length == 2) {
                map.put(spls[0], spls[1]);
            } else {
                LOGGER.error("Parse msg error is should be contains =:" + str);
            }
        }
        return map;
    }

    public void stop() {
        isRunning = false;
        listenPort = -1;
        lastStartTime = -1l;
    }

    private Integer newPort() {
        final Integer port = Integer.getInteger("stduio.signon.client.listen.port", -1);
        if (port <= 0) {
            try (ServerSocket socket = new ServerSocket(0)) {
                socket.setReuseAddress(true);
                return socket.getLocalPort();
            } catch (final IOException e) {
                throw new IllegalStateException(e);
            }
        }
        return port;
    }

    private void fireLoginStop(String code, String dataCenter) {
        for (SignOnEventListener l : listenerList) {
            try {
                l.loginStop(code, dataCenter);
            } catch (Exception ex) {
                LOGGER.info(ex);
            }
        }
    }

    private void fireLoginStart() {
        for (SignOnEventListener l : listenerList) {
            try {
                l.loginStart();
            } catch (Exception ex) {
                LOGGER.info(ex);
            }
        }
    }

    private void fireLoginFailed(Exception ex) {
        for (SignOnEventListener l : listenerList) {
            try {
                l.loginFailed(ex);
            } catch (Exception e) {
                LOGGER.info(e);
            }
        }
    }

    public void addLoginEventListener(SignOnEventListener listener) {
        listenerList.add(listener);
    }

    public void removeLoginEventListener(SignOnEventListener listener) {
        listenerList.remove(listener);
    }

    public int getListenPort() {
        return listenPort;
    }

    @Override
    public void run() {
        if (System.getProperty(STUDIO_SSO_CLIENT_MAX_WAITING_KEY) != null) {
            String strValue = System.getProperty(STUDIO_SSO_CLIENT_MAX_WAITING_KEY);
            try {
                int value = Integer.parseInt(strValue);
                if (value > 0) {
                    maxWaitingTime = value * 60;
                }
            } catch (Exception ex) {
                LOGGER.error("Update max waiting time failed", ex);
            }
        }

        if (isRunning) {
            lastStartTime = System.currentTimeMillis();
            return;
        }
        isRunning = true;
        listenPort = newPort();

        ServerSocket server;
        try {
            lastStartTime = System.currentTimeMillis();
            fireLoginStart();
            server = new ServerSocket(listenPort);
            ExecutorService threadPool = Executors.newFixedThreadPool(1);
            while (isRunning) {
                if (System.currentTimeMillis() - lastStartTime > maxWaitingTime) {
                    stop();
                    LOGGER.info("Stop Sign on client listener by timeout.");
                }
                Socket socket = server.accept();
                Runnable runnable = () -> {
                    try {
                        InputStream inputStream = socket.getInputStream();
                        byte[] bytes = new byte[1024];
                        int len;
                        StringBuilder sb = new StringBuilder();
                        while ((len = inputStream.read(bytes)) != -1) {
                            sb.append(new String(bytes, 0, len, StandardCharsets.UTF_8));
                        }
                        inputStream.close();
                        processData(sb.toString());
                        stop();
                        LOGGER.info("Stop Sign on client listener by return result.");
                    } catch (Exception e) {
                        LOGGER.error(e);
                        fireLoginFailed(e);
                    } finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            LOGGER.error(e);
                            fireLoginFailed(e);
                        }
                    }
                };
                threadPool.submit(runnable);
                Thread.sleep(1000l);
            }
        } catch (Exception ex) {
            LOGGER.error(ex);
            fireLoginFailed(ex);
        }        
    }
 
    public static boolean isRunning() {
        return isRunning;
    }

}
