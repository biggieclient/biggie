package biggie;

import biggie.manager.ModuleManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Biggie {
	public static final ExecutorService ASYNC_EXECUTOR = Executors.newWorkStealingPool();

	public static void init() {
		ModuleManager.init();

		Runtime.getRuntime().addShutdownHook(new Thread(Biggie::shutdown));
	}

	public static void shutdown() {
		ASYNC_EXECUTOR.shutdown();
	}
}
