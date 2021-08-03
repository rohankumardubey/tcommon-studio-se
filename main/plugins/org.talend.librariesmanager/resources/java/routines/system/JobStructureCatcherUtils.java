// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
// ============================================================================
package routines.system;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

//TODO split to several classes by the level when have a clear requirement or design : job, component, connection
public class JobStructureCatcherUtils {

	private SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss.SSSZ");

	// TODO split it as too big, even for storing the reference only which point
	// null
	public class JobStructureCatcherMessage {

		public String component_id;

		public String component_label;

		public String component_name;

		public Map<String, String> component_parameters;

		public List<Map<String, String>> component_schema;

		public String input_connectors;

		public String output_connectors;

		public Map<String, String> connector_name_2_connector_schema;

		public String job_name;

		public String job_id;

		public String job_version;

		public boolean current_connector_as_input;

		public String current_connector_type;

		public String current_connector;

		public String currrent_row_content;

		public String sourceId;
		public String sourceLabel;
		public String sourceComponentName;
		public String targetId;
		public String targetLabel;
		public String targetComponentName;

		public long row_count;

		public long total_row_number;

		public long start_time;

		public long end_time;

		public String moment;

		public String status;

		public LogType log_type;

		// process uuid
		public String pid = ProcessIdAndThreadId.getProcessId();

		// thread uuid
		public String tid = ProcessIdAndThreadId.getThreadId();

		public JobStructureCatcherMessage() {
		}

	}

	public static enum LogType {
		JOBSTART, JOBEND, RUNCOMPONENT, FLOWINPUT, FLOWOUTPUT, PERFORMANCE,

		RUNTIMEPARAMETER, RUNTIMESCHEMA
	}

	// single one for message send order as batch support
	// ConcurrentLinkedQueue is not good way to implement batch, except we send
	// it regularly by time, also out of memory risk and kill cpu time if
	// produce slow a lot than consumer, and if call wait/sleep, no mmuch better
	// than BlockingQueue
	// seems BlockingQueue not better a lot than synchronizedList or syn self
	// directly, but introduce risk
	// if no batch or batch size is 1, no need this
	private static List<JobStructureCatcherMessage> messages;

	private static final boolean asyn = Boolean.getBoolean("audit.asyn");
	private static final int message_batch_size;

	static {
		Integer batch_size = Integer.getInteger("audit.batch.size");
		message_batch_size = batch_size != null ? batch_size : 0;

		if (message_batch_size > 1) {
			messages = Collections.synchronizedList(
					new ArrayList<JobStructureCatcherMessage>());
		}
	}

	private static class LazyHolder {
		// not load it if not enable audit/runtime
		private static LogSender sender;

		static {
			if (asyn) {
				sender = new LogSender();
			}
		}
	}

	public String job_name = "";

	public String job_id = "";

	public String job_version = "";

	public JobStructureCatcherUtils(String jobName, String jobId,
			String jobVersion) {
		this.job_name = jobName;
		this.job_id = jobId;
		this.job_version = jobVersion;

	}

	public void init(String pid, String fatherPid, String rootPid) {
		this.pid = pid;
		this.fatherPid = fatherPid;
		this.rootPid = rootPid;
	}

	public void addComponentParameterMessage(String component_id,
			String component_name, Map<String, String> component_parameters) {
		JobStructureCatcherMessage scm = new JobStructureCatcherMessage();
		scm.job_name = this.job_name;
		scm.job_id = this.job_id;
		scm.job_version = this.job_version;

		scm.component_id = component_id;
		scm.component_name = component_name;

		scm.component_parameters = component_parameters;

		scm.log_type = LogType.RUNTIMEPARAMETER;

		addMessage(scm);
		sendAll(scm);
	}

	private void addMessage(JobStructureCatcherMessage scm) {
		if (message_batch_size > 1) {
			messages.add(scm);
		}
	}

	public void addConnectionSchemaMessage(String source_component_id,
			String source_component_name, String target_component_id,
			String target_component_name, String current_connector,
			List<Map<String, String>> component_schema) {
		JobStructureCatcherMessage scm = new JobStructureCatcherMessage();
		scm.job_name = this.job_name;
		scm.job_id = this.job_id;
		scm.job_version = this.job_version;

		scm.current_connector = current_connector;
		scm.sourceId = source_component_id;
		scm.sourceComponentName = source_component_name;
		scm.targetId = target_component_id;
		scm.targetComponentName = target_component_name;

		scm.component_schema = component_schema;

		scm.log_type = LogType.RUNTIMESCHEMA;

		addMessage(scm);
		sendAll(scm);
	}

