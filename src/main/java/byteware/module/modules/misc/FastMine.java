package byteware.module.modules.misc;

import byteware.module.Module;
import byteware.module.ModuleCategory;
import byteware.setting.settings.DoubleSetting;
import byteware.setting.settings.ListSetting;
import org.lwjgl.input.Keyboard;

public class FastMine extends Module {
	public final ListSetting mode = new ListSetting(
			"Mode",
			"Normal",
			"Normal",
			"Increment"
	);

	public final DoubleSetting speed = new DoubleSetting(
			"Speed",
			1.0,
			0.0,
			1.0,
			0.01
	);

	public FastMine() {
		super("Fast Mine", ModuleCategory.MISC, Keyboard.KEY_NONE);
	}
}
