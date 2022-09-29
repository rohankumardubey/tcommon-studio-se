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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.talend.signon.util.listener.LoginEventListener;

public class SSOClientUtil {

    private static Logger LOGGER = Logger.getLogger(SSOClientUtil.class);

    private static final String STUDIO_CLIENT_ID = "0c51933d-c542-4918-9baf-86ef709af5d8";

    private static final String CLIENT_FILE_PATH_PROPERTY = "talend.studio.signon.client.path";

    private static final String CLIENT_FILE_NAME_ON_WINDOWS = "Talend_Sign_On_Tool_win-x86_64.exe";

    private static final String CLIENT_FILE_NAME_ON_LINUX_X86 = "Talend_Sign_On_Tool_linux_gtk_x86_64";

    private static final String CLIENT_FILE_NAME_ON_LINUX_AARCH64 = "Talend_Sign_On_Tool_linux_gtk_aarch64";

    private static final String CLIENT_FILE_NAME_ON_MAC_X86 = "Talend_Sign_On_Tool.app";

    private static final String CLIENT_FILE_NAME_ON_MAC_AARCH64 = "Talend_Sign_On_Tool_aarch64.app";

    private static final String CLIENT_FOLDER_NAME = "studio_sso_client";

    static final String DATA_CENTER_KEY = "talend.tmc.datacenter";

    static final String DATA_CENTER_DISPLAY_KEY = "talend.tmc.datacenter.display";

    public static final String TALEND_DEBUG = "--talendDebug"; //$NON-NLS-1$

    private static final SSOClientUtil instance = new SSOClientUtil();

    private SSOClientExec signOnClientExec;

    private SSOClientUtil() {
        if (SSOClientInstaller.getInstance().isNeedInstall()) {
            try {
                SSOClientInstaller.getInstance().install();
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
        File folder = getSSOClientFolder();
        if (EnvironmentUtils.isWindowsSystem()) {
            return new File(folder, CLIENT_FILE_NAME_ON_WINDOWS);
        } else if (EnvironmentUtils.isLinuxUnixSystem()) {
            if (EnvironmentUtils.isX86_64()) {
                return new File(folder, CLIENT_FILE_NAME_ON_LINUX_X86);
            } else if (EnvironmentUtils.isAarch64()) {
                return new File(folder, CLIENT_FILE_NAME_ON_LINUX_AARCH64);
            }
        } else if (EnvironmentUtils.isMacOsSytem()) {
            File appFolder = null;
            if (EnvironmentUtils.isX86_64()) {
                appFolder = new File(folder, CLIENT_FILE_NAME_ON_MAC_X86);
            } else if (EnvironmentUtils.isAarch64()) {
                appFolder = new File(folder, CLIENT_FILE_NAME_ON_MAC_AARCH64);
            }
            if (appFolder != null) {
                return new File(appFolder, "Contents/MacOS/Talend_Sign_On_Tool");
            }
        }
        throw new Exception("Unsupported OS");
    }

    public static File getSSOClientFolder() {
        File configFolder = EquinoxUtils.getConfigurationFolder();
        File signClientFolder = new File(configFolder, CLIENT_FOLDER_NAME);
        return signClientFolder;
    }

    private synchronized void startSignOnClient(LoginEventListener listener) throws Exception {
        if (signOnClientExec != null) {
            signOnClientExec.stop();
        }
        String clientId = getClientID();
        File execFile = getSSOClientAppFile();
        String codeChallenge = listener.getCodeChallenge();
        if (isDebugMode()) {
            LOGGER.info("Prepare to start login cloud client monitor");
        }
        SSOClientMonitor signOnClientListener = SSOClientMonitor.getInscance();
        signOnClientListener.addLoginEventListener(listener);
        new Thread(signOnClientListener).start();
        if (isDebugMode()) {
            LOGGER.info("Login cloud client monitor started.");
        }
        while (!SSOClientMonitor.isRunning()) {
            TimeUnit.MILLISECONDS.sleep(100);
        }
        if (signOnClientListener.getListenPort() < 0) {
            throw new Exception("Login cloud client monitor start failed.");
        }
        if (isDebugMode()) {
            LOGGER.info("Prepare to start cloud client on " + signOnClientListener.getListenPort());
        }
        signOnClientExec = new SSOClientExec(execFile, clientId, codeChallenge, signOnClientListener.getListenPort(), listener);
        new Thread(signOnClientExec).start();
        if (isDebugMode()) {
            LOGGER.info("Login cloud client started.");
        }
    }

    public static SSOClientUtil getInstance() {
        return instance;
    }

    public void signOnCloud(LoginEventListener listener) throws Exception {
        SSOClientUtil.getInstance().startSignOnClient(listener);
    }

    public String getSignOnURL(String clientID, String codeChallenge, int callbackPort) throws UnsupportedEncodingException {
        String dataCenter = TMCRepositoryUtil.getDefaultDataCenter();
        StringBuffer urlSB = new StringBuffer();
        urlSB.append(TMCRepositoryUtil.getBaseLoginURL(dataCenter)).append("?");
        urlSB.append("client_id=").append(URLEncoder.encode(clientID, StandardCharsets.UTF_8.name())).append("&");
        urlSB.append("redirect_uri=")
                .append(URLEncoder.encode(TMCRepositoryUtil.getRedirectURL(dataCenter), StandardCharsets.UTF_8.name()))
                .append("&");
        urlSB.append("scope=").append(URLEncoder.encode("openid refreshToken", StandardCharsets.UTF_8.name())).append("&");
        urlSB.append("response_type=").append(URLEncoder.encode("code", StandardCharsets.UTF_8.name())).append("&");
        urlSB.append("code_challenge_method=").append(URLEncoder.encode("S256", StandardCharsets.UTF_8.name())).append("&");
        urlSB.append("code_challenge=").append(URLEncoder.encode(codeChallenge, StandardCharsets.UTF_8.name())).append("&");        
        String state = String.valueOf(callbackPort) + SSOUtil.STATE_PARAM_SEPARATOR + TMCRepositoryUtil.getDefaultDataCenter();
        urlSB.append("state=").append(URLEncoder.encode(state, StandardCharsets.UTF_8.name()));        
        return urlSB.toString();
    }

    public static boolean isDebugMode() {
        return Boolean.getBoolean("talendDebug") //$NON-NLS-1$
                || ArrayUtils.contains(Platform.getApplicationArgs(), SSOClientUtil.TALEND_DEBUG);
    }
}
