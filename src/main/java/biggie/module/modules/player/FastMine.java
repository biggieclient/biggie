package biggie.module.modules.player;

import biggie.module.Module;
import biggie.module.ModuleCategory;
import biggie.setting.settings.DoubleSetting;
import biggie.setting.settings.ListSetting;
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
			1.0,
			3.0,
			0.01,
			() -> mode.value.equals("Normal")
	);

	public final DoubleSetting incrementValue = new DoubleSetting(
			"Increment Value",
			0.30,
			0.01,
			3.0,
			0.01,
			() -> mode.value.equals("Increment")
	);

	public FastMine() {
		super("Fast Mine", ModuleCategory.PLAYER, Keyboard.KEY_NONE);
	}
}
