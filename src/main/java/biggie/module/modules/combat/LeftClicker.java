package biggie.module.modules.combat;

import biggie.event.client.GameLoopEvent;
import biggie.module.Module;
import biggie.module.ModuleCategory;
import biggie.setting.settings.DoubleSetting;
import biggie.setting.settings.IntegerSetting;
import biggie.setting.settings.ListSetting;
import biggie.util.math.MathUtil;
import biggie.util.misc.MouseUtil;
import net.lenni0451.asmevents.event.EventTarget;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.Random;

public class LeftClicker extends Module {
	private final ListSetting randomizerType = new ListSetting(
			"Randomizer Type",
			"Random",
			"Random",
			"Gaussian"
	);

	private final IntegerSetting changeCpsChance = new IntegerSetting(
			"Change CPS Chance",
			20,
			1,
			100,
			1
	);

	private final IntegerSetting outlierChance = new IntegerSetting(
			"Outlier Chance",
			10,
			1,
			100,
			1
	);

	private final IntegerSetting outlierAmount = new IntegerSetting(
			"Outlier Amount",
			50,
			1,
			100,
			1
	);

	private final IntegerSetting minCps = new IntegerSetting(
			"Min CPS",
			16,
			1,
			100,
			1,
			() -> !randomizerType.value.equals("Gaussian")
	);

	private final IntegerSetting maxCps = new IntegerSetting(
			"Max CPS",
			18,
			1,
			100,
			1,
			() -> !randomizerType.value.equals("Gaussian")
	);

	private final IntegerSetting cps = new IntegerSetting(
			"CPS",
			18,
			1,
			100,
			1,
			() -> randomizerType.value.equals("Gaussian")
	);

	private final DoubleSetting stdDev = new DoubleSetting(
			"STD Dev",
			1.1,
			1.0,
			2.0,
			0.01,
			() -> randomizerType.value.equals("Gaussian")
	);

	private Random random;
	private long lastMs;
	private long delay = 0;
	private boolean holding;

	public LeftClicker() {
		super("LeftClicker", ModuleCategory.COMBAT, Keyboard.KEY_NONE);
	}

	@Override
	public void onEnable() {
		random = new Random();

		generateRandomCps();
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
		if (mc.theWorld == null || mc.thePlayer == null)
			return;

		final int attackKey = mc.gameSettings.keyBindAttack.getKeyCode();

		if (mc.currentScreen != null) {
			KeyBinding.setKeyBindState(attackKey, false);
			MouseUtil.setButtonState(attackKey + 100, false);

			holding = false;

			return;
		}

		if (holding) {
			KeyBinding.setKeyBindState(attackKey, false);
			MouseUtil.setButtonState(attackKey + 100, false);

			holding = false;

			return;
		}

		final boolean canClick = mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK;
		final long currentTime = System.currentTimeMillis();

		if (!Mouse.isButtonDown(attackKey + 100))
			return;

		if ((int) (random.nextDouble() * 100) <= changeCpsChance.value) {
			generateRandomCps();
		}

		if (currentTime - lastMs < delay || !canClick)
			return;

		KeyBinding.setKeyBindState(attackKey, true);
		KeyBinding.onTick(attackKey);

		MouseUtil.setButtonState(attackKey + 100, true);

		lastMs = currentTime;
		holding = true;
	}

	// IntelliJ bom
	private void generateRandomCps() {
		switch (randomizerType.value) {
			case "Random":
				delay = (long) (1000 / (maxCps.value - (maxCps.value - minCps.value) * random.nextDouble()));

				break;
			case "Gaussian":
				delay = (long) (1000 / MathUtil.clampDouble(
						(int) (random.nextGaussian() * stdDev.value + cps.value),
						1,
						cps.value
				));

				break;
		}

		if ((int) (random.nextDouble() * 100) <= outlierChance.value) {
			delay += (long) (outlierAmount.value - (outlierAmount.value - 1) * random.nextDouble());
		}
	}
}
