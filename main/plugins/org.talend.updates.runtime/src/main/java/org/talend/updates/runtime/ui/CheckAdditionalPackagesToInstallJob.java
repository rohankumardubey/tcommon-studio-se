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
package org.talend.updates.runtime.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.talend.updates.runtime.engine.factory.PluginOptionalMissingJarsExtraUpdatesFactory;
import org.talend.updates.runtime.engine.factory.PluginRequiredMissingJarsExtraUpdatesFactory;
import org.talend.updates.runtime.i18n.Messages;
import org.talend.updates.runtime.model.ExtraFeature;
import org.talend.commons.exception.ExceptionHandler;
import org.eclipse.jface.dialogs.MessageDialog;

public class CheckAdditionalPackagesToInstallJob extends Job {
    protected boolean isCheckUpdateOnLine = false;
    /**
     * DOC sgandon CheckExtraFeaturesToUpdateJob constructor comment.
     *
     * @param name
     */
    public CheckAdditionalPackagesToInstallJob() {
        super(Messages.getString("CheckAdditionalPackagesToInstallJob.check.third.party.lib.to.install")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected IStatus run(IProgressMonitor monitor) {
        final Set<ExtraFeature> uninstalledExtraFeatures = new HashSet<ExtraFeature>();
        PluginRequiredMissingJarsExtraUpdatesFactory pluginRequiredFactory = new PluginRequiredMissingJarsExtraUpdatesFactory();
        pluginRequiredFactory.setCheckUpdateOnLine(false);
        PluginOptionalMissingJarsExtraUpdatesFactory pluginOptionalFactory = new PluginOptionalMissingJarsExtraUpdatesFactory();
        pluginOptionalFactory.setCheckUpdateOnLine(false);
        try {
            pluginRequiredFactory.retrieveUninstalledExtraFeatures(monitor, uninstalledExtraFeatures);
            pluginOptionalFactory.retrieveUninstalledExtraFeatures(monitor, uninstalledExtraFeatures);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        // if feature to update are available then show the update wizard
        if (monitor.isCanceled()) {
             return Status.CANCEL_STATUS;
        }
        java.util.List<ExtraFeature> mustInstallList = new ArrayList<ExtraFeature>();
        for (ExtraFeature feature : uninstalledExtraFeatures) {
            if (feature.mustBeInstalled()) {
                mustInstallList.add(feature);
            }
        }
        if (!mustInstallList.isEmpty()) {
            synchronized (ShowWizardHandler.showWizardLock) {
                // make sure this dialog won't be popup in some special cases
                // just waiting for the lock to be released, then continue to execute.
                Display.getDefault().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                    UpdateWizardModel updateWizardModel = new UpdateWizardModel(uninstalledExtraFeatures);
                    AdditionalPackagesDialog dialog = new AdditionalPackagesDialog(
                                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                                "Third-party Libraries", "Choose the third-party libraries to install",
                                updateWizardModel);
                    dialog.showDialog(true);

                    }
                });
            }
        } else {
            MessageDialog.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                    Messages.getString("download.external.dialog.warning"),
                    Messages.getString("download.external.dialog.message"));
        }
        return Status.OK_STATUS;
    }

    public boolean isCheckUpdateOnLine() {
        return isCheckUpdateOnLine;
    }

    public void setCheckUpdateOnLine(boolean isCheckUpdateOnLine) {
        this.isCheckUpdateOnLine = isCheckUpdateOnLine;
    }
}
