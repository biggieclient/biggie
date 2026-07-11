package biggie.module.modules.misc;

import biggie.module.Module;
import biggie.module.ModuleCategory;
import biggie.setting.settings.BooleanSetting;
import org.lwjgl.input.Keyboard;

public class NoBreakSlow extends Module {
	public NoBreakSlow() {
		super("No Break Slow", ModuleCategory.MISC, Keyboard.KEY_NONE);
	}
}
