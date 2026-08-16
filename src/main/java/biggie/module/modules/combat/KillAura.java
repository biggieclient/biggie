package biggie.module.modules.combat;

import biggie.module.Module;
import biggie.module.ModuleCategory;
import org.lwjgl.input.Keyboard;

// TODO: Recodar esse modulo e verificar se ta dando flag de timing dos event.
public class KillAura extends Module {
	public KillAura() {
		super("KillAura", ModuleCategory.COMBAT, Keyboard.KEY_NONE);
	}

	@Override
	public void onDisable() {}
}
