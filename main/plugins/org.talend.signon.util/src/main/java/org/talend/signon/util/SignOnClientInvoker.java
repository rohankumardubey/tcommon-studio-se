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
import org.eclipse.equinox.app.IApplication;

public class SignOnClientInvoker implements Runnable {
    private static Logger LOGGER = Logger.getLogger(SignOnClientInvoker.class);
    
    private static final String STUDIO_CALL_PREFIX = "studioCall:";

    private static final String STUDIO_LOGIN_URL_KEY = "URL";
    
    private static final String STUDIO_SIGN_CLIENT_DEBUG_PORT="talend.studio.sign.client.debug.port";

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
        CommandLine cmdLine = null;
        cmdLine = new CommandLine(execFile);
        cmdLine.addArgument(getInvokeParameter(clientId, SignOnClientUtil.TMC_LOGIN_URL, port)); 
        if (getClientDebugPort() != null) {
            cmdLine.addArgument("-vmargs");
            cmdLine.addArgument("-Xdebug");
            String cmd = "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=*:" + getClientDebugPort();
            cmdLine.addArgument(cmd);
        }
        DefaultExecutor executor = new DefaultExecutor();
        executeWatchdog = new ExecuteWatchdog(600000);
        executor.setWatchdog(executeWatchdog);
        executor.setExitValues(new int [] {0, 24});
        try {
            if (!execFile.canExecute()) {
                execFile.setExecutable(true);
            }
            executor.setWorkingDirectory(execFile.getParentFile());
            int exitValue = executor.execute(cmdLine);
            if (IApplication.EXIT_RELAUNCH == exitValue) {
                cmdLine = new CommandLine(execFile);
                if (getClientDebugPort() != null) {
                    cmdLine.addArgument("-vmargs");
                    cmdLine.addArgument("-Xdebug");
                    String cmd = "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=*:" + getClientDebugPort();
                    cmdLine.addArgument(cmd);
                }
                exitValue = executor.execute(cmdLine);
            }
        } catch (Exception e) {
            error = e;
            LOGGER.error(e);
        }
    }
    
    private String getClientDebugPort() {
        return System.getProperty(STUDIO_SIGN_CLIENT_DEBUG_PORT);
    }

    private String getInvokeParameter(String clientID, String loginURL, int callbackPort) {
        StringBuffer stateSB = new StringBuffer();
        stateSB.append("p=").append(callbackPort);
        
        StringBuffer urlSB = new StringBuffer();
        urlSB.append(STUDIO_LOGIN_URL_KEY).append("=");
        urlSB.append(SignOnClientUtil.getInstance().getSignOnURL(loginURL, clientID, codeChallenge, Base64.getEncoder().encodeToString(stateSB.toString().getBytes())));
        
        return STUDIO_CALL_PREFIX + Base64.getEncoder().encodeToString(urlSB.toString().getBytes());
    }

    public void stop() {
        if (executeWatchdog != null && !executeWatchdog.killedProcess()) {
            executeWatchdog.destroyProcess();
        }
    }

    
    public Exception getError() {
        return error;
    }
}
