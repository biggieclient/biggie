package byteware.module.modules.combat;

import byteware.module.Module;
import byteware.module.ModuleCategory;
import org.lwjgl.input.Keyboard;

public class NoHitDelay extends Module {
	public NoHitDelay() {
		super("NoHitDelay", ModuleCategory.COMBAT, Keyboard.KEY_NONE);
	}
}
