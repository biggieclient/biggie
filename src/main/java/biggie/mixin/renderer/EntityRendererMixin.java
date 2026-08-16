package biggie.mixin.renderer;

import biggie.event.render.Render3DEvent;
import biggie.event.render.RenderTickEvent;
import net.lenni0451.asmevents.EventManager;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

	@Inject(
			method = "renderHand",
			at = @At("HEAD")
	)
	public void renderHand_callRender3dEvent(
			float partialTicks,
			int xOffset,
			CallbackInfo ci
	) {
		EventManager.call(new Render3DEvent());
	}
}
