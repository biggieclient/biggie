package biggie.module.modules.misc;

import biggie.module.Module;
import biggie.module.ModuleCategory;
import biggie.setting.settings.IntegerSetting;
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
		super("NoBlockHitDelay", ModuleCategory.MISC, Keyboard.KEY_NONE);
	}
}