	public void addConnectionMessage(String component_id,
			String component_label, String component_name,
			boolean current_connector_as_input, String current_connector_type,
			String current_connector, long total_row_number, long start_time,
			long end_time) {
		JobStructureCatcherMessage scm = new JobStructureCatcherMessage();
		scm.job_name = this.job_name;
		scm.job_id = this.job_id;
		scm.job_version = this.job_version;

		scm.component_id = component_id;
		scm.component_label = component_label;
		scm.component_name = component_name;
		scm.current_connector_as_input = current_connector_as_input;
		scm.current_connector_type = current_connector_type;
		scm.current_connector = current_connector;
		scm.total_row_number = total_row_number;
		scm.start_time = start_time;
		scm.end_time = end_time;

		if (current_connector_as_input) {
			scm.log_type = LogType.FLOWINPUT;
		} else {
			scm.log_type = LogType.FLOWOUTPUT;
		}

		addMessage(scm);
		sendBatch(scm);
	}

	public void addCM(String component_id, String component_label,
			String component_name) {
		JobStructureCatcherMessage scm = new JobStructureCatcherMessage();
		scm.moment = sdf.format(new Date());

		scm.job_name = this.job_name;
		scm.job_id = this.job_id;
		scm.job_version = this.job_version;

		scm.component_id = component_id;
		scm.component_label = component_label;
		scm.component_name = component_name;

		scm.log_type = LogType.RUNCOMPONENT;

		addMessage(scm);
		sendBatch(scm);
	}

	public void addJobStartMessage() {
		JobStructureCatcherMessage scm = new JobStructureCatcherMessage();
		scm.moment = sdf.format(new Date());

		scm.job_name = this.job_name;
		scm.job_id = this.job_id;
		scm.job_version = this.job_version;

		scm.log_type = LogType.JOBSTART;

		addMessage(scm);
		sendBatch(scm);
	}

	public void addJobEndMessage(long start_time, long end_time,
			String status) {
		JobStructureCatcherMessage scm = new JobStructureCatcherMessage();
		scm.moment = sdf.format(new Date());

		scm.job_name = this.job_name;
		scm.job_id = this.job_id;
		scm.job_version = this.job_version;

		scm.status = (status == "" ? "end" : status);
		scm.start_time = start_time;
		scm.end_time = end_time;

		scm.log_type = LogType.JOBEND;

		addMessage(scm);
		sendAll(scm);
	}

	public void addConnectionMessage4PerformanceMonitor(
			String current_connector, String sourceId, String sourceLabel,
			String sourceComponentName, String targetId, String targetLabel,
			String targetComponentName, int row_count, long start_time,
			long end_time) {
		JobStructureCatcherMessage scm = new JobStructureCatcherMessage();
		scm.job_name = this.job_name;
		scm.job_id = this.job_id;
		scm.job_version = this.job_version;

		scm.current_connector = current_connector;

		scm.sourceId = sourceId;
		scm.sourceLabel = sourceLabel;
		scm.sourceComponentName = sourceComponentName;

		scm.targetId = targetId;
		scm.targetLabel = targetLabel;
		scm.targetComponentName = targetComponentName;

		scm.row_count = row_count;
		scm.start_time = start_time;
		scm.end_time = end_time;

		scm.log_type = LogType.PERFORMANCE;

		addMessage(scm);
		sendBatch(scm);
	}

	private List<JobStructureCatcherMessage> getMessages() {
		synchronized (messages) {
			if (messages.size() < message_batch_size) {
				return Collections.emptyList();
			}

			List<JobStructureCatcherMessage> messagesToSend = new ArrayList<JobStructureCatcherMessage>();

			for (JobStructureCatcherMessage scm : messages) {
				messagesToSend.add(scm);
			}
			messages.clear();

			return messagesToSend;
		}
	}

	private void sendAll(JobStructureCatcherMessage message) {
		if (asyn) {
			// this is a inside blocking queue with no limit size here for
			// tasks, good
			// for performance? no memory risk? our concurrent is not too high,
			// producer is slow
			// a lot than consumer, maybe OK
			LazyHolder.sender.send(() -> {
				if (message_batch_size > 1) {
					send(getAllMessages());
				} else {
					sendOne(message);
				}
			});
		} else {
			if (message_batch_size > 1) {
				send(getAllMessages());
			} else {
				sendOne(message);
			}
		}
	}

	private void sendBatch(JobStructureCatcherMessage message) {
		if (asyn) {
			LazyHolder.sender.send(() -> {
				if (message_batch_size > 1) {
					send(getMessages());
				} else {
					sendOne(message);
				}
			});
		} else {
			if (message_batch_size > 1) {
				send(getMessages());
			} else {
				sendOne(message);
			}
		}
	}

	public void shutdown() {
		if (asyn) {
			LazyHolder.sender.shutdown();
		}
	}

	// it works for final send for not loss data and also for runtime
	// parameter/schema log as they don't have stop method
	private List<JobStructureCatcherMessage> getAllMessages() {
		List<JobStructureCatcherMessage> messagesToSend = new ArrayList<JobStructureCatcherMessage>();
		synchronized (messages) {
			for (JobStructureCatcherMessage scm : messages) {
				messagesToSend.add(scm);
			}
			messages.clear();
		}
		return messagesToSend;
	}

