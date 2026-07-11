package biggie.mixin.renderer;

import biggie.event.render.Render3DEvent;
import net.lenni0451.asmevents.EventManager;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

	@Inject(
			method = "renderWorldPass",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/EntityRenderer;renderHand(FI)V"
			)
	)
	public void renderWorldPass_callRender3dEvent(
			int pass,
			float partialTicks,
			long finishTimeNano,
			CallbackInfo ci
	) {
		EventManager.call(new Render3DEvent());
	}
}
