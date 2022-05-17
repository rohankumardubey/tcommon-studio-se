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
import java.util.Base64;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.log4j.Logger;

public class SignOnClientInvoker implements Runnable {
    private static Logger LOGGER = Logger.getLogger(SignOnClientInvoker.class);
    
    private static final String STUDIO_CALL_PREFIX = "studioCall:";

    private static final String STUDIO_LOGIN_URL_KEY = "URL";

    private File execFile;

    private int port;
    
    private String codeChallenge;

    private String clientId;

    private ExecuteWatchdog executeWatchdog;

    private Exception error;

    public SignOnClientInvoker(File execFile, String clientId, int port, String codeChallenge) {
        this.execFile = execFile;
        this.clientId = clientId;
        this.port = port;
        this.codeChallenge = codeChallenge;
    }

    @Override
    public void run() {
        CommandLine cmdLine = new CommandLine(execFile.getAbsolutePath());
        cmdLine.addArgument(getInvokeParameter(clientId, SignOnClientUtil.TMC_LOGIN_URL, port));
        DefaultExecutor executor = new DefaultExecutor();
        executor.setExitValue(1);
        executeWatchdog = new ExecuteWatchdog(600000);
        executor.setWatchdog(executeWatchdog);
        try {
            executor.setWorkingDirectory(execFile.getParentFile());
            int exitValue = executor.execute(cmdLine);
        } catch (Exception e) {
            error = e;
            LOGGER.error(e);
        }
    }

    private String getInvokeParameter(String clientID, String loginURL, int callbackPort) {
        StringBuffer urlSB = new StringBuffer();
        urlSB.append(STUDIO_LOGIN_URL_KEY).append("=");
        urlSB.append(loginURL).append("?");
        urlSB.append("client_id=").append(clientID).append("&");
        urlSB.append("code_challenge=").append(this.codeChallenge);
        
        StringBuffer stateSB = new StringBuffer();
        stateSB.append("c=").append(clientID).append("&");
        stateSB.append("p=").append(callbackPort);
        urlSB.append("&").append("state=").append(Base64.getEncoder().encodeToString(stateSB.toString().getBytes()));
        return STUDIO_CALL_PREFIX + Base64.getEncoder().encodeToString(urlSB.toString().getBytes());
    }

    public void stop() {
        if (executeWatchdog != null && !executeWatchdog.killedProcess()) {
            executeWatchdog.destroyProcess();
        }
    }
}