	private void send(
			List<JobStructureCatcherUtils.JobStructureCatcherMessage> messages) {
		if (messages.isEmpty()) {
			return;
		}

		for (JobStructureCatcherUtils.JobStructureCatcherMessage jcm : messages) {
			sendOne(jcm);
		}
	}

	private void sendOne(
			JobStructureCatcherUtils.JobStructureCatcherMessage jcm) {
		org.talend.job.audit.JobContextBuilder builder = org.talend.job.audit.JobContextBuilder
				.create().jobName(jcm.job_name).jobId(jcm.job_id)
				.jobVersion(jcm.job_version).custom("process_id", jcm.pid)
				.custom("thread_id", jcm.tid).custom("pid", pid)
				.custom("father_pid", fatherPid).custom("root_pid", rootPid);
		org.talend.logging.audit.Context log_context = null;

		if (jcm.log_type == JobStructureCatcherUtils.LogType.PERFORMANCE) {
			long timeMS = jcm.end_time - jcm.start_time;
			String duration = String.valueOf(timeMS);

			log_context = builder.sourceId(jcm.sourceId)
					.sourceLabel(jcm.sourceLabel)
					.sourceConnectorType(jcm.sourceComponentName)
					.targetId(jcm.targetId).targetLabel(jcm.targetLabel)
					.targetConnectorType(jcm.targetComponentName)
					.connectionName(jcm.current_connector).rows(jcm.row_count)
					.duration(duration).build();
			auditLogger.flowExecution(log_context);
		} else if (jcm.log_type == JobStructureCatcherUtils.LogType.JOBSTART) {
			log_context = builder.timestamp(jcm.moment).build();
			auditLogger.jobstart(log_context);
		} else if (jcm.log_type == JobStructureCatcherUtils.LogType.JOBEND) {
			long timeMS = jcm.end_time - jcm.start_time;
			String duration = String.valueOf(timeMS);

			log_context = builder.timestamp(jcm.moment).duration(duration)
					.status(jcm.status).build();

			auditLogger.jobstop(log_context);
		} else if (jcm.log_type == JobStructureCatcherUtils.LogType.RUNCOMPONENT) {
			log_context = builder.timestamp(jcm.moment)
					.connectorType(jcm.component_name)
					.connectorId(jcm.component_id)
					.connectorLabel(jcm.component_label).build();
			auditLogger.runcomponent(log_context);
		} else if (jcm.log_type == JobStructureCatcherUtils.LogType.FLOWINPUT) {// log
																				// current
																				// component
																				// input
																				// line
			long timeMS = jcm.end_time - jcm.start_time;
			String duration = String.valueOf(timeMS);

			log_context = builder.connectorType(jcm.component_name)
					.connectorId(jcm.component_id)
					.connectorLabel(jcm.component_label)
					.connectionName(jcm.current_connector)
					.connectionType(jcm.current_connector_type)
					.rows(jcm.total_row_number).duration(duration).build();
			auditLogger.flowInput(log_context);
		} else if (jcm.log_type == JobStructureCatcherUtils.LogType.FLOWOUTPUT) {// log
																					// current
																					// component
																					// output/reject
																					// line
			long timeMS = jcm.end_time - jcm.start_time;
			String duration = String.valueOf(timeMS);

			log_context = builder.connectorType(jcm.component_name)
					.connectorId(jcm.component_id)
					.connectorLabel(jcm.component_label)
					.connectionName(jcm.current_connector)
					.connectionType(jcm.current_connector_type)
					.rows(jcm.total_row_number).duration(duration).build();
			auditLogger.flowOutput(log_context);
		} else if (jcm.log_type == JobStructureCatcherUtils.LogType.RUNTIMEPARAMETER) {
			builder.connectorType(jcm.component_name)
					.connectorId(jcm.component_id);

			for (Map.Entry<String, String> entry : jcm.component_parameters
					.entrySet()) {
				builder.custom("P_" + entry.getKey(), entry.getValue());
			}

			log_context = builder.build();

			runtime_lineage_logger.componentParameters(log_context);
		} else if (jcm.log_type == JobStructureCatcherUtils.LogType.RUNTIMESCHEMA) {
			log_context = builder.sourceConnectorType(jcm.sourceComponentName)
					.sourceId(jcm.sourceId)
					.connectionName(jcm.current_connector)
					.schema(jcm.component_schema.toString())
					.targetConnectorType(jcm.targetComponentName)
					.targetId(jcm.targetId).build();
			runtime_lineage_logger.schema(log_context);
		}
	}

	private org.talend.job.audit.JobAuditLogger auditLogger;
	private org.talend.job.audit.JobAuditLogger runtime_lineage_logger;
	private String pid;
	private String fatherPid;
	private String rootPid;

	public void setAuditLogger(
			org.talend.job.audit.JobAuditLogger auditLogger) {
		this.auditLogger = auditLogger;
	}

	public void setRuntimeLineageLogger(
			org.talend.job.audit.JobAuditLogger runtime_lineage_logger) {
		this.runtime_lineage_logger = runtime_lineage_logger;
	}

}
