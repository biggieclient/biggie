package byteware.module.modules.misc;

import byteware.module.Module;
import byteware.module.ModuleCategory;
import byteware.setting.settings.IntegerSetting;
import org.lwjgl.input.Keyboard;

public class NoBlockHitDelay extends Module {
	public final IntegerSetting delay = new IntegerSetting(
			"Delay",
			0,
			0,
			5,
			1
	);

	public NoBlockHitDelay() {
		super("No Block Hit Delay", ModuleCategory.MISC, Keyboard.KEY_NONE);
	}
}
