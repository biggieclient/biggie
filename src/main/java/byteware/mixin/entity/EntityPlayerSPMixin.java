package byteware.mixin.entity;

import byteware.event.motion.ItemSlowDownEvent;
import byteware.event.motion.LivingUpdateEvent;
import com.mojang.authlib.GameProfile;
import net.lenni0451.asmevents.EventManager;
import net.lenni0451.asmevents.event.enums.EnumEventType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.potion.Potion;
import net.minecraft.util.MovementInput;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerSP.class)
public abstract class EntityPlayerSPMixin extends AbstractClientPlayer {
	@Shadow
	public MovementInput movementInput;

	public EntityPlayerSPMixin(
			World worldIn,
			GameProfile playerProfile
	) {
		super(worldIn, playerProfile);
	}

	@Shadow
	protected abstract boolean isCurrentViewEntity();

	@Shadow
	protected int sprintToggleTimer;

	@Shadow
	public float timeInPortal;

	@Shadow
	protected Minecraft mc;

	@Shadow
	public int sprintingTicksLeft;

	@Shadow
	public float prevTimeInPortal;

	@Shadow
	public abstract boolean isRidingHorse();

	@Shadow
	private int horseJumpPowerCounter;

	@Shadow
	private float horseJumpPower;

	@Shadow
	protected abstract void sendHorseJump();

	@Inject(
			method = "onLivingUpdate",
			at = @At("HEAD"),
			cancellable = true
	)
	public void onLivingUpdate_rewrite(CallbackInfo ci) {
		EventManager.call(new LivingUpdateEvent(EnumEventType.PRE));

		if (this.sprintingTicksLeft > 0) {
			--this.sprintingTicksLeft;

			if (this.sprintingTicksLeft == 0) {
				this.setSprinting(false);
			}
		}

		if (this.sprintToggleTimer > 0) {
			--this.sprintToggleTimer;
		}

		this.prevTimeInPortal = this.timeInPortal;

		if (this.inPortal) {
			if (this.mc.currentScreen != null && !this.mc.currentScreen.doesGuiPauseGame()) {
				this.mc.displayGuiScreen(null);
			}

			if (this.timeInPortal == 0.0F) {
				this.mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("portal.trigger"), this.rand.nextFloat() * 0.4F + 0.8F));
			}

			this.timeInPortal += 0.0125F;

			if (this.timeInPortal >= 1.0F) {
				this.timeInPortal = 1.0F;
			}

