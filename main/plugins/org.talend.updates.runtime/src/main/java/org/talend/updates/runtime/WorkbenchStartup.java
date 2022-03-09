package org.talend.updates.runtime;

import org.eclipse.ui.IStartup;
import org.talend.core.PluginChecker;
import org.talend.core.pendo.PendoTrackDataUtil;
import org.talend.core.pendo.PendoTrackDataUtil.TrackEvent;
import org.talend.core.pendo.PendoTrackSender;
import org.talend.migration.MigrationReportHelper;
import org.talend.updates.runtime.ui.CheckExtraFeaturesToInstallJob;

public class WorkbenchStartup implements IStartup {

    @Override
    public void earlyStartup() {
        MigrationReportHelper.getInstance().checkMigrationReport(true);
        PendoTrackSender.getInstance().sendToPendo(TrackEvent.PROJECT_LOGIN, PendoTrackDataUtil.getLoginEventProperties());
        if (!PluginChecker.isTIS() && !PluginChecker.isStudioLite()) {
            CheckExtraFeaturesToInstallJob checkExtraFeaturesToInstallJob = new CheckExtraFeaturesToInstallJob();
            checkExtraFeaturesToInstallJob.schedule();
        }
    }

}
