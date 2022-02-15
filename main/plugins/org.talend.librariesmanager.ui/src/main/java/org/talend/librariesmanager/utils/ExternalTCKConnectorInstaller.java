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
package org.talend.librariesmanager.utils;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.general.ModuleToInstall;
import org.talend.librariesmanager.model.ExternalTCKConnectorDataProvider;

public class ExternalTCKConnectorInstaller implements Runnable {

    private List<ModuleToInstall> downloadedTCKConnectors;

    public ExternalTCKConnectorInstaller(List<ModuleToInstall> downloadedTCKConnectors) {
        this.downloadedTCKConnectors = downloadedTCKConnectors;
    }

    @Override
    public void run() {
        File extFolder = ExternalTCKConnectorDataProvider.getTCKConnectorExtFolder();
        for (ModuleToInstall module : downloadedTCKConnectors) {
            if (module.getModuleFile().exists()) {
                try {
                    File destFile = new File(extFolder, module.getModuleFile().getName());
                    Files.copy(module.getModuleFile().toPath(), destFile.toPath());
                    ExternalTCKConnectorDataProvider.getIntance().connectorDownloaded(module);
                    showInfomation();
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            } else {
                ExceptionHandler.log("Can't find module file for:" + module.getMavenUri());
            }
        }
    }

    private void showInfomation() {
        String[] dialogButtonLabels = new String[] { "Yes", //$NON-NLS-1$
                "Not now" }; //$NON-NLS-1$
        MessageDialog dialog = new MessageDialog(null, "Restart Studio", (Image) null, //$NON-NLS-1$
                "Restart the Studio to load new connector?\\nOtherwise, Studio load it next time you log in.", //$NON-NLS-1$
                MessageDialog.QUESTION, 0, dialogButtonLabels) {

            @Override
            protected int getShellStyle() {
                return super.getShellStyle() | SWT.SHEET;
            }
        };
        boolean restart = dialog.open() == 0;
        if (restart) {
            PlatformUI.getWorkbench().restart();
        }
    }
}
