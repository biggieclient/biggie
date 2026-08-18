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

			final float yaw = ServerRotation.getInterpYaw(ServerRotation.timer.renderPartialTicks);
			final float pitch = ServerRotation.getInterpPitch(ServerRotation.timer.renderPartialTicks);

			player.renderYawOffset = yaw;

			this.bipedHead.rotateAngleY = 0;
			this.bipedHead.rotateAngleX = (float) Math.toRadians(pitch);
		}
	}
}
