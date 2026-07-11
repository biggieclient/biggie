package biggie.mixin.client;

import biggie.Biggie;
import biggie.event.client.GameLoopEvent;
import biggie.event.client.LoadWorldEvent;
import biggie.event.client.TickEvent;
import biggie.manager.ModuleManager;
import biggie.module.Module;
import biggie.module.modules.combat.NoHitDelay;
import net.lenni0451.asmevents.EventManager;
import net.lenni0451.asmevents.event.enums.EnumEventType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

	@Shadow
	private int leftClickCounter;

	@Inject(
			method = "startGame",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/client/Minecraft;ingameGUI:Lnet/minecraft/client/gui/GuiIngame;",
					opcode = Opcodes.PUTFIELD,
					shift = At.Shift.AFTER
			)
	)
	public void startGame_startClient(CallbackInfo ci) {
		Biggie.init();
	}

	@Inject(
			method = "runGameLoop",
			at = @At("HEAD")
	)
	public void runGameLoop_callGameLoopEvent(CallbackInfo ci) {
		EventManager.call(new GameLoopEvent());
	}

	@Inject(
			method = "runTick",
			at = @At("HEAD")
	)
	public void runTick_callPreTickEvent(CallbackInfo ci) {
		EventManager.call(new TickEvent(EnumEventType.PRE));
	}

	@Inject(
			method = "runTick",
			at = @At("TAIL")
	)
	public void runTick_callPostTickEvent(CallbackInfo ci) {
		EventManager.call(new TickEvent(EnumEventType.POST));
	}

	@Inject(
			method = "runTick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/Minecraft;dispatchKeypresses()V",
					shift = At.Shift.AFTER
			)
	)
	public void runTick_checkModuleKeybind(CallbackInfo ci) {
		int key = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey();

		if (Keyboard.getEventKeyState()) {
			for (Module module : ModuleManager.MODULES) {
				if (key == module.keybind) {
					module.toggle();
				}
			}
		}
	}

	@Inject(
			method = "clickMouse",
			at = @At("HEAD")
	)
	public void clickMouse_removeHitDelay(CallbackInfo ci) {
		if (ModuleManager.getModule(NoHitDelay.class).isEnabled()) {
			this.leftClickCounter = 0;
		}
	}

	@Inject(
			method = "loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V",
			at = @At("HEAD")
	)
	public void loadWorld_callLoadWorldEvent(
			WorldClient worldClientIn,
			String loadingMessage,
			CallbackInfo ci
	) {
		EventManager.call(new LoadWorldEvent());
	}
}
