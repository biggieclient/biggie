package biggie.module.modules.combat;

import biggie.module.Module;
import biggie.module.ModuleCategory;
import org.lwjgl.input.Keyboard;

public class KillAura extends Module {
	public KillAura() {
		super("KillAura", ModuleCategory.COMBAT, Keyboard.KEY_NONE);
	}

	@Override
	public void onDisable() {}
}
