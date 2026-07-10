package byteware.module.modules.combat;

import byteware.module.Module;
import byteware.module.ModuleCategory;
import org.lwjgl.input.Keyboard;

public class Backtrack extends Module {
	public Backtrack() {
		super("Backtrack", ModuleCategory.COMBAT, Keyboard.KEY_NONE);
	}
}
