package biggie.module.modules.misc;

import biggie.event.motion.LivingUpdateEvent;
import biggie.event.motion.MotionEvent;
import biggie.module.Module;
import biggie.module.ModuleCategory;
import biggie.setting.settings.DoubleSetting;
import biggie.util.player.RotationUtil;
import net.lenni0451.asmevents.event.EventTarget;
import net.lenni0451.asmevents.event.enums.EnumEventType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSand;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;

import java.util.*;

public class Scaffold extends Module {
    // TODO: Adicionar mais métodos de search.
    //private final ListSetting searchMode = new ListSetting("Center", "Center");
    private final DoubleSetting placeRange = new DoubleSetting("Place Range", 4.5, 3, 8, 0.5);

    private float yaw = Float.NaN;
    private float pitch = Float.NaN;

    private float lastYaw = Float.NaN;
    private float lastPitch = Float.NaN;

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
        lastYaw = Float.NaN;
        lastPitch = Float.NaN;
    }

    @EventTarget
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null)
            return;

        if (event.getType() != EnumEventType.PRE)
            return;

        final Queue<BlockPos> blockQueue = new ArrayDeque<>();
        final Set<Long> visitedList = new HashSet<>();

        final BlockPos startBlock = new BlockPos(
                mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ
        );
        final long startBlockLong = startBlock.toLong();

        blockQueue.add(startBlock);
        visitedList.add(startBlock.toLong());

        final double rangeSq = (placeRange.value * placeRange.value);

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
                    final Vec3 relPos = getRelPoint(facing.getOpposite());
                    targetBlock = new BlockData(neighbor, facing.getOpposite(), neighbor, relPos);
                    break;
                }

                blockQueue.add(neighbor);
            }

            if (targetBlock != null)
                break;
        }

        if (targetBlock == null) {
            yaw = Float.NaN;
            pitch = Float.NaN;
            lastYaw = Float.NaN;
            lastPitch = Float.NaN;
            return;
        }

        final double rotX = targetBlock.hitPos.getX() + targetBlock.relPos.xCoord;
        final double rotY = targetBlock.hitPos.getY() + targetBlock.relPos.yCoord;
        final double rotZ = targetBlock.hitPos.getZ() + targetBlock.relPos.zCoord;

        final float[] rots = RotationUtil.getRotationTo(
                mc.thePlayer,
                rotX, rotY, rotZ
        );

        final float fixedLastYaw = (Float.isNaN(lastYaw)) ? mc.thePlayer.rotationYaw : lastYaw;
        final float fixedLastPitch = (Float.isNaN(lastPitch)) ? mc.thePlayer.rotationPitch : lastPitch;

        final float sensibility = mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
        final float gcdPatch = (sensibility * sensibility * sensibility) * 8.0f;

        final float deltaYaw = (((rots[0] - fixedLastYaw) + 180.0f) % 360.0f + 360.0f) % 360.0f - 180.0f;

        final float fixedDeltaYaw = Math.round(deltaYaw / gcdPatch) * gcdPatch;
        final float fixedDeltaPitch = Math.round((rots[1] - fixedLastPitch) / gcdPatch) * gcdPatch;

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
                targetBlock.relPos
        )) {
            mc.thePlayer.swingItem();
        }
    }

    @EventTarget
    public void onMotion(MotionEvent event) {
        if (event.getType() != EnumEventType.PRE)
            return;

        if (Float.isNaN(yaw) || Float.isNaN(pitch))
            return;

        event.yaw = yaw;
        event.pitch = pitch;
    }

    Vec3 getRelPoint(EnumFacing facing) {
        if (facing == EnumFacing.WEST) {
            return new Vec3(0, 0.5, 0.5);
        } else if (facing == EnumFacing.EAST) {
            return new Vec3(1, 0.5, 0.5);
        } else if (facing == EnumFacing.NORTH) {
            return new Vec3(0.5, 0.5, 1);
        } else if (facing == EnumFacing.SOUTH) {
            return new Vec3(0.5, 0.5, 0);
        } else if (facing == EnumFacing.UP) {
            return new Vec3(0.5, 1, 0.5);
        }

        return null;
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