			this.inPortal = false;
		} else if (this.isPotionActive(Potion.confusion) && this.getActivePotionEffect(Potion.confusion).getDuration() > 60) {
			this.timeInPortal += 0.006666667F;

			if (this.timeInPortal > 1.0F) {
				this.timeInPortal = 1.0F;
			}
		} else {
			if (this.timeInPortal > 0.0F) {
				this.timeInPortal -= 0.05F;
			}

			if (this.timeInPortal < 0.0F) {
				this.timeInPortal = 0.0F;
			}
		}

		if (this.timeUntilPortal > 0) {
			--this.timeUntilPortal;
		}

		boolean isJumping = this.movementInput.jump;
		boolean isSneaking = this.movementInput.sneak;
		float walkVelocity = 0.8F;
		boolean isWalking = this.movementInput.moveForward >= walkVelocity;

		this.movementInput.updatePlayerMoveState();

		ItemSlowDownEvent itemSlowDownEvent = new ItemSlowDownEvent(
				0.2F,
				0.2F,
				!this.isUsingItem()
		);

		EventManager.call(itemSlowDownEvent);

		if (this.isUsingItem() && !this.isRiding()) {
			this.movementInput.moveStrafe *= itemSlowDownEvent.strafe;
			this.movementInput.moveForward *= itemSlowDownEvent.forward;

			if (!itemSlowDownEvent.sprint) {
				this.sprintToggleTimer = 0;
			}
		}

		this.pushOutOfBlocks(this.posX - (double) this.width * 0.35D, this.getEntityBoundingBox().minY + 0.5D, this.posZ + (double) this.width * 0.35D);
		this.pushOutOfBlocks(this.posX - (double) this.width * 0.35D, this.getEntityBoundingBox().minY + 0.5D, this.posZ - (double) this.width * 0.35D);
		this.pushOutOfBlocks(this.posX + (double) this.width * 0.35D, this.getEntityBoundingBox().minY + 0.5D, this.posZ - (double) this.width * 0.35D);
		this.pushOutOfBlocks(this.posX + (double) this.width * 0.35D, this.getEntityBoundingBox().minY + 0.5D, this.posZ + (double) this.width * 0.35D);

		boolean allowSprinting = (float) this.getFoodStats().getFoodLevel() > 6.0F || this.capabilities.allowFlying;

		if (this.onGround && !isSneaking && !isWalking && this.movementInput.moveForward >= walkVelocity && !this.isSprinting() && allowSprinting && itemSlowDownEvent.sprint && !this.isPotionActive(Potion.blindness)) {
			if (this.sprintToggleTimer <= 0 && !this.mc.gameSettings.keyBindSprint.isKeyDown()) {
				this.sprintToggleTimer = 7;
			} else {
				this.setSprinting(true);
			}
		}

		if (!this.isSprinting() && this.movementInput.moveForward >= walkVelocity && allowSprinting && itemSlowDownEvent.sprint && !this.isPotionActive(Potion.blindness) && this.mc.gameSettings.keyBindSprint.isKeyDown()) {
			this.setSprinting(true);
		}

		if (this.isSprinting() && (this.movementInput.moveForward < walkVelocity || this.isCollidedHorizontally || !allowSprinting || !itemSlowDownEvent.sprint)) {
			this.setSprinting(false);
		}

		if (this.capabilities.allowFlying) {
			if (this.mc.playerController.isSpectatorMode()) {
				if (!this.capabilities.isFlying) {
					this.capabilities.isFlying = true;
					this.sendPlayerAbilities();
				}
			} else if (!isJumping && this.movementInput.jump) {
				if (this.flyToggleTimer == 0) {
					this.flyToggleTimer = 7;
				} else {
					this.capabilities.isFlying = !this.capabilities.isFlying;
					this.sendPlayerAbilities();
					this.flyToggleTimer = 0;
				}
			}
		}

		if (this.capabilities.isFlying && this.isCurrentViewEntity()) {
			if (this.movementInput.sneak) {
				this.motionY -= this.capabilities.getFlySpeed() * 3.0F;
			}

			if (this.movementInput.jump) {
				this.motionY += this.capabilities.getFlySpeed() * 3.0F;
			}
		}

		if (this.isRidingHorse()) {
			if (this.horseJumpPowerCounter < 0) {
				++this.horseJumpPowerCounter;

				if (this.horseJumpPowerCounter == 0) {
					this.horseJumpPower = 0.0F;
				}
			}

			if (isJumping && !this.movementInput.jump) {
				this.horseJumpPowerCounter = -10;
				this.sendHorseJump();
			} else if (!isJumping && this.movementInput.jump) {
				this.horseJumpPowerCounter = 0;
				this.horseJumpPower = 0.0F;
			} else if (isJumping) {
				++this.horseJumpPowerCounter;

				if (this.horseJumpPowerCounter < 10) {
					this.horseJumpPower = (float) this.horseJumpPowerCounter * 0.1F;
				} else {
					this.horseJumpPower = 0.8F + 2.0F / (float) (this.horseJumpPowerCounter - 9) * 0.1F;
				}
			}
		} else {
			this.horseJumpPower = 0.0F;
		}

		super.onLivingUpdate();

		if (this.onGround && this.capabilities.isFlying && !this.mc.playerController.isSpectatorMode()) {
			this.capabilities.isFlying = false;

			this.sendPlayerAbilities();
		}

		EventManager.call(new LivingUpdateEvent(EnumEventType.POST));

		ci.cancel();
	}
}
