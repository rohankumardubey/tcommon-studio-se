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
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.log4j.Logger;
import org.talend.signon.util.listener.SignOnEventListener;

public class SignOnMonitor implements Runnable {
    
    private static Logger LOGGER = Logger.getLogger(SignOnMonitor.class);
    
    String STUDIO_CLIENT_ID_KEY = "c";

    String STUDIO_AUTH_CODE_KEY = "code";
    
    String STUDIO_CALLBACK_PREFIX="studioCallback:";

    private volatile boolean stopThread;

    private String code;

    private String clientID;

    private int port;
    
    private SignOnClientInvoker invoker;

    private List<SignOnEventListener> listenerList = new ArrayList<SignOnEventListener>();

    public SignOnMonitor(String clientID, int port, SignOnClientInvoker invoker) {
        this.clientID = clientID;
        this.port = port;
        this.invoker = invoker;
    }

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        ServerSocket server;
        try {
            server = new ServerSocket(port);
            ExecutorService threadPool = Executors.newFixedThreadPool(1);
            invoker.run();
            while (!stopThread) {
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
                        processData(sb.toString());
                        inputStream.close();
                    } catch (Exception e) {
                        LOGGER.error(e);
                    } finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            LOGGER.error(e);
                        }
                    }
                };
                threadPool.submit(runnable);
                Thread.sleep(1000l);
            }
        } catch (IOException ex) {
            LOGGER.error(ex);
        } catch (InterruptedException e) {
            LOGGER.error(e);
        }
    }

    private void processData(String msg) {
        if (msg.startsWith(STUDIO_CALLBACK_PREFIX)) {
            msg = msg.substring(STUDIO_CALLBACK_PREFIX.length());
        }
        Map<String, String> data = decodeMsg(msg);
        if (!StringUtils.equals(clientID, data.get(STUDIO_CLIENT_ID_KEY))) {
            LOGGER.error("Invalid clientID:" + clientID);
        }
        
        this.clientID = data.get(STUDIO_CLIENT_ID_KEY);
        this.code = data.get(STUDIO_AUTH_CODE_KEY);
        if (code != null) {
            LOGGER.error("Got code and stopped monitor.");
            stopThread();
        }
    }

    private Map<String, String> decodeMsg(String data) {
        Map<String, String> map = new HashMap<String, String>();
        final Base64.Decoder decoder = Base64.getDecoder();
        String strValue = new String(decoder.decode(data));
        String[] values = strValue.split(",");
        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            String[] kv = value.split("=");
            if (kv.length == 2) {
                map.put(kv[0], kv[1]);
            }
        }
        return map;
    }

    public void stopThread() {
        fireLoginStop();
        stopThread = true;
        synchronized (this) {
            notify();
        }
    }

    public String getCode() {
        return code;
    }

    private void fireLoginStop() {
        for (SignOnEventListener l : listenerList) {
            l.loginStop(clientID, code);
        }
    }

    private void fireLoginStart() {
        for (SignOnEventListener l : listenerList) {
            l.loginStart(clientID);
        }
    }

    private void fireLoginFailed(Exception ex) {
        for (SignOnEventListener l : listenerList) {
            l.loginFailed(ex);
        }
    }
    
    public void addLoginEventListener(SignOnEventListener listener) {
        listenerList.add(listener);
    }
    
    public void removeLoginEventListener(SignOnEventListener listener) {
        listenerList.remove(listener);
    }
}
