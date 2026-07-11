package biggie.mixin.model;

import biggie.util.render.ServerRotation;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBiped;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
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
			args.set(4, ServerRotation.PITCH);

			ServerRotation.ROTATE = false;
		}
	}
}
