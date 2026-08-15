package biggie.module.modules.misc;

import biggie.event.client.TickEvent;
import biggie.event.input.PostPlayerInputEvent;
import biggie.event.motion.JumpEvent;
import biggie.event.motion.LivingUpdateEvent;
import biggie.event.motion.MotionEvent;
import biggie.event.motion.StrafeEvent;
import biggie.module.Module;
import biggie.module.ModuleCategory;
import biggie.setting.settings.BooleanSetting;
import biggie.setting.settings.DoubleSetting;
import biggie.setting.settings.ListSetting;
import biggie.util.player.RotationUtil;
import biggie.util.render.ServerRotation;
import net.lenni0451.asmevents.event.EventTarget;
import net.lenni0451.asmevents.event.enums.EnumEventType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSand;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import org.lwjgl.input.Keyboard;

import java.util.*;

public class Scaffold extends Module {
    private final static double MIN_RANDOM_EPS = 0.0006;

    private final ListSetting searchMode = new ListSetting("Rotation", "Center", "Center", "Random");
    public static final ListSetting sprintMode = new ListSetting("Sprint", "None", "None", "Sprint", "Keep-Y");

    private final BooleanSetting moveFix = new BooleanSetting("Movement Fix", true);

    private float yaw = Float.NaN;
    private float pitch = Float.NaN;

    private float lastYaw = Float.NaN;
    private float lastPitch = Float.NaN;

    private double keepY = Double.NaN;

    private int keepTicks = 0;
    private boolean keepRot = false;

    private static final EnumFacing[] BLOCK_OFFSETS = {
            EnumFacing.NORTH,
            EnumFacing.EAST,
            EnumFacing.SOUTH,
            EnumFacing.WEST,
            EnumFacing.DOWN
    };

    public Scaffold() { super("Scaffold", ModuleCategory.MISC, Keyboard.KEY_NONE); }

    @Override
    public void onDisable() {
        final float gcd = (float) Math.pow(mc.gameSettings.mouseSensitivity * 0.6f + 0.2f, 3f) * 1.2f;

        if (!Float.isNaN(yaw)) {
            final float deltaYaw = mc.thePlayer.rotationYaw - yaw;
            mc.thePlayer.rotationYaw = yaw + MathHelper.wrapAngleTo180_float(Math.round(deltaYaw / gcd) * gcd);
        }

        yaw = Float.NaN;
        pitch = Float.NaN;
        lastYaw = Float.NaN;
        lastPitch = Float.NaN;
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null)
            return;

        if (event.getType() != EnumEventType.PRE)
            return;

        final float gcd = (float) Math.pow(mc.gameSettings.mouseSensitivity * 0.6f + 0.2f, 3f) * 1.2f;

