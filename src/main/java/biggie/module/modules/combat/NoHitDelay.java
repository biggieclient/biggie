package biggie.module.modules.combat;

import biggie.module.Module;
import biggie.module.ModuleCategory;
import org.lwjgl.input.Keyboard;

public class NoHitDelay extends Module {
	public NoHitDelay() {
		super("NoHitDelay", ModuleCategory.COMBAT, Keyboard.KEY_NONE);
	}
}
