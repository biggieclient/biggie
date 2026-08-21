package biggie.module.modules.player;

import biggie.module.Module;
import biggie.module.ModuleCategory;
import org.lwjgl.input.Keyboard;

public class NoBlockHitDelay extends Module {
	public NoBlockHitDelay() {
		super("NoBlockHitDelay", ModuleCategory.PLAYER, Keyboard.KEY_NONE);
	}
}
