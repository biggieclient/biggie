package byteware.module.modules.combat;

import byteware.event.client.GameLoopEvent;
import byteware.module.Module;
import byteware.module.ModuleCategory;
import byteware.util.player.ChatUtil;
import net.lenni0451.asmevents.event.EventTarget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Keyboard;

public class KillAura extends Module {
	private long lastMs;

	public KillAura() {
		super("KillAura", ModuleCategory.COMBAT, Keyboard.KEY_NONE);
	}

	@EventTarget
	public void onGameLoop(GameLoopEvent event) {
		if (mc.theWorld != null && mc.thePlayer != null) {
			for (Entity entity : mc.theWorld.loadedEntityList) {
				if (entity instanceof EntityLivingBase) {
					EntityLivingBase en = (EntityLivingBase) entity;

					if (en.deathTime != 0) {
						continue;
					}

					if (en == mc.thePlayer) {
						continue;
					}

					if (mc.thePlayer.getDistanceToEntity(en) <= 3.15) {
						double deltaX = en.posX - mc.thePlayer.posX;
						double deltaZ = en.posZ - mc.thePlayer.posZ;
						double length = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

						deltaX /= length;
						deltaZ /= length;

						float yaw = (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90;

						if (yaw < 0) {
							yaw += 360;
						}
					}
				}
			}
		}
	}
}
