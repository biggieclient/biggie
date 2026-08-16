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
import biggie.util.math.MathUtil;
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
        clearRotation();
    }

    @EventTarget
    public void onTick(TickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null)
            return;

        if (event.getType() != EnumEventType.PRE)
            return;

        if (!sprintMode.value.equals("Keep-Y") || mc.gameSettings.keyBindJump.isKeyDown() || mc.thePlayer.posY - 1 < keepY) {
            keepY = Double.NaN;
            keepRot = false;
            keepTicks = 0;
        } else if (mc.thePlayer.onGround && sprintMode.value.equals("Keep-Y")) {
            keepY = Math.floor(mc.thePlayer.posY) - 1;

            if (mc.thePlayer.movementInput.moveForward != 0 || mc.thePlayer.movementInput.moveStrafe != 0) {
                clearRotation();
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

        final BlockPos startPos = new BlockPos(
                mc.thePlayer.posX, Double.isNaN(keepY) ? Math.floor(mc.thePlayer.posY) - 1 : keepY, mc.thePlayer.posZ
        );

        final double rangeSq = (mc.playerController.getBlockReachDistance() * mc.playerController.getBlockReachDistance());
        final BlockData targetBlock = getBlockData(startPos, rangeSq);

        if (targetBlock == null) {
            clearRotation();
            return;
        }

        final Vec3 placeVec = targetBlock.vecPos.add(targetBlock.relPos);
        final float[] rots = RotationUtil.getRotationTo(mc.thePlayer, placeVec);

        final double dX = placeVec.xCoord - mc.thePlayer.posX;
        final double dY = placeVec.yCoord - mc.thePlayer.posY - mc.thePlayer.getEyeHeight();
        final double dZ = placeVec.zCoord - mc.thePlayer.posZ;

        if (MathUtil.getSqModule(dX, dY, dZ) > rangeSq) {
            clearRotation();
            return;
        }

        final float fixedLastYaw = (Float.isNaN(yaw)) ? mc.thePlayer.rotationYaw : yaw;
        final float fixedLastPitch = (Float.isNaN(pitch)) ? mc.thePlayer.rotationPitch : pitch;

        yaw = RotationUtil.getGCDPatchedYaw(mc, fixedLastYaw, rots[0]);
        pitch = RotationUtil.getGCDPatchedPitch(mc, fixedLastPitch, rots[1]);

        findAndSetBlockSlot();
        placeBlock(mc.thePlayer.getHeldItem(), targetBlock.facing, targetBlock.blockPos, placeVec);
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

    void clearRotation() {
        if (!Float.isNaN(yaw))
            mc.thePlayer.rotationYaw = RotationUtil.getGCDPatchedYaw(mc, yaw, mc.thePlayer.rotationYaw);

        yaw = Float.NaN;
        pitch = Float.NaN;
    }

    BlockData getBlockData(final BlockPos startPos, final double rangeSq) {
        BlockData targetBlock = null;

        final Queue<BlockPos> blockQueue = new ArrayDeque<>();
        final Set<Long> visitedList = new HashSet<>();
        final long startBlockLong = startPos.toLong();

        blockQueue.add(startPos);
        visitedList.add(startBlockLong);

        while (!blockQueue.isEmpty()) {
            final BlockPos pos = blockQueue.poll();

            for (int i = 0; i < 5; ++i) {
                final EnumFacing facing = BLOCK_OFFSETS[i];
                final BlockPos neighbor = pos.add(facing.getDirectionVec());
                final long neighborLong = neighbor.toLong();

                if (visitedList.contains(neighborLong))
                    continue;

                visitedList.add(neighborLong);

                if (mc.thePlayer.getDistanceSq(neighbor) > rangeSq)
                    continue;

                if (mc.theWorld.getBlockState(neighbor).getBlock().getMaterial().isSolid()) {
                    final Vec3 relVec = getRelPoint(facing.getOpposite(), searchMode.value.equals("Random"));
                    targetBlock = new BlockData(new Vec3(neighbor), facing.getOpposite(), neighbor, relVec);
                    break;
                }

                blockQueue.add(neighbor);
            }

            if (targetBlock != null)
                break;
        }

        return targetBlock;
    }

    void findAndSetBlockSlot() {
        for (int slot = 0; slot < 8; ++slot) {
            final ItemStack item = mc.thePlayer.inventory.mainInventory[slot];

            if (item == null)
                continue;

            if (item.getItem() == null)
                continue;

            if (!(item.getItem() instanceof ItemBlock))
                continue;

            final Block itemBlock = BlockSand.getBlockFromItem(item.getItem());

            if (itemBlock == Blocks.gravel || itemBlock == Blocks.sand || itemBlock == Blocks.tnt)
                continue;

            mc.thePlayer.inventory.currentItem = slot;
        }
    }

    void placeBlock(final ItemStack itemStack, final EnumFacing facing, final BlockPos blockPos, final Vec3 placeVec) {
        if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, itemStack, blockPos, facing, placeVec))
            mc.thePlayer.swingItem();
    }

    Vec3 getRelPoint(final EnumFacing facing, final boolean randomize) {
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
        public final Vec3 vecPos;
        public final EnumFacing facing;
        public final BlockPos blockPos;
        public final Vec3 relPos;

        public BlockData(Vec3 vecPos, EnumFacing facing, BlockPos blockPos, Vec3 relPos) {
            this.vecPos = vecPos;
            this.facing = facing;
            this.blockPos = blockPos;
            this.relPos = relPos;
        }
    }
}
