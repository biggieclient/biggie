package biggie.mixin.entity;

import biggie.manager.ModuleManager;
import biggie.module.modules.misc.NoBreakSlow;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayer.class)
public abstract class EntityPlayerMixin extends EntityLivingBase {
	@Shadow
	public InventoryPlayer inventory;

	public EntityPlayerMixin(World worldIn) {
		super(worldIn);
	}

	@Inject(
			method = "getBreakSpeed",
			at = @At("RETURN"),
			cancellable = true,
			remap = false
	)
	public void getBreakSpeed_removeBreakSlow(IBlockState state, BlockPos pos, CallbackInfoReturnable<Float> cir) {
		float breakSpeed = cir.getReturnValue();

		if (ModuleManager.getModule(NoBreakSlow.class).isEnabled()) {
			if (this.isInsideOfMaterial(Material.water) && !EnchantmentHelper.getAquaAffinityModifier(this)) {
				breakSpeed *= 5.0F;
			}

			if (!this.onGround) {
				breakSpeed *= 5.0F;
			}

			cir.setReturnValue(breakSpeed);
		}
	}
}
