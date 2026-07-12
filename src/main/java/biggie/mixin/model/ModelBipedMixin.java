package biggie.mixin.model;

import biggie.util.render.ServerRotation;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelBiped.class)
public class ModelBipedMixin {

	@Shadow
	public ModelRenderer bipedHead;

	@Inject(
			method = "setRotationAngles",
			at = @At("TAIL")
	)
	public void setRotationAngles_changeHeadRotation(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn, CallbackInfo ci) {
		if (entityIn instanceof EntityPlayerSP) {
			final EntityPlayerSP player = (EntityPlayerSP) entityIn;
			final float yaw = ServerRotation.getInterpYaw();

			//if (ServerRotation.ROTATE_BODY)
			//	args.get.rotateAngleY = yaw;

			final float yawOffset = yaw - ServerRotation.interpYaw(player.renderYawOffset, player.prevRenderYawOffset);

			this.bipedHead.rotateAngleY = (float) Math.toRadians(MathHelper.wrapAngleTo180_float(yawOffset));
			this.bipedHead.rotateAngleX = (float) Math.toRadians(ServerRotation.getInterpPitch());
		}
	}
}
