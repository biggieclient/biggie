package byteware.module.modules.combat;

import byteware.event.client.GameLoopEvent;
import byteware.module.Module;
import byteware.module.ModuleCategory;
import byteware.setting.settings.DoubleSetting;
import byteware.setting.settings.IntegerSetting;
import byteware.setting.settings.ListSetting;
import byteware.util.misc.MouseUtil;
import net.lenni0451.asmevents.event.EventTarget;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.Random;

public class LeftClicker extends Module {
	private final ListSetting randomizerType = new ListSetting(
			"Randomizer Type",
			"Random",
			"Random",
			"Gaussian",
			"Constant"
	);

	private final IntegerSetting minCps = new IntegerSetting(
			"Min CPS",
			16,
			1,
			100,
			1,
			() -> randomizerType.value.equals("Random")
	);

	private final IntegerSetting maxCps = new IntegerSetting(
			"Max CPS",
			18,
			1,
			100,
			1,
			() -> randomizerType.value.equals("Random")
	);

	private final IntegerSetting cps = new IntegerSetting(
			"CPS",
			18,
			1,
			100,
			1,
			() -> !randomizerType.value.equals("Random")
	);

	private final DoubleSetting stdDev = new DoubleSetting(
			"STD Dev",
			1.0,
			0.1,
			2.0,
			0.01,
			() -> randomizerType.value.equals("Gaussian")
	);

	private Random random;
	private long lastMs;
	private boolean holding;

	public LeftClicker() {
		super("Left Clicker", ModuleCategory.COMBAT, Keyboard.KEY_NONE);
	}

	@Override
	public void onEnable() {
		random = new Random();
	}

	@Override
	public void onDisable() {
		lastMs = 0;
		random = null;

		if (holding) {
			KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);

			holding = false;
		}
	}

	@EventTarget(noParamEvents = GameLoopEvent.class)
	public void onGameLoop() {
		if (mc.theWorld != null && mc.thePlayer != null) {
			if (mc.currentScreen == null) {
				long delay = 0;
				int attackKey = mc.gameSettings.keyBindAttack.getKeyCode();

				switch (randomizerType.value) {
					case "Random":
						delay = 1000 / (int) (
								maxCps.value - (maxCps.value + 1 - minCps.value) * random.nextDouble() + 1
						);

						break;
					case "Gaussian":
						delay = 1000 / Math.max(
								MathHelper.clamp_int(
										(int) (random.nextGaussian() * stdDev.value + cps.value),
										Math.max(cps.value - 2, 1),
										cps.value
								),
								1
						);

						break;
					case "Constant":
						delay = 1000 / cps.value;

						break;
				}

				if (holding) {
					KeyBinding.setKeyBindState(attackKey, false);

					MouseUtil.setButtonState(attackKey + 100, false);

					holding = false;

					return;
				}

				if (Mouse.isButtonDown(attackKey + 100)) {
					if (System.currentTimeMillis() - lastMs >= delay) {
						KeyBinding.setKeyBindState(attackKey, true);
						KeyBinding.onTick(attackKey);

						MouseUtil.setButtonState(attackKey + 100, true);

						lastMs = System.currentTimeMillis();
					}
				}
			}
		}
	}
}
