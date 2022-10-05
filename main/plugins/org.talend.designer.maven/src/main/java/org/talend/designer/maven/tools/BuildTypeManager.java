package org.talend.designer.maven.tools;

import java.lang.reflect.InvocationTargetException;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.core.model.general.Project;
import org.talend.core.repository.model.ProxyRepositoryFactory;
import org.talend.designer.maven.migration.common.MigrationReportHelper;
import org.talend.designer.maven.migration.tasks.CorrectBuildTypeForDIJobMigrationTask;
import org.talend.designer.maven.migration.tasks.CorrectBuildTypeForDsRestMigrationTask;
import org.talend.designer.maven.migration.tasks.CorrectBuildTypeForRoutesMigrationTask;
import org.talend.designer.maven.migration.tasks.CorrectBuildTypeForSOAPServiceJobMigrationTask;
import org.talend.migration.IProjectMigrationTask;
import org.talend.repository.ProjectManager;
import org.talend.repository.RepositoryWorkUnit;

public class BuildTypeManager {

	private IProjectMigrationTask[] syncBuildTypeMigrationTasks = { new CorrectBuildTypeForRoutesMigrationTask(),
			new CorrectBuildTypeForSOAPServiceJobMigrationTask(), new CorrectBuildTypeForDsRestMigrationTask(),
			new CorrectBuildTypeForDIJobMigrationTask() };

	public void syncBuildTypes() throws Exception {

		IRunnableWithProgress runnableWithProgress = new IRunnableWithProgress() {

			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

				RepositoryWorkUnit<Object> workUnit = new RepositoryWorkUnit<Object>("Synchronize all build types") { //$NON-NLS-1$

					@Override
					protected void run() {
						final IWorkspaceRunnable op = new IWorkspaceRunnable() {

							@Override
							public void run(final IProgressMonitor monitor) throws CoreException {
								try {
									syncAllBuildTypesWithProgress(monitor);
								} catch (Exception e) {
									ExceptionHandler.process(e);
								}
							}

						};
						IWorkspace workspace = ResourcesPlugin.getWorkspace();
						try {
							ISchedulingRule schedulingRule = workspace.getRoot();
							workspace.run(op, schedulingRule, IWorkspace.AVOID_UPDATE, monitor);
						} catch (CoreException e) {
							ExceptionHandler.process(e);
						}
					}

				};
				workUnit.setAvoidUnloadResources(true);
				ProxyRepositoryFactory.getInstance().executeRepositoryWorkUnit(workUnit);
			}
		};
		new ProgressMonitorDialog(Display.getDefault().getActiveShell()).run(true, true, runnableWithProgress);
	}

	public void syncAllBuildTypesWithProgress(IProgressMonitor monitor) throws Exception {

		Project project = ProjectManager.getInstance().getCurrentProject();

		SubMonitor subMonitor = SubMonitor.convert(monitor, syncBuildTypeMigrationTasks.length);

		for (IProjectMigrationTask task : syncBuildTypeMigrationTasks) {
			subMonitor.beginTask(task.getDescription(), syncBuildTypeMigrationTasks.length);
			task.execute(project);
			subMonitor.worked(1);

		}

		subMonitor.beginTask("Generate migration report", syncBuildTypeMigrationTasks.length);
		MigrationReportHelper.getInstance().generateMigrationReport(project.getTechnicalLabel());

		monitor.done();
	}
}
