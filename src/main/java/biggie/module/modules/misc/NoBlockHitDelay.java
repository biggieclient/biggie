package biggie.module.modules.misc;

import biggie.module.Module;
import biggie.module.ModuleCategory;
import org.lwjgl.input.Keyboard;

public class NoBlockHitDelay extends Module {
	public NoBlockHitDelay() {
		super("NoBlockHitDelay", ModuleCategory.MISC, Keyboard.KEY_NONE);
	}
}
