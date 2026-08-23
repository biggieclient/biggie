package biggie.module.modules.combat;

import biggie.event.client.GameLoopEvent;
import biggie.module.Module;
import biggie.module.ModuleCategory;
import biggie.setting.settings.BooleanSetting;
import biggie.setting.settings.IntegerSetting;
import biggie.setting.settings.ListSetting;
import biggie.util.misc.MouseUtil;
import biggie.util.player.RotationUtil;
import net.lenni0451.asmevents.event.EventTarget;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
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
			() -> !randomizerType.value.equals("Constant")
	);

	private final IntegerSetting maxCps = new IntegerSetting(
			"Max CPS",
			18,
			1,
			100,
			1,
			() -> !randomizerType.value.equals("Constant")
	);

	private final IntegerSetting cps = new IntegerSetting(
			"CPS",
			18,
			1,
			100,
			1,
			() -> randomizerType.value.equals("Constant")
	);

	private final BooleanSetting reverse = new BooleanSetting(
			"Max Values",
			false,
			() -> randomizerType.value.equals("Gaussian")
	);

	private Random random;
	private long lastMs;
	private boolean holding;

	// TODO: Caso o player esteja segurando o botão esquerdo e esteja mirando num bloco, fazer ele
	//  pressionar o botão esquerdo denovo pra não cancelar o break quando voce mirar num bloco enquanto o clicker ta funcionando.

	public LeftClicker() {
		super("LeftClicker", ModuleCategory.COMBAT, Keyboard.KEY_NONE);
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
		if (mc.theWorld == null || mc.thePlayer == null)
			return;

		long delay = 0;
		final long currTime = System.currentTimeMillis();
		final int attackKey = mc.gameSettings.keyBindAttack.getKeyCode();

		if (mc.currentScreen != null) {
			KeyBinding.setKeyBindState(attackKey, false);
			MouseUtil.setButtonState(attackKey + 100, false);

			holding = false;
			return;
		}

		switch (randomizerType.value) {
			case "Random":
				delay = (long) (1000 / (maxCps.value - (maxCps.value - minCps.value) * random.nextDouble()));
				break;
			case "Gaussian":
				final double factor = reverse.value ? (1 - MathHelper.clamp_double(0.5 + random.nextGaussian() * 0.15, 0, 1)) : MathHelper.clamp_double(0.5 + random.nextGaussian() * 0.15, 0, 1);
				delay = (long) (1000 / (maxCps.value - (maxCps.value - minCps.value) * (factor)));
				break;
			case "Constant":
				delay = (long) 1000 / cps.value;
				break;
		}

		if (holding) {
			KeyBinding.setKeyBindState(attackKey, false);
			MouseUtil.setButtonState(attackKey + 100, false);

			holding = false;
			return;
		}

		final boolean canClick = mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK;

		if (!Mouse.isButtonDown(attackKey + 100))
			return;

		if (currTime - lastMs < delay || !canClick)
			return;

		KeyBinding.setKeyBindState(attackKey, true);
		KeyBinding.onTick(attackKey);

		MouseUtil.setButtonState(attackKey + 100, true);

		lastMs = currTime;
		holding = true;
	}
}
