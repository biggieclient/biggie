package biggie.module.modules.combat;

import biggie.event.client.GameLoopEvent;
import biggie.event.client.TickEvent;
import biggie.event.render.RenderTickEvent;
import biggie.module.Module;
import biggie.module.ModuleCategory;
import biggie.setting.settings.BooleanSetting;
import biggie.setting.settings.DoubleSetting;
import biggie.setting.settings.ListSetting;
import biggie.util.math.MathUtil;
import biggie.util.player.RotationUtil;
import biggie.util.render.RenderUtil;
import net.lenni0451.asmevents.event.EventTarget;
import net.lenni0451.asmevents.event.enums.EnumEventType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

// TODO: Adicionar o modo Linear que só soma um passo fixo e o modo Adaptive
//  que é literalmente o atual que fica mais lerdo ou mais rapido dependendo do gap da rotação.
public class AimAssist extends Module {
	private final ListSetting mode = new ListSetting("Mode", "Recursive", "Linear", "Exponential");

	private final BooleanSetting horizontal = new BooleanSetting(
			"Horizontal",
			true
	);

	private final BooleanSetting vertical = new BooleanSetting(
			"Vertical",
			true
	);

	private final DoubleSetting range = new DoubleSetting(
			"Range",
			4.0,
			3.0,
			6.0,
			0.01
	);

	private final DoubleSetting fov = new DoubleSetting(
			"FOV",
			45,
			45,
			360,
			5
	);

	private final DoubleSetting smooth = new DoubleSetting(
			"Smooth",
			5,
			1,
			10,
			0.5
	);

	private final DoubleSetting speed = new DoubleSetting(
			"Speed",
			5,
			1,
			10,
			0.5
	);

	private final BooleanSetting clickOnly = new BooleanSetting(
			"Click Only",
			true
	);

	private final BooleanSetting breakBlocks = new BooleanSetting(
			"Break Blocks",
			false
	);

	private Entity target = null;
	private float[] rots = null;

	public AimAssist() {
		super("AimAssist", ModuleCategory.COMBAT, Keyboard.KEY_NONE);
	}

	@Override
	public void onDisable() {
		target = null;
		rots = null;
	}

	@EventTarget
	public void onTick(TickEvent event) {
		if (event.getType() != EnumEventType.PRE)
			return;

		if (mc.theWorld == null || mc.thePlayer == null || mc.currentScreen != null) {
			clearTargetAndRots();
			return;
		}

		if (clickOnly.value && !Mouse.isButtonDown(mc.gameSettings.keyBindAttack.getKeyCode() + 100)) {
			clearTargetAndRots();
			return;
		}

		if (breakBlocks.value && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
			clearTargetAndRots();
			return;
		}

		final double sqRange = range.value * range.value;

		for (final Entity en : mc.theWorld.loadedEntityList) {
			if (!(en instanceof EntityLivingBase))
				continue;

			if (en == mc.thePlayer)
				continue;

			if (en.isDead)
				continue;

			if (mc.thePlayer.getDistanceSqToEntity(en) > sqRange)
				continue;

			final double bestTargetRelY = RotationUtil.getBestTargetRelY(mc.thePlayer, (EntityLivingBase) en);

			final float[] enRots = RotationUtil.getRotationTo(
					mc.thePlayer,
					en.posX,
					en.posY + bestTargetRelY,
					en.posZ
			);

			if (!RotationUtil.isInFOV(mc.thePlayer.rotationYaw, enRots[0], fov.value.floatValue()))
				continue;

			rots = enRots;
			target = en;

			// esse return serve pra pegar o primeiro player que achar, se fizermos um switch vamo ter que tirar.
			return;
		}

		clearTargetAndRots();
	}

	@EventTarget(noParamEvents = GameLoopEvent.class)
	public void onGameLoop() {
		if (mc.theWorld == null || mc.thePlayer == null || mc.currentScreen != null)
			return;

		if (target == null)
			return;

		final float yaw = RotationUtil.getGCDPatchedYaw(mc, mc.thePlayer.rotationYaw, rots[0]);
		final float pitch = RotationUtil.getGCDPatchedYaw(mc, mc.thePlayer.rotationPitch, rots[1]);

		if (horizontal.value) {
			if (mode.value.equals("Exponential")) {
				mc.thePlayer.rotationYaw = RotationUtil.getInterpRot(mc.thePlayer.rotationYaw, yaw, (float) (speed.value / (smooth.value * 10)));
			} else if (mode.value.equals("Linear")) {
				mc.thePlayer.rotationYaw += MathUtil.getLinearStep(mc.thePlayer.rotationYaw, yaw, (float) (speed.value / (smooth.value * 10)));
			}
		}

		if (vertical.value) {
			if (mode.value.equals("Exponential")) {
				mc.thePlayer.rotationPitch = RotationUtil.getInterpRot(mc.thePlayer.rotationPitch, pitch, (float) (speed.value / (smooth.value * 20)));
			} else if (mode.value.equals("Linear")) {
				mc.thePlayer.rotationPitch += MathUtil.getLinearStep(mc.thePlayer.rotationPitch, pitch, (float) (speed.value / (smooth.value * 8)));
			}
		}
	}

	void clearTargetAndRots() {
		target = null;
		rots = null;
	}
}
