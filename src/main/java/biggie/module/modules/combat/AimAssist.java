package biggie.module.modules.combat;

import biggie.event.render.RenderTickEvent;
import biggie.module.Module;
import biggie.module.ModuleCategory;
import biggie.setting.settings.BooleanSetting;
import biggie.setting.settings.DoubleSetting;
import biggie.util.player.RotationUtil;
import net.lenni0451.asmevents.event.EventTarget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class AimAssist extends Module {
	private final DoubleSetting range = new DoubleSetting(
			"Range",
			4.0,
			3.0,
			6.0,
			0.01
	);

	private final BooleanSetting clickOnly = new BooleanSetting(
			"Click Only",
			true
	);

	private final BooleanSetting throughBlocks = new BooleanSetting(
			"Through Blocks",
			false
	);

	private Entity target;
	private float lastYaw = 0;

	public AimAssist() {
		super("AimAssist", ModuleCategory.COMBAT, Keyboard.KEY_NONE);
	}

	@Override
	public void onDisable() {
		target = null;
		lastYaw = 0;
	}

	@EventTarget(noParamEvents = RenderTickEvent.class)
	public void onRenderTick() {
		if (mc.theWorld != null && mc.thePlayer != null) {
			if (!clickOnly.value || Mouse.isButtonDown(mc.gameSettings.keyBindAttack.getKeyCode() + 100)) {
				for (Entity entity : mc.theWorld.loadedEntityList) {
					if (!(entity instanceof EntityLivingBase)) {
						continue;
					}

					if (entity == mc.thePlayer) {
						continue;
					}

					if (entity.isDead) {
						if (entity.getEntityId() == target.getEntityId()) {
							target = null;
						}

						continue;
					}

					if (target != null) {
						if (entity.getEntityId() == target.getEntityId()) {
							target = entity;

							continue;
						}
					}

					if (mc.thePlayer.getDistanceToEntity(entity) < range.value) {
						if (target == null) {
							target = entity;
						}
					}
				}

				if (target != null) {
					float[] rots = RotationUtil.getRotationTo(
							mc.thePlayer,
							target.posX,
							target.posY,
							target.posZ
					);
					float gcd = (float) Math.pow(mc.gameSettings.mouseSensitivity * 0.6f + 0.2f, 3f) * 1.2f;
					float deltaYaw = rots[0] - lastYaw;
					float yaw = lastYaw + MathHelper.wrapAngleTo180_float(Math.round(deltaYaw / gcd) * gcd);

					mc.thePlayer.rotationYaw = lastYaw + (yaw - lastYaw) * 0.5F;
					lastYaw = yaw;

					target = null;
				}
			}
		} else {
			target = null;
			lastYaw = 0;
		}
	}
}
