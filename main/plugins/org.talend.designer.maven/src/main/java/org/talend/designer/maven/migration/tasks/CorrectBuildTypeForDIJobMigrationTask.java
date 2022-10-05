package org.talend.designer.maven.migration.tasks;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.components.filters.IComponentFilter;
import org.talend.core.model.components.filters.NameComponentFilter;
import org.talend.core.model.properties.Item;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.maven.migration.common.MigrationReportRecorder;

/*
 * If Job does not contain any of the following components: "tRouteInput", "tRESTClient", "tESBConsumer" 
 * then BUILD_TYPE must be STANDALONE
 * Else (job contains one of "tRouteInput", "tRESTClient" or "tESBConsumer")
 * If no BUILD_TYPE is set then default BUILD_TYPE must be STANDALONE
 * Manage child jobs for jobs ( parent, target BUILD_TYPE = STANDALONE )
 * If BUILD_TYPE is STANDALONE
 * Manage child jobs for jobs ( parent, target BUILD_TYPE = STANDALONE )
 * If BUILD_TYPE is ROUTE > EXCEPTION: need warning message! BUILD_TYPE was wrongly set to ROUTE from a previous migration task and has to be manually updated (all subjobs have to be checked manually). Value should be either STANDALONE (in most cases) or OSGI.
 */

public class CorrectBuildTypeForDIJobMigrationTask extends AbstractDataServiceJobMigrationTask {

	private static final String[] ESB_COMPONENTS = { "tRouteInput", "tRESTClient", "tESBConsumer" };

	/*
	 * (non-Javadoc)
	 *
	 * @see org.talend.migration.IMigrationTask#getOrder()
	 */
	@Override
	public Date getOrder() {
		GregorianCalendar gc = new GregorianCalendar(2021, 7, 25, 12, 0, 0);
		return gc.getTime();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ExecutionResult execute(Item item) {
		final ProcessType processType = getProcessType(item);

		boolean modified = false;

		Object originalBuildType = item.getProperty().getAdditionalProperties().get(BUILD_TYPE_PROPERTY);

		for (String name : ESB_COMPONENTS) {
			IComponentFilter filter = new NameComponentFilter(name);

			List<NodeType> c = searchComponent(processType, filter);

			if (!c.isEmpty()) {

				/*
				 * job contains one of "tRouteInput", "tRESTClient" or "tESBConsumer") If no
				 * BUILD_TYPE is set then default BUILD_TYPE must be STANDALONE
				 */

				if (null == originalBuildType) {
					item.getProperty().getAdditionalProperties().put(BUILD_TYPE_PROPERTY, BUILD_TYPE_STANDALONE);
					try {
						save(item);
						modified |= true;
						generateReportRecord(
								new MigrationReportRecorder(this, MigrationReportRecorder.MigrationOperationType.MODIFY,
										item, null, "Build Type", null, BUILD_TYPE_STANDALONE));
					} catch (PersistenceException e) {
						ExceptionHandler.process(e);
						return ExecutionResult.FAILURE;
					}
					return ExecutionResult.SUCCESS_NO_ALERT;
				}
				return ExecutionResult.NOTHING_TO_DO;
			}
		}

		/*
		 * If Job does not contain any of the following components: "tRouteInput",
		 * "tRESTClient", "tESBConsumer" then BUILD_TYPE must be STANDALONE
		 */

		if (null == originalBuildType || !BUILD_TYPE_STANDALONE.equalsIgnoreCase(originalBuildType.toString())) {
			item.getProperty().getAdditionalProperties().put(BUILD_TYPE_PROPERTY, BUILD_TYPE_STANDALONE);
			try {
				save(item);
				modified |= true;
				generateReportRecord(new MigrationReportRecorder(this,
						MigrationReportRecorder.MigrationOperationType.MODIFY, item, null, "Build Type",
						(null == originalBuildType) ? null : originalBuildType.toString(), BUILD_TYPE_STANDALONE));
			} catch (PersistenceException e) {
				ExceptionHandler.process(e);
				return ExecutionResult.FAILURE;
			}

			if (modified) {
				return ExecutionResult.SUCCESS_NO_ALERT;
			}
		}

		return ExecutionResult.NOTHING_TO_DO;
	}

	@Override
	public String getDescription() {
		return "Synchronize build types for DI jobs";
	}
}
