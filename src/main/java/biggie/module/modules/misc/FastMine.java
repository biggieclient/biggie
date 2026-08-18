package biggie.module.modules.misc;

import biggie.module.Module;
import biggie.module.ModuleCategory;
import biggie.setting.settings.DoubleSetting;
import biggie.setting.settings.ListSetting;
import org.lwjgl.input.Keyboard;

// TODO: Fazer ele quebrar não tão rapido conforme você aumenta a speed,
//  não sei e não quero saber como block break do mine funciona, fodase.
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
			3.0,
			0.01
	);

	public FastMine() {
		super("Fast Mine", ModuleCategory.MISC, Keyboard.KEY_NONE);
	}
}
