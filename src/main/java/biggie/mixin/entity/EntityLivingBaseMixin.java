package biggie.mixin.entity;

import biggie.event.motion.JumpEvent;
import net.lenni0451.asmevents.EventManager;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityLivingBase.class)
public abstract class EntityLivingBaseMixin extends Entity {
    public EntityLivingBaseMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(
            method = "jump",
            at = @At("HEAD"),
            cancellable = true
    )
    public void jump(CallbackInfo ci) {
        final EntityLivingBase en = (EntityLivingBase) (Object) this;

        if (en instanceof EntityPlayerSP) {
            final JumpEvent jumpEvent = new JumpEvent(this.rotationYaw);
            EventManager.call(jumpEvent);

            this.motionY = 0.42f;

            if (en.isPotionActive(Potion.jump)) {
                this.motionY += (en.getActivePotionEffect(Potion.jump).getAmplifier() + 1f) * 0.1f;
            }

            if (this.isSprinting()) {
                float radYaw = (float) Math.toRadians(jumpEvent.yaw);
                this.motionX -= Math.sin(radYaw) * 0.2f;
                this.motionZ += Math.cos(radYaw) * 0.2f;
            }

            this.isAirBorne = true;
            ForgeHooks.onLivingJump(en);

            ci.cancel();
        }
    }
}
