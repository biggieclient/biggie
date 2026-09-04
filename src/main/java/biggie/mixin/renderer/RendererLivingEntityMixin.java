package biggie.mixin.renderer;

import biggie.event.render.RenderEntityNameTagEvent;
import net.lenni0451.asmevents.EventManager;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderPlayer.class)
public abstract class RendererLivingEntityMixin {
	@Inject(
			method = "renderOffsetLivingLabel*",
			at = @At("HEAD"),
			cancellable = true
	)
	private void renderOffsetLivingLabel_cancel(
			AbstractClientPlayer entityIn,
			double x,
			double y,
			double z,
			String str,
			float p_177069_9_,
			double p_177069_10_,
			CallbackInfo ci
	) {
		final RenderEntityNameTagEvent event = new RenderEntityNameTagEvent(entityIn);
		EventManager.call(event);

		if (event.isCancelled())
			ci.cancel();
	}
}
