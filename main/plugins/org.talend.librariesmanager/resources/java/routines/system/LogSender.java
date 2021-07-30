package routines.system;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LogSender {

	private ScheduledExecutorService executor;

	void send(Runnable task) {
		if (executor == null) {
			executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
				Thread thread = new Thread(runnable, "Sender-Worker");
				thread.setDaemon(true);
				return thread;
			});

			// not sure can use runtime hook, as that may not executed for some
			// case, and also, the audit sender implement also use runtime hook,
			// how
			// to process execute order when jvm down?

			// and we don't depend on the order now as we call shutdown to
			// mark end in any producer threads,
			Runtime.getRuntime().addShutdownHook(Executors
					.defaultThreadFactory().newThread(() -> shutdown()));
		}
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
