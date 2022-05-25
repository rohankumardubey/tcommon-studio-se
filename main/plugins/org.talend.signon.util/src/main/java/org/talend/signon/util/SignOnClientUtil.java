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

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.talend.signon.util.listener.SignOnEventListener;

public class SignOnClientUtil {

    private static Logger LOGGER = Logger.getLogger(SignOnClientUtil.class);

    private static final String STUDIO_CLIENT_ID = "0c51933d-c542-4918-9baf-86ef709af5d8";

    static final String TMC_LOGIN_URL = "http://10.67.8.153:8080/auth/auth.jsp";

    private static final String CLIENT_FILE_PATH_PROPERTY = "talend.studio.signon.client.path";

    private static final String CLIENT_FILE_NAME_ON_WINDOWS = "Talend_Sign_On_Tool_win-x86_64.exe";

    private static final String CLIENT_FILE_NAME_ON_LINUX_X86 = "Talend_Sign_On_Tool_linux_gtk_x86_64";

    private static final String CLIENT_FILE_NAME_ON_LINUX_AARCH64 = "Talend_Sign_On_Tool_linux_gtk_aarch64";

    private static final String CLIENT_FOLDER_NAME = "TalendSignTool";

    private static final SignOnClientUtil instance = new SignOnClientUtil();

    private SignOnClientInvoker signonClientInvoker;

    private SignOnClientUtil() {
        if (SignClientInstallService.getInstance().isNeedInstall()) {
            try {
                SignClientInstallService.getInstance().install();
            } catch (Exception e) {
                LOGGER.error(e);
            }
        }
    }

    public String getClientID() throws IOException {
        return STUDIO_CLIENT_ID;
    }

    public File getSSOClientAppFile() throws Exception {
        if (System.getProperty(CLIENT_FILE_PATH_PROPERTY) != null) {
            return new File(System.getProperty(CLIENT_FILE_PATH_PROPERTY));
        }
        File folder = getSignToolFolder();
        if (EnvironmentUtils.isWindowsSystem()) {
            return new File(folder, CLIENT_FILE_NAME_ON_WINDOWS);
        } else if (EnvironmentUtils.isLinuxUnixSystem()) {
            if (EnvironmentUtils.isX86_64()) {
                return new File(folder, CLIENT_FILE_NAME_ON_LINUX_X86);
            } else if (EnvironmentUtils.isAarch64()) {
                return new File(folder, CLIENT_FILE_NAME_ON_LINUX_AARCH64);
            }
        }
        throw new Exception("Unsupported OS");
    }

    public static File getSignToolFolder() {
        File configFolder = getConfigurationFolder();
        File signClientFolder = new File(configFolder, CLIENT_FOLDER_NAME);
        return signClientFolder;
    }

    private void startSignOnClient(SignOnEventListener listener) throws Exception {
        if (signonClientInvoker != null) {
            signonClientInvoker.stop();
        }
        String clientId = getClientID();
        File execFile = getSSOClientAppFile();
        int port = newPort();
        String codeChallenge = listener.getCodeChallenge();
        signonClientInvoker = new SignOnClientInvoker(execFile, clientId, port, codeChallenge);
        SignOnMonitor monitor = new SignOnMonitor(clientId, port, signonClientInvoker);
        monitor.addLoginEventListener(listener);
        // Start monitor
        new Thread(monitor).start();
    }

    private Integer newPort() {
        final Integer port = Integer.getInteger("stduio.signon.client.java.port", -1);
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

    public static SignOnClientUtil getInstance() {
        return instance;
    }

    public void signOnCloud(SignOnEventListener listener) throws Exception {
        SignOnClientUtil.getInstance().startSignOnClient(listener);
    }

    public String getLoginURL(SignOnEventListener listener) {
        return TMC_LOGIN_URL;
    }

    public String getSignOnURL(String loginURL, String clientID, String codeChallenge, String state) {
        StringBuffer urlSB = new StringBuffer();
        urlSB.append(loginURL).append("?");
        urlSB.append("client_id=").append(clientID).append("&");
        urlSB.append("state=").append(state).append("&");
        urlSB.append("code_challenge=").append(codeChallenge);
        return urlSB.toString();
    }

    public static File getConfigurationFolder() {
        BundleContext configuratorBundleContext = getCurrentBundleContext();
        final URL url = EquinoxUtils.getConfigLocation(configuratorBundleContext).getURL();
        try {
            return URIUtil.toFile(URIUtil.toURI(url));
        } catch (URISyntaxException e) {
            //
        }
        return null;
    }

    // always return a valid bundlesContext or throw a runtimeException
    public static BundleContext getCurrentBundleContext() {
        Bundle bundle = FrameworkUtil.getBundle(SignOnClientUtil.class);
        if (bundle != null) {
            BundleContext bundleContext = bundle.getBundleContext();
            if (bundleContext != null) {
                return bundleContext;
            } else {
                throw new RuntimeException(
                        "could not find current BundleContext, this should never happen, check that the bunlde is activated when this class is accessed");
            }
        } else {
            throw new RuntimeException(
                    "could not find current Bundle, this should never happen, check that the bunlde is activated when this class is accessed");
        }
    }
}
