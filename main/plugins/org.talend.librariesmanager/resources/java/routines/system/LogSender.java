package routines.system;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LogSender {

	private ScheduledExecutorService executor = Executors
			.newSingleThreadScheduledExecutor(runnable -> {
				Thread thread = new Thread(runnable, "Sender-Worker");
				thread.setDaemon(true);
				return thread;
			});

	LogSender() {
		Runtime.getRuntime().addShutdownHook(
				Executors.defaultThreadFactory().newThread(() -> shutdown()));
	}

	void send(Runnable task) {
		executor.execute(task);
	}

	void shutdown() {
		if (executor == null) {
			return;
		}

		try {
			executor.shutdown();
			executor.awaitTermination(500, TimeUnit.MILLISECONDS);
			executor = null;
		} catch (final InterruptedException e) {
			// nothing to do
		} finally {

		}
	}

}
