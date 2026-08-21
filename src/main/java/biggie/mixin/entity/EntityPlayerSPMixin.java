package biggie.mixin.entity;

import biggie.event.input.PlayerInputEvent;
import biggie.event.motion.ItemSlowDownEvent;
import biggie.event.motion.LivingUpdateEvent;
import biggie.event.motion.MotionEvent;
import biggie.event.motion.UpdateEvent;
import biggie.manager.ModuleManager;
import biggie.module.modules.misc.Scaffold;
import biggie.util.render.ServerRotation;
import com.mojang.authlib.GameProfile;
import net.lenni0451.asmevents.EventManager;
import net.lenni0451.asmevents.event.enums.EnumEventType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.potion.Potion;
import net.minecraft.util.MovementInput;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerSP.class)
public abstract class EntityPlayerSPMixin extends AbstractClientPlayer {
	public EntityPlayerSPMixin(
			World worldIn,
			GameProfile playerProfile
	) {
		super(worldIn, playerProfile);
	}

	@Shadow
	public MovementInput movementInput;

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

	@Shadow
	private boolean serverSprintState;

	@Shadow
	@Final
	public NetHandlerPlayClient sendQueue;

	@Shadow
	private boolean serverSneakState;

	@Shadow
	private double lastReportedPosX;

	@Shadow
	private double lastReportedPosY;

	@Shadow
	private double lastReportedPosZ;

	@Shadow
	private float lastReportedYaw;

	@Shadow
	private float lastReportedPitch;

	@Shadow
	private int positionUpdateTicks;

	@Inject(
			method = "onUpdateWalkingPlayer",
			at = @At("HEAD"),
			cancellable = true
	)
	public void onUpdateWalkingPlayer_rewrite(CallbackInfo ci) {
		boolean sprinting = this.isSprinting();
		boolean sneaking = this.isSneaking();

		MotionEvent preMotionEvent = new MotionEvent(
				EnumEventType.PRE,
				this.posX,
				this.getEntityBoundingBox().minY,
				this.posZ,
				this.rotationYaw,
				this.rotationPitch,
				this.onGround
		);

		if (sprinting != this.serverSprintState) {
			if (sprinting) {
				this.sendQueue.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.START_SPRINTING));
			} else {
				this.sendQueue.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.STOP_SPRINTING));
			}

			this.serverSprintState = sprinting;
		}

		if (sneaking != this.serverSneakState) {
			if (sneaking) {
				this.sendQueue.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.START_SNEAKING));
			} else {
				this.sendQueue.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.STOP_SNEAKING));
			}

			this.serverSneakState = sneaking;
		}

		if (this.isCurrentViewEntity()) {
			EventManager.call(preMotionEvent);

			ServerRotation.LAST_TICK_YAW = ServerRotation.INTERP_YAW;
			ServerRotation.LAST_TICK_PITCH = ServerRotation.INTERP_PITCH;

			ServerRotation.INTERP_YAW = ServerRotation.getInterpYaw();
			ServerRotation.INTERP_PITCH = ServerRotation.getInterpPitch();

			ServerRotation.DEST_YAW = preMotionEvent.yaw;
			ServerRotation.DEST_PITCH = preMotionEvent.pitch;

			double diffX = preMotionEvent.x - this.lastReportedPosX;
			double diffY = preMotionEvent.y - this.lastReportedPosY;
			double diffZ = preMotionEvent.z - this.lastReportedPosZ;

			double diffYaw = preMotionEvent.yaw - this.lastReportedYaw;
			double diffPitch = preMotionEvent.pitch - this.lastReportedPitch;

			boolean move = diffX * diffX + diffY * diffY + diffZ * diffZ > 9.0E-4D || this.positionUpdateTicks >= 20;
			boolean rotation = diffYaw != 0.0D || diffPitch != 0.0D;

			if (this.ridingEntity == null) {
				if (move && rotation) {
					this.sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(preMotionEvent.x, preMotionEvent.y, preMotionEvent.z, preMotionEvent.yaw, preMotionEvent.pitch, preMotionEvent.onGround));
				} else if (move) {
					this.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(preMotionEvent.x, preMotionEvent.y, preMotionEvent.z, preMotionEvent.onGround));
				} else if (rotation) {
					this.sendQueue.addToSendQueue(new C03PacketPlayer.C05PacketPlayerLook(preMotionEvent.yaw, preMotionEvent.pitch, preMotionEvent.onGround));
				} else {
					this.sendQueue.addToSendQueue(new C03PacketPlayer(preMotionEvent.onGround));
				}
			} else {
				this.sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(this.motionX, -999.0D, this.motionZ, preMotionEvent.yaw, preMotionEvent.pitch, preMotionEvent.onGround));
				move = false;
			}

			++this.positionUpdateTicks;

			if (move) {
				this.lastReportedPosX = preMotionEvent.x;
				this.lastReportedPosY = preMotionEvent.y;
				this.lastReportedPosZ = preMotionEvent.z;
				this.positionUpdateTicks = 0;
			}

			if (rotation) {
				this.lastReportedYaw = preMotionEvent.yaw;
				this.lastReportedPitch = preMotionEvent.pitch;
			}

			EventManager.call(new MotionEvent(
					EnumEventType.POST,
					this.posX,
					this.getEntityBoundingBox().minY,
					this.posZ,
					this.rotationYaw,
					this.rotationPitch,
					this.onGround
			));
		}

		ci.cancel();
	}

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

		final PlayerInputEvent preInputEvent = new PlayerInputEvent(
				EnumEventType.PRE,
				this.movementInput.moveForward,
				this.movementInput.moveStrafe
		);

		EventManager.call(preInputEvent);

		this.movementInput.updatePlayerMoveState();

		final PlayerInputEvent postInputEvent = new PlayerInputEvent(
				EnumEventType.POST,
				this.movementInput.moveForward,
				this.movementInput.moveStrafe
		);

		EventManager.call(postInputEvent);

		this.movementInput.moveForward = postInputEvent.forward;
		this.movementInput.moveStrafe = postInputEvent.strafe;

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
		final boolean scaffoldSprint = !ModuleManager.getModule(Scaffold.class).isEnabled() || !Scaffold.sprintMode.value.equals("None");

		if (this.onGround && scaffoldSprint && !isSneaking && !isWalking && this.movementInput.moveForward >= walkVelocity && !this.isSprinting() && allowSprinting && itemSlowDownEvent.sprint && !this.isPotionActive(Potion.blindness)) {
			if (this.sprintToggleTimer <= 0 && !this.mc.gameSettings.keyBindSprint.isKeyDown()) {
				this.sprintToggleTimer = 7;
			} else {
				this.setSprinting(true);
			}
		}

		if (!this.isSprinting() && scaffoldSprint && this.movementInput.moveForward >= walkVelocity && allowSprinting && itemSlowDownEvent.sprint && !this.isPotionActive(Potion.blindness) && this.mc.gameSettings.keyBindSprint.isKeyDown()) {
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

	@Inject(
			method = "onUpdate",
			at = @At("HEAD")
	)
	public void onUpdate_callPreUpdateEvent(CallbackInfo ci) {
		EventManager.call(new UpdateEvent(EnumEventType.PRE));
	}

	@Inject(
			method = "onUpdate",
			at = @At("TAIL")
	)
	public void onUpdate_callPostUpdateEvent(CallbackInfo ci) {
		EventManager.call(new UpdateEvent(EnumEventType.POST));
	}
}
