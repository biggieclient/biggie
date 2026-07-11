package biggie.module.modules.combat;

import biggie.module.Module;
import biggie.module.ModuleCategory;
import org.lwjgl.input.Keyboard;

public class Backtrack extends Module {
	public Backtrack() {
		super("Backtrack", ModuleCategory.COMBAT, Keyboard.KEY_NONE);
	}
}
