package biggie.mixin.model;

import biggie.util.render.ServerRotation;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ModelBiped.class)
public class ModelBipedMixin {

	@ModifyArgs(
			method = "render",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/model/ModelBiped;setRotationAngles(FFFFFFLnet/minecraft/entity/Entity;)V"
			)
	)
	public void setRotationAngles_changeHeadRotation(Args args) {
		if (args.get(args.size() - 1) instanceof EntityPlayerSP) {
			final EntityPlayerSP player = args.get(args.size() - 1);
			final float yaw = ServerRotation.getInterpYaw();

			//if (ServerRotation.ROTATE_BODY)
			//	args.get.rotateAngleY = yaw;

			final float yawOffset = yaw - ServerRotation.interpYaw(player.renderYawOffset, player.prevRenderYawOffset);

			args.set(3, MathHelper.wrapAngleTo180_float(yawOffset));
			args.set(4, ServerRotation.getInterpPitch());
		}
	}
}
