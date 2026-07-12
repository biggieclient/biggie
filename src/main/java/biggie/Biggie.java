package biggie;

import biggie.manager.ModuleManager;
import biggie.util.render.ServerRotation;
import org.lwjgl.opengl.GL11;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Biggie {
	public static final ExecutorService ASYNC_EXECUTOR = Executors.newWorkStealingPool();

	public static void init() {
		ServerRotation.setup();
		ModuleManager.init();

		Runtime.getRuntime().addShutdownHook(new Thread(Biggie::shutdown));
	}

	public static void shutdown() {
		ASYNC_EXECUTOR.shutdown();
	}
}
