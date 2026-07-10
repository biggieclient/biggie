package byteware.mixin.entity;

import byteware.manager.ModuleManager;
import byteware.module.modules.misc.NoBreakSlow;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
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
			at = @At("HEAD"),
			cancellable = true,
			remap = false
	)
	public void getBreakSpeed_rewrite(IBlockState state, BlockPos pos, CallbackInfoReturnable<Float> cir) {
		ItemStack stack = inventory.getCurrentItem();
		float speed = (stack == null ? 1.0F : stack.getItem().getDigSpeed(stack, state));

		if (speed > 1.0F) {
			int efficiencyModifier = EnchantmentHelper.getEfficiencyModifier(this);
			ItemStack itemstack = this.inventory.getCurrentItem();

			if (efficiencyModifier > 0 && itemstack != null) {
				speed += (float) (efficiencyModifier * efficiencyModifier + 1);
			}
		}

		if (this.isPotionActive(Potion.digSpeed)) {
			speed *= 1.0F + (float) (this.getActivePotionEffect(Potion.digSpeed).getAmplifier() + 1) * 0.2F;
		}

		NoBreakSlow noBreakSlow = ModuleManager.getModule(NoBreakSlow.class);

		if (!noBreakSlow.isEnabled() || !noBreakSlow.affectPotion.value) {
			if (this.isPotionActive(Potion.digSlowdown)) {
				float digSlowdownSpeed;

				switch (this.getActivePotionEffect(Potion.digSlowdown).getAmplifier()) {
					case 0:
						digSlowdownSpeed = 0.3F;
						break;
					case 1:
						digSlowdownSpeed = 0.09F;
						break;
					case 2:
						digSlowdownSpeed = 0.0027F;
						break;
					case 3:
					default:
						digSlowdownSpeed = 8.1E-4F;
				}

				speed *= digSlowdownSpeed;
			}
		}

		if (!noBreakSlow.isEnabled()) {
			if (this.isInsideOfMaterial(Material.water) && !EnchantmentHelper.getAquaAffinityModifier(this)) {
				speed /= 5.0F;
			}

			if (!this.onGround) {
				speed /= 5.0F;
			}
		}

		speed = net.minecraftforge.event.ForgeEventFactory.getBreakSpeed((EntityPlayer) (Object) this, state, speed, pos);

		cir.setReturnValue(speed < 0 ? 0 : speed);
	}
}
