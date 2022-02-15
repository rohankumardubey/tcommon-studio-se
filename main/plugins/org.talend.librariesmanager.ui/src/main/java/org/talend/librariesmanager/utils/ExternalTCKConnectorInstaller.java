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
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.model.components.IComponentsFactory;
import org.talend.core.model.general.ModuleToInstall;
import org.talend.core.runtime.util.SharedStudioUtils;


public class ExternalTCKConnectorInstaller implements Runnable {

    private List<ModuleToInstall> downloadedTCKConnectors;

    public ExternalTCKConnectorInstaller(List<ModuleToInstall> downloadedTCKConnectors) {
        this.downloadedTCKConnectors = downloadedTCKConnectors;
    }

    @Override
    public void run() {
        File extFolder = gettTCKExtFolder();
        for (ModuleToInstall module : downloadedTCKConnectors) {
            if (module.getModuleFile().exists()) {
                try {
                    File destFile = new File(extFolder, module.getModuleFile().getName());
                    Files.copy(module.getModuleFile().toPath(), destFile.toPath());
                    showInfomation();
                } catch (IOException e) {
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

    private File gettTCKExtFolder() {
        File componentFolder = SharedStudioUtils.getSharedStudioComponentsParentFolder();
        IPath path = new Path(IComponentsFactory.COMPONENTS_INNER_FOLDER);
        path = path.append(IComponentsFactory.EXTERNAL_COMPONENTS_INNER_FOLDER);
        File extFolder = new File(componentFolder, path.toOSString());
        if (!extFolder.exists()) {
            extFolder.mkdirs();
        }
        return extFolder;
    }
}
