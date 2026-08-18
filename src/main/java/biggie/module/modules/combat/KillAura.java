package biggie.module.modules.combat;

import biggie.event.client.TickEvent;
import biggie.event.input.PostPlayerInputEvent;
import biggie.event.motion.JumpEvent;
import biggie.event.motion.MotionEvent;
import biggie.event.motion.StrafeEvent;
import biggie.module.Module;
import biggie.module.ModuleCategory;
import biggie.module.modules.misc.AntiBot;
import biggie.setting.settings.BooleanSetting;
import biggie.setting.settings.DoubleSetting;
import biggie.setting.settings.ListSetting;
import biggie.util.player.ChatUtil;
import biggie.util.player.RotationUtil;
import biggie.util.render.ServerRotation;
import net.lenni0451.asmevents.event.EventTarget;
import net.lenni0451.asmevents.event.enums.EnumEventType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.MovingObjectPosition;
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

	private final BooleanSetting teamsCheck = new BooleanSetting("Teams Check", true);
	private final BooleanSetting throughBlocks = new BooleanSetting("Through Blocks", true);
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
				findTargets(((attackRange.value * 2) * (attackRange.value * 2)), blockRange.value * blockRange.value) &&
				currItem != null &&
				currItem.getItem() instanceof ItemSword;
		final boolean shouldAttack = currTime - attackMs >= (1000.0f / aps.value);
		final boolean shouldSwitch = currTime - switchMs > switchDelay.value && targetMode.value.equals("Switch");

		// TODO: Lidar com o block.

		if (targets.isEmpty()) {
			clearTargetAndRotations(false);
			return;
		}

		final boolean success = tryToTarget(currTime, shouldSwitch, shouldAttack);

		if (success)
			return;

		// Caso a primeira tentativa é invalida tentamos todos os outros possiveis targets.
		for (int i = 0; i < targets.size(); ++i) {
			final EntityLivingBase en = targets.get(i);

			final boolean iterationSuccess = tryToTargetEn(en, currTime, shouldAttack);

			if (iterationSuccess) {
				switchMs = currTime;
				switchIndex = i;
				return;
			}
		}

		clearTargetAndRotations(false);
	}

	@EventTarget
	public void onMotion(MotionEvent event) {
		if (event.getType() != EnumEventType.PRE)
			return;

		if (target == null || Float.isNaN(yaw) || Float.isNaN(pitch))
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
				mc.thePlayer,
				mc.thePlayer.rotationYaw,
				yaw,
				mc.thePlayer.movementInput.moveForward,
				mc.thePlayer.movementInput.moveStrafe
		);

		event.moveForward = fixedMove[0];
		event.moveStrafe = fixedMove[1];
	}

	@EventTarget
	public void onStrafe(StrafeEvent event) {
		if (Float.isNaN(yaw) || !moveFix.value)
			return;

		event.yaw = yaw;
	}

	boolean tryToTarget(final long currTime, final boolean shouldSwitch, final boolean shouldAttack) {
		if (shouldSwitch) {
			++switchIndex;
			switchMs = currTime;
		}

		if (switchIndex >= targets.size())
			switchIndex = 0;

		final EntityLivingBase possibleTarget = targets.get(switchIndex);

		final boolean validTarget = updateRotations(possibleTarget, true);

		if (!validTarget)
			return false;

		target = possibleTarget;

		if (shouldAttack) {
			mc.thePlayer.swingItem();
			mc.playerController.attackEntity(mc.thePlayer, target);
			attackMs = currTime;
		}

		return true;
	}

	boolean tryToTargetEn(final EntityLivingBase possibleTarget, final long currTime, final boolean shouldAttack) {
		final boolean validTarget = updateRotations(possibleTarget, true);

		if (!validTarget)
			return false;

		target = possibleTarget;

		if (shouldAttack) {
			mc.thePlayer.swingItem();
			mc.playerController.attackEntity(mc.thePlayer, target);
			attackMs = currTime;
		}

		return true;
	}

	// WARNING: Esse 'shouldRayTrace' é meio inutil porque não tem nenhuma opção pra usar ou não, mas fodase
	// acho que não usar isso fica meio injogavel porque o range não considera a boundingBox do mano.
	boolean updateRotations(final EntityLivingBase target, final boolean shouldRayTrace) {
		final float fixedLastYaw = Float.isNaN(yaw) ? mc.thePlayer.rotationYaw : yaw;
		final float fixedLastPitch = Float.isNaN(pitch) ? mc.thePlayer.rotationPitch : pitch;

		final double bestRelY = RotationUtil.getBestTargetRelY(mc.thePlayer, target);

		final float[] rots = RotationUtil.getRotationTo(mc.thePlayer, target.posX, target.posY + bestRelY, target.posZ);

		final float patchedYaw = RotationUtil.getGCDPatchedYaw(mc, fixedLastYaw, rots[0]);
		final float patchedPitch = RotationUtil.getGCDPatchedPitch(mc, fixedLastPitch, rots[1]);

		if (shouldRayTrace) {
			final MovingObjectPosition rayTrace =
					RotationUtil.rayTraceAll(mc.thePlayer, mc.theWorld, patchedYaw, patchedPitch, attackRange.value * 2, !throughBlocks.value);

			if (rayTrace == null || rayTrace.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY || rayTrace.entityHit != target)
				return false;

			if (mc.thePlayer.getPositionEyes(1.0f).distanceTo(rayTrace.hitVec) >= attackRange.value)
				return false;
		}

		yaw = patchedYaw;
		pitch = patchedPitch;

		return true;
	}

	void clearTargetAndRotations(final boolean delays) {
		target = null;
		targets.clear();

		if (delays) {
			switchMs = 0;
			attackMs = 0;
		}

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
			if (!(en instanceof EntityPlayer))
				continue;

			if (en == mc.thePlayer)
				continue;

			if (en.isDead)
				continue;

			final EntityLivingBase enLivingBase = (EntityLivingBase) en;

			if (enLivingBase.isOnSameTeam(mc.thePlayer) && teamsCheck.value)
				continue;

			if (AntiBot.botList.contains(enLivingBase))
				continue;

			final double module = en.getDistanceSq(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);

			if (module <= sqBlockRange)
				shouldBlock = true;

			if (module > sqAttackRange)
				continue;

			targets.add(enLivingBase);
		}

		return shouldBlock;
	}
}