        if (!sprintMode.value.equals("Keep-Y") || mc.gameSettings.keyBindJump.isKeyDown() || mc.thePlayer.posY - 1 < keepY) {
            keepY = Double.NaN;
            keepRot = false;
            keepTicks = 0;
        } else if (mc.thePlayer.onGround && sprintMode.value.equals("Keep-Y")) {
            keepY = Math.floor(mc.thePlayer.posY) - 1;

            if (mc.thePlayer.movementInput.moveForward != 0 || mc.thePlayer.movementInput.moveStrafe != 0) {
                final float patchedDeltaYaw = Math.round(MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - yaw) / gcd) * gcd;

                yaw = yaw + patchedDeltaYaw;
                lastYaw = yaw + patchedDeltaYaw;
                keepRot = true;
            }
        }

        if (keepTicks == 1 && mc.thePlayer.onGround)
            mc.thePlayer.jump();

        if (keepTicks >= 2) {
            keepTicks = 0;
            keepRot = false;
        }

        if (sprintMode.value.equals("Keep-Y") && keepRot) {
            ++keepTicks;
            return;
        }

        final Queue<BlockPos> blockQueue = new ArrayDeque<>();
        final Set<Long> visitedList = new HashSet<>();

        final BlockPos startBlock = new BlockPos(
                mc.thePlayer.posX, Double.isNaN(keepY) ? Math.floor(mc.thePlayer.posY) - 1 : keepY, mc.thePlayer.posZ
        );
        final long startBlockLong = startBlock.toLong();

        blockQueue.add(startBlock);
        visitedList.add(startBlock.toLong());

        final double rangeSq = (mc.playerController.getBlockReachDistance() * mc.playerController.getBlockReachDistance());

        BlockData targetBlock = null;

        while (!blockQueue.isEmpty()) {
            final BlockPos pos = blockQueue.poll();

            for (int i = 0; i < 5; ++i) {
                final EnumFacing facing = BLOCK_OFFSETS[i];
                final BlockPos neighbor = pos.add(facing.getDirectionVec());
                final long neighborLong = neighbor.toLong();

                if (neighborLong == startBlockLong)
                    continue;

                if (visitedList.contains(neighborLong))
                    continue;

                visitedList.add(neighborLong);

                if (mc.thePlayer.getDistanceSq(neighbor) > rangeSq)
                    continue;

                if (mc.theWorld.getBlockState(neighbor).getBlock().getMaterial().isSolid()) {
                    final Vec3 relPos = getRelPoint(facing.getOpposite(), searchMode.value.equals("Random"));
                    targetBlock = new BlockData(neighbor, facing.getOpposite(), neighbor, relPos);
                    break;
                }

                blockQueue.add(neighbor);
            }

            if (targetBlock != null)
                break;
        }

        if (targetBlock == null) {
            if (!Float.isNaN(yaw)) {
                final float deltaYaw = mc.thePlayer.rotationYaw - yaw;
                mc.thePlayer.rotationYaw = yaw + MathHelper.wrapAngleTo180_float(Math.round(deltaYaw / gcd) * gcd);
            }

            yaw = Float.NaN;
            pitch = Float.NaN;
            lastYaw = Float.NaN;
            lastPitch = Float.NaN;
            return;
        }

        final double rotX = targetBlock.hitPos.getX() + targetBlock.relPos.xCoord;
        final double rotY = targetBlock.hitPos.getY() + targetBlock.relPos.yCoord;
        final double rotZ = targetBlock.hitPos.getZ() + targetBlock.relPos.zCoord;

        final float[] rots = RotationUtil.getRotationTo(mc.thePlayer, rotX, rotY, rotZ);

        final double dX = rotX - mc.thePlayer.posX;
        final double dY = rotY - mc.thePlayer.posY - mc.thePlayer.getEyeHeight();
        final double dZ = rotZ - mc.thePlayer.posZ;

        if (dX * dX + dY * dY + dZ * dZ > rangeSq) {
            if (!Float.isNaN(yaw)) {
                final float deltaYaw = mc.thePlayer.rotationYaw - yaw;
                mc.thePlayer.rotationYaw = yaw + MathHelper.wrapAngleTo180_float(Math.round(deltaYaw / gcd) * gcd);
            }

            yaw = Float.NaN;
            pitch = Float.NaN;
            lastYaw = Float.NaN;
            lastPitch = Float.NaN;
            return;
        }

        final float fixedLastYaw = (Float.isNaN(lastYaw)) ? mc.thePlayer.rotationYaw : lastYaw;
        final float fixedLastPitch = (Float.isNaN(lastPitch)) ? mc.thePlayer.rotationPitch : lastPitch;

        final float deltaYaw = rots[0] - fixedLastYaw;

        final float fixedDeltaYaw = MathHelper.wrapAngleTo180_float(Math.round(deltaYaw / gcd) * gcd);
        final float fixedDeltaPitch = Math.round((rots[1] - fixedLastPitch) / gcd) * gcd;

        lastYaw = fixedLastYaw + fixedDeltaYaw;
        lastPitch = fixedLastPitch + fixedDeltaPitch;
        yaw = fixedLastYaw + fixedDeltaYaw;
        pitch = fixedLastPitch + fixedDeltaPitch;

        for (int slot = 0; slot < 8; ++slot) {
            final ItemStack item = mc.thePlayer.inventory.mainInventory[slot];

            if (item == null)
                continue;

            if (item.getItem() == null)
                continue;

            if (!(item.getItem() instanceof ItemBlock))
                continue;

            final Block itemBlock = BlockSand.getBlockFromItem(item.getItem());

            if (itemBlock == Blocks.gravel || itemBlock == Blocks.sand   || itemBlock == Blocks.tnt)
                continue;

            mc.thePlayer.inventory.currentItem = slot;
        }

        if (mc.playerController.onPlayerRightClick(
                mc.thePlayer, mc.theWorld,
                mc.thePlayer.getHeldItem(),
                targetBlock.hitPos, targetBlock.facing,
                new Vec3(targetBlock.relPos.xCoord + targetBlock.hitPos.getX(), targetBlock.relPos.yCoord  + targetBlock.hitPos.getY(), targetBlock.relPos.zCoord  + targetBlock.hitPos.getZ())
        )) {
            mc.thePlayer.swingItem();
        }
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (event.getType() != EnumEventType.PRE)
            return;

        if (!Float.isNaN(yaw))
            event.yaw = yaw;

        if (!Float.isNaN(pitch))
            event.pitch = pitch;
    }

    @EventTarget
    public void onJump(JumpEvent event) {
        if (Float.isNaN(yaw)  || !moveFix.value)
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

    Vec3 getRelPoint(EnumFacing facing, boolean randomize) {
        if (facing == null)
            return new Vec3(0.5, 0.5, 0.5);

        final double minRandom = randomize ? Math.random() * MIN_RANDOM_EPS : 0;

        final double centerRandom1 = randomize ? 0.5 + (-MIN_RANDOM_EPS + Math.random() * (MIN_RANDOM_EPS * 2)) : 0.5;
        final double centerRandom2 = randomize ? 0.5 + (-MIN_RANDOM_EPS + Math.random() * (MIN_RANDOM_EPS * 2)) : 0.5;

        switch (facing) {
            case WEST:
                return new Vec3(minRandom, centerRandom1, centerRandom2);
            case EAST:
                return new Vec3(1 - minRandom, centerRandom1, centerRandom2);
            case NORTH:
                return new Vec3(centerRandom1, centerRandom2, minRandom);
            case SOUTH:
                return new Vec3(centerRandom1, centerRandom2, 1 - minRandom);
            case UP:
                return new Vec3(centerRandom1, 1 - minRandom, centerRandom2);
            case DOWN:
                return new Vec3(centerRandom1, minRandom, centerRandom2);
            default:
                return new Vec3(0.5, 0.5, 0.5);
        }
    }

    static class BlockData {
        public final BlockPos pos;
        public final EnumFacing facing;
        public final BlockPos hitPos;
        public final Vec3 relPos;

        public BlockData(BlockPos pos, EnumFacing facing, BlockPos hitPos, Vec3 relPos) {
            this.pos = pos;
            this.facing = facing;
            this.hitPos = hitPos;
            this.relPos = relPos;
        }
    }
}
