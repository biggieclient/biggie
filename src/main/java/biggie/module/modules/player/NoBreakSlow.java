package biggie.module.modules.player;

import biggie.module.Module;
import biggie.module.ModuleCategory;
import org.lwjgl.input.Keyboard;

public class NoBreakSlow extends Module {
	public NoBreakSlow() {
		super("NoBreakSlow", ModuleCategory.PLAYER, Keyboard.KEY_NONE);
	}
}
