package biggie.module.modules.combat;

import biggie.event.client.GameLoopEvent;
import biggie.event.motion.MotionEvent;
import biggie.event.render.Render2DEvent;
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
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.Sys;
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

	private float yaw = Float.NaN;
	private float pitch = Float.NaN;

	final List<EntityLivingBase> targetList = new ArrayList<>();
	private EntityLivingBase target = null;

	public KillAura() {
		super("KillAura", ModuleCategory.COMBAT, Keyboard.KEY_R);
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
	}

	@EventTarget
	public void onRender3D(Render3DEvent event) {
		if (target == null || !drawBox.value)
			return;

		final AxisAlignedBB box = target.getEntityBoundingBox();
		final AxisAlignedBB lastBox = RenderUtil.getLastTickBoundingBox(target);

		RenderUtil.drawBoundingBox(
				box.minX, box.minY, box.minZ,
				box.maxX, box.maxY, box.maxZ,
				lastBox.minX, lastBox.minY, lastBox.minZ,
				lastBox.maxX, lastBox.maxY, lastBox.maxZ,
				false, false,
				0, 255, 0, 63
		);
	}

	@EventTarget
	public void onMotion(MotionEvent event) {
		if (event.getType() != EnumEventType.PRE)
			return;

		if (!Float.isNaN(yaw) && !Float.isNaN(pitch)) {
			event.yaw = this.yaw;
			event.pitch = this.pitch;

			this.yaw = Float.NaN;
			this.pitch = Float.NaN;
		}

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
			target = null;
			return;
		}

		if ((currTime - lastSwitch) > switchDelay.value) {
			++targetIndex;
			lastSwitch = currTime;
		}

		if (targetIndex >= targetList.size()) {
			targetIndex = 0;
		}

		if (rotMode.value.equals("Single")) {
			target = targetList.get(0);
		} else if (rotMode.value.equals("Switch")) {
			target = targetList.get(targetIndex);
		}

		targetList.clear();
	}

	// WARNING: Nós poderiamos só fazer tudo isso no motion event, porém
	// o cps maximo só poderia ser 20 pra baixo.
	@EventTarget
	public void onGameLoop(GameLoopEvent event) {
		if (mc.theWorld == null || mc.thePlayer == null)
			return;

		final long currTime = System.currentTimeMillis();

		final boolean canAttack = (currTime - lastAttack) > (1000.0 / aps.value);

		if (target == null)
			return;

		final float[] rots = RotationUtil.getRotationTo(mc.thePlayer, target.posX, target.posY + (target.getEyeHeight() * 0.5), target.posZ);
		final AxisAlignedBB box = target.getEntityBoundingBox();

		if (box == null)
			return;

		if (useHitbox.value) {
			double dist = RotationUtil.rayCastToBoundingBox(
					mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ,
					box.minX, box.maxX,
					box.minY, box.maxY,
					box.minZ, box.maxZ,
					rots[0], rots[1]
			);

			if (dist < 0 || dist > attackDist.value)
				return;
		}

		yaw = rots[0];
		pitch = rots[1];

		if (!canAttack)
			return;

		if (swing.value)
			mc.thePlayer.swingItem();

		mc.getNetHandler().addToSendQueue(new C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK));
		lastAttack = currTime;
	}
}
