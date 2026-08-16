package biggie.module.modules.combat;

import biggie.event.client.TickEvent;
import biggie.event.input.PostPlayerInputEvent;
import biggie.event.motion.JumpEvent;
import biggie.event.motion.MotionEvent;
import biggie.event.motion.StrafeEvent;
import biggie.module.Module;
import biggie.module.ModuleCategory;
import biggie.setting.settings.BooleanSetting;
import biggie.setting.settings.DoubleSetting;
import biggie.setting.settings.ListSetting;
import biggie.util.player.RotationUtil;
import net.lenni0451.asmevents.event.EventTarget;
import net.lenni0451.asmevents.event.enums.EnumEventType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;

// TODO: Adicionar e implementar o autoblock.
public class KillAura extends Module {
	private final DoubleSetting aps = new DoubleSetting("APS", 20, 1, 20, 0.5);
	private final DoubleSetting attackRange = new DoubleSetting("Attack Range", 3, 3, 6, 0.05);

	private final ListSetting targetMode = new ListSetting("Target", "Switch", "Single", "Switch");
	private final DoubleSetting switchDelay = new DoubleSetting("Switch Delay", 200, 100, 1000, 50);

	private final ListSetting rotationMode = new ListSetting("Rotation", "Silent", "Silent", "Lock");

	private final ListSetting blockMode = new ListSetting("Block", "Pre", "None", "Pre", "Post");
	private final DoubleSetting blockRange = new DoubleSetting("Block Range", 3, 3, 6, 0.05);

	private final BooleanSetting moveFix = new BooleanSetting("Movement Fix", true);

	private final ArrayList<EntityLivingBase> targets = new ArrayList<>();
	private EntityLivingBase target = null;

	private int switchIndex = 0;
	private long switchMs = 0;

	private long attackMs = 0;

	private float yaw = Float.NaN;
	private float pitch = Float.NaN;

	public KillAura() {
		super("KillAura", ModuleCategory.COMBAT, Keyboard.KEY_NONE);
	}

	@Override
	public void onDisable() {
		clearTargetAndRotations(true);
	}

	@Override
	public String getInfo() {
		return targetMode.value;
	}

	@EventTarget
	public void onTick(TickEvent event) {
		if (event.getType() != EnumEventType.PRE)
			return;

		if (mc.thePlayer == null || mc.theWorld == null)
			return;

		if (mc.currentScreen != null) {
			clearTargetAndRotations(false);
			return;
		}

		final long currTime = System.currentTimeMillis();

		final ItemStack currItem = mc.thePlayer.getHeldItem();

		final boolean shouldBlock =
				findTargets(attackRange.value * attackRange.value, blockRange.value * blockRange.value) &&
						currItem != null &&
						currItem.getItem() instanceof ItemSword;
		final boolean shouldAttack = currTime - attackMs > (1000.0f / aps.value);
		final boolean shouldSwitch = currTime - switchMs > switchDelay.value;

		// TODO: Handle Block.

		if (targets.isEmpty()) {
			clearTargetAndRotations(false);
			return;
		}

		if (shouldSwitch) {
			if (switchIndex + 1 >= targets.size())
				switchIndex = 0;
			else
				++switchIndex;

			switchMs = currTime;
		}

		target = targets.get(switchIndex);

		if (shouldAttack) {
			mc.thePlayer.swingItem();
			mc.playerController.attackEntity(mc.thePlayer, target);
			attackMs = currTime;
		}

		updateRotations(target);
	}

	@EventTarget
	public void onMotion(MotionEvent event) {
		if (event.getType() != EnumEventType.PRE)
			return;

		if (target == null)
			return;

		if (rotationMode.value.equals("Silent")) {
			event.yaw = yaw;
			event.pitch = pitch;
		} else if (rotationMode.value.equals("Lock")) {
			mc.thePlayer.rotationYaw = yaw;
			mc.thePlayer.rotationPitch = pitch;
		}
	}

	@EventTarget
	public void onJump(JumpEvent event) {
		if (Float.isNaN(yaw) || !moveFix.value)
			return;

		event.yaw = yaw;
	}

	@EventTarget
	public void onPostPlayerInput(PostPlayerInputEvent event) {
		if (Float.isNaN(yaw) || !moveFix.value)
			return;

		final float[] fixedMove = RotationUtil.getFixedMove(
				mc.thePlayer.rotationYaw, yaw,
				mc.thePlayer.movementInput.moveForward, mc.thePlayer.movementInput.moveStrafe
		);

		event.moveForward = fixedMove[0] * ((mc.thePlayer.isSneaking()) ? 0.3f : 1.0f);
		event.moveStrafe = fixedMove[1] * ((mc.thePlayer.isSneaking()) ? 0.3f : 1.0f);
	}

	@EventTarget
	public void onStrafe(StrafeEvent event) {
		if (Float.isNaN(yaw) || !moveFix.value)
			return;

		event.yaw = yaw;
	}

	void updateRotations(final EntityLivingBase target) {
		final float fixedLastYaw = Float.isNaN(yaw) ? mc.thePlayer.rotationYaw : yaw;
		final float fixedLastPitch = Float.isNaN(pitch) ? mc.thePlayer.rotationPitch : pitch;

		final float[] rots = RotationUtil.getRotationTo(mc.thePlayer, target.posX, target.posY + target.getEyeHeight(), target.posZ);

		final float patchedYaw = RotationUtil.getGCDPatchedYaw(mc, fixedLastYaw, rots[0]);
		final float patchedPitch = RotationUtil.getGCDPatchedPitch(mc, fixedLastPitch, rots[1]);

		yaw = patchedYaw;
		pitch = patchedPitch;
	}

	void clearTargetAndRotations(final boolean delays) {
		target = null;
		targets.clear();

		switchMs = 0;
		attackMs = 0;

		switchIndex = 0;

		final float fixedLastYaw = Float.isNaN(yaw) ? mc.thePlayer.rotationYaw : yaw;
		final float fixedLastPitch = Float.isNaN(pitch) ? mc.thePlayer.rotationPitch : pitch;

		mc.thePlayer.rotationYaw = RotationUtil.getGCDPatchedYaw(mc, fixedLastYaw, mc.thePlayer.rotationYaw);
		mc.thePlayer.rotationPitch = RotationUtil.getGCDPatchedPitch(mc, fixedLastPitch, mc.thePlayer.rotationPitch);

		yaw = Float.NaN;
		pitch = Float.NaN;
	}

	boolean findTargets(final double sqAttackRange, final double sqBlockRange) {
		targets.clear();

		boolean shouldBlock = false;

		for (final Entity en : mc.theWorld.loadedEntityList) {
			if (!(en instanceof EntityLivingBase))
				continue;

			if (!(en instanceof EntityPlayer))
				continue;

			if (en == mc.thePlayer)
				continue;

			if (en.isDead)
				continue;

			final double module = en.getDistanceSq(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);

			if (module <= sqBlockRange)
				shouldBlock = true;

			if (module > sqAttackRange)
				continue;

			targets.add((EntityLivingBase) en);
		}

		return shouldBlock;
	}
}
