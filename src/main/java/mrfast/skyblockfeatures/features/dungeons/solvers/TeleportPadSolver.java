package mrfast.skyblockfeatures.features.dungeons.solvers;

import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import mrfast.skyblockfeatures.SkyblockFeatures;
import mrfast.skyblockfeatures.utils.RenderUtil;
import mrfast.skyblockfeatures.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEndPortalFrame;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

public class TeleportPadSolver {
    List<BlockPos> endportalFrames = new ArrayList<>();
    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        endportalFrames.clear();
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent event) {
        Minecraft mc = Utils.GetMC();
        if(Utils.GetMC().theWorld == null || !Utils.inDungeons || !SkyblockFeatures.config.teleportPadSolver) return;

        int[][] directionOffsets = {
            {-1, 1},   // North-West
            {0, 1},    // North
            {1, 1},    // North-East
            {-1, 0},   // West
            {0, 0},     // No Move
            {1, 0},    // East
            {-1, -1},  // South-West
            {0, -1},   // South
            {1, -1}    // South-East
        };

        BlockPos playerPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        
        for (int[] offset : directionOffsets) {
            int xOffset = offset[0];
            int zOffset = offset[1];

            BlockPos blockPos = playerPos.add(xOffset, 0, zOffset);
            Block block = Utils.GetMC().theWorld.getBlockState(blockPos).getBlock();
            if(block instanceof BlockEndPortalFrame && !endportalFrames.contains(playerPos)) {
                endportalFrames.add(playerPos.add(-xOffset, 0, -zOffset));
            }
        }

    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        if(Utils.GetMC().theWorld == null || !Utils.inDungeons || !SkyblockFeatures.config.teleportPadSolver) return;

        for(BlockPos frame:endportalFrames) {
            AxisAlignedBB aabb = new AxisAlignedBB(frame, frame.add(1, 1, 1));
            RenderUtil.drawOutlinedFilledBoundingBox(aabb, Color.green, event.partialTicks);
        }
    }
}
