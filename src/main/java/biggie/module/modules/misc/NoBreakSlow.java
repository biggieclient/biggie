package biggie.module.modules.misc;

import biggie.module.Module;
import biggie.module.ModuleCategory;
import org.lwjgl.input.Keyboard;

public class NoBreakSlow extends Module {
	public NoBreakSlow() {
		super("No Break Slow", ModuleCategory.MISC, Keyboard.KEY_NONE);
	}
}
