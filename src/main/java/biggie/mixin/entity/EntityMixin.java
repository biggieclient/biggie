package biggie.mixin.entity;

import biggie.event.motion.StrafeEvent;
import net.lenni0451.asmevents.EventManager;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {

	@Shadow
	public double motionX;

	@Shadow
	public double motionZ;

	@Shadow
	public float rotationYaw;

	@Inject(
			method = "moveFlying",
			at = @At("HEAD"),
			cancellable = true
	)
	public void moveFlying_callStrafeEvent(
			float strafe,
			float forward,
			float friction,
			CallbackInfo ci
	) {
		if ((Object) this instanceof EntityPlayerSP) {
			StrafeEvent strafeEvent = new StrafeEvent(this.rotationYaw, strafe, forward, friction);

			EventManager.call(strafeEvent);

			float yaw = strafeEvent.yaw;

			strafe = strafeEvent.strafe;
			forward = strafeEvent.forward;
			friction = strafeEvent.friction;

			if (strafeEvent.isCancelled()) {
				ci.cancel();

				return;
			}

			float f = strafe * strafe + forward * forward;

			if (f >= 1.0E-4F) {
				f = MathHelper.sqrt_float(f);

				if (f < 1.0F) {
					f = 1.0F;
				}

				f = friction / f;
				strafe = strafe * f;
				forward = forward * f;

				final float yawRad = (float) Math.toRadians(yaw);

				final float f1 = (float) Math.sin(yawRad);
				final float f2 = (float) Math.cos(yawRad);

				this.motionX += (-f1 * forward) + (f2 * strafe);
				this.motionZ += (f2 * forward) + (f1 * strafe);
			}

			ci.cancel();
		}
	}
}
