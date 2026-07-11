package biggie.mixin.client;

import biggie.event.client.AttackEvent;
import biggie.manager.ModuleManager;
import biggie.module.modules.misc.FastMine;
import biggie.module.modules.misc.NoBlockHitDelay;
import net.lenni0451.asmevents.EventManager;
import net.lenni0451.asmevents.event.enums.EnumEventType;
import net.minecraft.block.Block;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerControllerMP.class)
public class PlayerControllerMPMixin {

	@Shadow
	private int blockHitDelay;

	@Inject(
			method = "onPlayerDamageBlock",
			at = @At("HEAD")
	)
	public void onPlayerDamageBlock_removeBlockHitDelay(
			BlockPos posBlock,
			EnumFacing directionFacing,
			CallbackInfoReturnable<Boolean> cir
	) {
		NoBlockHitDelay noBlockHitDelay = ModuleManager.getModule(NoBlockHitDelay.class);

		if (noBlockHitDelay.isEnabled()) {
			this.blockHitDelay = noBlockHitDelay.delay.value;
		}
	}

	@Redirect(
			method = "onPlayerDamageBlock",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/block/Block;getPlayerRelativeBlockHardness(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;)F"
			)
	)
	public float onPlayerDamageBlock_modifyBreakSpeed(
			Block instance,
			EntityPlayer playerIn,
			World worldIn,
			BlockPos pos
	) {
		float hardness = instance.getPlayerRelativeBlockHardness(playerIn, worldIn, pos);
		FastMine fastMine = ModuleManager.getModule(FastMine.class);

		if (fastMine.isEnabled()) {
			switch (fastMine.mode.value) {
				case "Normal":
					return fastMine.speed.value.floatValue() / 6.0F;
				case "Increment":
					hardness += fastMine.speed.value.floatValue() / 12.0F;

					break;
			}
		}

		return hardness;
	}

	@Inject(
			method = "attackEntity",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/network/NetHandlerPlayClient;addToSendQueue(Lnet/minecraft/network/Packet;)V"
			)
	)
	public void onAttackEntity_callPreAttackEvent(
			EntityPlayer playerIn,
			Entity targetEntity,
			CallbackInfo ci
	) {
		EventManager.call(new AttackEvent(EnumEventType.PRE, targetEntity));
	}

	@Inject(
			method = "attackEntity",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/player/EntityPlayer;attackTargetEntityWithCurrentItem(Lnet/minecraft/entity/Entity;)V",
					shift = At.Shift.AFTER
			)
	)
	public void onAttackEntity_callPostAttackEvent(
			EntityPlayer playerIn,
			Entity targetEntity,
			CallbackInfo ci
	) {
		EventManager.call(new AttackEvent(EnumEventType.POST, targetEntity));
	}
}
