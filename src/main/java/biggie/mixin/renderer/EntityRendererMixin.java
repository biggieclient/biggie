package biggie.mixin.renderer;

import biggie.event.render.Render3DEvent;
import biggie.util.render.RenderUtil;
import net.lenni0451.asmevents.EventManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
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
					target = "Lnet/minecraftforge/client/ForgeHooksClient;dispatchRenderLast(Lnet/minecraft/client/renderer/RenderGlobal;F)V",
					shift = At.Shift.AFTER
			)
	)
	public void renderWorldPass_callRender3dEvent(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
		EventManager.call(new Render3DEvent());
	}
}
