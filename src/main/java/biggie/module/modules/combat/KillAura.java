package biggie.module.modules.combat;

import biggie.event.motion.LivingUpdateEvent;
import biggie.event.motion.MotionEvent;
import biggie.event.motion.StrafeEvent;
import biggie.event.render.Render3DEvent;
import biggie.module.Module;
import biggie.module.ModuleCategory;
import biggie.setting.settings.BooleanSetting;
import biggie.setting.settings.DoubleSetting;
import biggie.setting.settings.ListSetting;
import biggie.util.player.RotationUtil;
import biggie.util.render.RenderUtil;
import net.lenni0451.asmevents.event.EventTarget;
import net.lenni0451.asmevents.event.enums.EnumEventType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class KillAura extends Module {
	private final ListSetting rotMode = new ListSetting("Rotation Mode", "Switch", "Single", "Switch");

	private final DoubleSetting aps = new DoubleSetting("APS", 20, 1, 20, 0.5);

	private final DoubleSetting attackDist = new DoubleSetting("Attack Range", 3.15, 3.0, 6.0, 0.05);
	private final DoubleSetting blockDist = new DoubleSetting("Block Range", 4, 1, 6.0, 0.05);
	private final DoubleSetting switchDelay = new DoubleSetting("Switch Delay", 250, 50, 1000, 50);

	private final ListSetting blockMode = new ListSetting("Block Mode", "Normal", "None", "Normal");

	private final BooleanSetting swing = new BooleanSetting("Swing", true);
	private final BooleanSetting useHitbox = new BooleanSetting("Use Hitbox", true);

	private final BooleanSetting drawBox = new BooleanSetting("Draw Box", true);

	private boolean blocking = false;

	private long lastAttack = 0;
	private long lastSwitch = 0;

	private int targetIndex = 0;

	// WARNING: Usar NaN pra evitar packets invalidos ou coisa pior :)
	private float yaw = Float.NaN;
	private float pitch = Float.NaN;

	private float lastYaw = Float.NaN;
	private float lastPitch = Float.NaN;

	final List<EntityLivingBase> targetList = new ArrayList<>();
	private EntityLivingBase target = null;

	public KillAura() {
		super("KillAura", ModuleCategory.COMBAT, Keyboard.KEY_NONE);
	}

	@Override
	public void onDisable() {
		this.target = null;
		this.targetList.clear();

		lastAttack = 0;
		lastSwitch = 0;
		targetIndex = 0;

		if (blocking) {
			ReflectionHelper.setPrivateValue(EntityPlayer.class, mc.thePlayer, 0, "itemInUseCount", "field_71072_f");
			mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, new BlockPos(0, 0, 0), EnumFacing.DOWN));
			blocking = false;
		}

		lastYaw = Float.NaN;
		lastPitch = Float.NaN;

		yaw = Float.NaN;
		pitch = Float.NaN;
	}

	@EventTarget(noParamEvents = Render3DEvent.class)
	public void onRender3D() {
		if (target == null || !drawBox.value) {
			return;
		}

		final AxisAlignedBB box = target.getEntityBoundingBox();
		final AxisAlignedBB lastBox = RenderUtil.getLastTickBoundingBox(target);

		RenderUtil.drawBoundingBox(
				box.minX, box.minY, box.minZ,
				box.maxX, box.maxY, box.maxZ,
				lastBox.minX, lastBox.minY, lastBox.minZ,
				lastBox.maxX, lastBox.maxY, lastBox.maxZ,
				0, 255, 0, 63
		);
	}

	// WARNING: Deixa tudo isso no LivingUpdateEvent (PRE), ja que o yaw do strafe event
	// PRECISA e DEVE usar o mesmo yaw que o do motion event.
	// caso o strafe event usasse o yaw do motion event ele usaria o yaw do tick anterior ja
	// que o strafe event é chamado ANTES do motion event.
	@EventTarget
	public void onLivingUpdate(LivingUpdateEvent event) {
		if (event.getType() != EnumEventType.PRE)
			return;

		final long currTime = System.currentTimeMillis();
		final boolean canCheckBlock = !(mc.thePlayer.getHeldItem() == null || mc.thePlayer.getHeldItem().getItem() == null) && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword && !blockMode.value.equals("None");

		boolean canBlock = false;

		for (Entity entity : mc.theWorld.loadedEntityList) {
			if (!(entity instanceof EntityLivingBase))
				continue;

			final EntityLivingBase en = (EntityLivingBase) entity;

			if (en.deathTime != 0)
				continue;

			if (en == mc.thePlayer)
				continue;

			final double sqDist = mc.thePlayer.getDistanceSqToEntity(en);

			if (canCheckBlock && sqDist <= (blockDist.value * blockDist.value)) {
				if (!blocking) {
					mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
					ReflectionHelper.setPrivateValue(EntityPlayer.class, mc.thePlayer, 1, "itemInUseCount", "field_71072_f");
					blocking = true;
				}

				canBlock = true;
			}

			final double patchedMaxDist = useHitbox.value ? (attackDist.value + 1) : attackDist.value;

			if (sqDist > (patchedMaxDist * patchedMaxDist))
				continue;

			targetList.add(en);
		}

		if (!canBlock && blocking) {
			ReflectionHelper.setPrivateValue(EntityPlayer.class, mc.thePlayer, 0, "itemInUseCount", "field_71072_f");
			mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, new BlockPos(0, 0, 0), EnumFacing.DOWN));
			blocking = false;
		}

		if (targetList.isEmpty()) {
			lastYaw = Float.NaN;
			lastPitch = Float.NaN;

			yaw = Float.NaN;
			pitch = Float.NaN;

			target = null;
			return;
		}

		if ((currTime - lastSwitch) > switchDelay.value) {
			++targetIndex;
			lastSwitch = currTime;
		}

		if (targetIndex >= targetList.size())
			targetIndex = 0;

		if (rotMode.value.equals("Single"))
			target = targetList.get(0);
		else if (rotMode.value.equals("Switch"))
			target = targetList.get(targetIndex);

		targetList.clear();

		final boolean canAttack = (currTime - lastAttack) > (1000.0 / aps.value);

		if (target == null)
			return;

		final float[] rots = RotationUtil.getRotationTo(mc.thePlayer, target.posX, target.posY + (target.getEyeHeight() * 0.7), target.posZ);
		final AxisAlignedBB box = target.getEntityBoundingBox();

		if (box == null)
			return;

		final float fixedLastYaw = (Float.isNaN(lastYaw)) ? mc.thePlayer.rotationYaw : lastYaw;
		final float fixedLastPitch = (Float.isNaN(lastPitch)) ? mc.thePlayer.rotationPitch : lastPitch;

		final float sensibility = mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
		final float gcdPatch = (sensibility * sensibility * sensibility) * 8.0f;

		final float deltaYaw = (((rots[0] - fixedLastYaw) + 180.0f) % 360.0f + 360.0f) % 360.0f - 180.0f;

		final float fixedDeltaYaw = Math.round(deltaYaw / gcdPatch) * gcdPatch;
		final float fixedDeltaPitch = Math.round((rots[1] - fixedLastPitch) / gcdPatch) * gcdPatch;

		if (useHitbox.value) {
			double dist = RotationUtil.rayCastToBoundingBox(
					mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ,
					box.minX, box.maxX,
					box.minY, box.maxY,
					box.minZ, box.maxZ,
					fixedLastYaw + fixedDeltaYaw, fixedLastPitch + fixedDeltaPitch
			);

			if (dist < 0 || dist > attackDist.value)
				return;
		}

		yaw = fixedLastYaw + fixedDeltaYaw;
		pitch = fixedLastPitch + fixedDeltaPitch;

		lastYaw = fixedLastYaw + fixedDeltaYaw;
		lastPitch = fixedLastPitch + fixedDeltaPitch;

		if (!canAttack)
			return;

		if (swing.value)
			mc.thePlayer.swingItem();

		mc.playerController.attackEntity(mc.thePlayer, target);
		lastAttack = currTime;
	}

	// TODO: Fazer o método jump usar o yaw das rots.
	@EventTarget
	public void onMotion(MotionEvent event) {
		if (event.getType() != EnumEventType.PRE)
			return;

		if (target == null || Float.isNaN(yaw) || Float.isNaN(pitch))
			return;

		event.yaw = yaw;
		event.pitch = pitch;
	}

	@EventTarget
	public void onStrafe(StrafeEvent event) {
		if (target == null || Float.isNaN(yaw))
			return;

		event.yaw = yaw;
	}
}
