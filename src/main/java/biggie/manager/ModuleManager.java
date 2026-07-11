package biggie.manager;

import biggie.module.Module;
import biggie.module.ModuleCategory;
import biggie.module.modules.combat.*;
import biggie.module.modules.misc.*;
import biggie.module.modules.movement.*;
import biggie.module.modules.render.*;
import biggie.setting.Setting;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ModuleManager {
	public static final ArrayList<Module> MODULES = new ArrayList<>();

	public static void init() {
		MODULES.addAll(Arrays.asList(
				// combat modules
				new Backtrack(),
				new KillAura(),
				new LeftClicker(),
				new MoreKB(),
				new NoHitDelay(),
				new Velocity(),

				// misc modules
				new NoBlockHitDelay(),
				new NoBreakSlow(),
				new FastMine(),

				// movement modules
				new NoItemSlow(),
				new Sprint(),
				new Speed(),

				// render modules
				new ClickGuiModule(),
				new ArrayListModule()
		));

		MODULES.sort((m1, m2) -> m2.name.compareToIgnoreCase(m1.name));

		for (Module module : MODULES) {
			for (Field field : module.getClass().getDeclaredFields()) {
				if (!field.isAccessible()) {
					field.setAccessible(true);
				}

				try {
					Object object = field.get(module);

					if (object instanceof Setting<?>) {
						module.registerSettings((Setting<?>) object);
					}
				} catch (Exception ignored) {
				}
			}
		}
	}

	public static ArrayList<Module> getModulesFromCategory(ModuleCategory moduleCategory) {
		return (ArrayList<Module>) MODULES.parallelStream()
				.filter(module -> module.category == moduleCategory)
				.collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked")
	public static <T extends Module> T getModule(Class<T> moduleClass) {
		for (Module module : MODULES) {
			if (module.getClass() == moduleClass) {
				return (T) module;
			}
		}

		return (T) new Module(null, null, 0);
	}
}
