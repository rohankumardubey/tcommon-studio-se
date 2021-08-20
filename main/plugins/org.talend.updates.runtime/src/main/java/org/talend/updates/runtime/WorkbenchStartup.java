package org.talend.updates.runtime;

import org.eclipse.ui.IStartup;
import org.talend.core.PluginChecker;
import org.talend.migration.MigrationReportHelper;
import org.talend.updates.runtime.ui.CheckExtraFeaturesToInstallJob;

public class WorkbenchStartup implements IStartup {

    @Override
    public void earlyStartup() {
        MigrationReportHelper.getInstance().checkMigrationReport(true);
        CheckExtraFeaturesToInstallJob checkExtraFeaturesToInstallJob = new CheckExtraFeaturesToInstallJob();
   
        if(!PluginChecker.isStudioLite()) {
        	checkExtraFeaturesToInstallJob.schedule();
        }

    }

}
