package biggie;

import biggie.manager.ModuleManager;
import biggie.util.render.ServerRotation;
import net.lenni0451.asmevents.EventManager;

public class Biggie {
	public static void init() {
		ServerRotation.setup();
		ModuleManager.init();

		Runtime.getRuntime().addShutdownHook(new Thread(Biggie::shutdown));

		EventManager.setErrorListener(Throwable::printStackTrace);
	}

	public static void shutdown() {
	}
}
