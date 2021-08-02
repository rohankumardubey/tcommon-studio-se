// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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
import org.talend.updates.runtime.engine.ExtraFeaturesUpdatesFactory;
import org.talend.updates.runtime.i18n.Messages;
import org.talend.updates.runtime.model.ExtraFeature;

/**
 * This will check if there are extra features to update and provide a wizard for to choose which featurte to download
 * and install. First a check i done to see if the user refused the check once.
 *
 */
public class CheckAdditionalPackagesToInstallJob extends Job {
    protected boolean isCheckUpdateOnLine = false;
    /**
     * DOC sgandon CheckExtraFeaturesToUpdateJob constructor comment.
     *
     * @param name
     */
    public CheckAdditionalPackagesToInstallJob() {
        super(Messages.getString("CheckExtraFeaturesToInstallJob.check.extra.feature.to.install")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected IStatus run(IProgressMonitor monitor) {
    	ExtraFeaturesUpdatesFactory extraFeaturesFactory = new ExtraFeaturesUpdatesFactory(false);
        final Set<ExtraFeature> uninstalledExtraFeatures = new HashSet<ExtraFeature>();
        extraFeaturesFactory.retrieveUninstalledExtraFeatures(monitor, uninstalledExtraFeatures, false);
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
                            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Third-party Libraries", "title",updateWizardModel);  
                    dialog.showDialog(true);

                    }
                });
            }
        }// else not feature to install
        return Status.OK_STATUS;
    }

    public boolean isCheckUpdateOnLine() {
        return isCheckUpdateOnLine;
    }

    public void setCheckUpdateOnLine(boolean isCheckUpdateOnLine) {
        this.isCheckUpdateOnLine = isCheckUpdateOnLine;
    }
}
