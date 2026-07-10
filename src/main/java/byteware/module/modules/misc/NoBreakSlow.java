package byteware.module.modules.misc;

import byteware.module.Module;
import byteware.module.ModuleCategory;
import byteware.setting.settings.BooleanSetting;
import org.lwjgl.input.Keyboard;

public class NoBreakSlow extends Module {
	public final BooleanSetting affectPotion = new BooleanSetting("Affect Potion", false);

	public NoBreakSlow() {
		super("No Break Slow", ModuleCategory.MISC, Keyboard.KEY_NONE);
	}
}
