package mrfast.sbf.features.dungeons.solvers;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.utils.RenderUtil;
import mrfast.sbf.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
/**
 * Modified from SkyblockMod under GNU Lesser General Public License v3.0
 * https://github.com/bowser0000/SkyblockMod/blob/master/COPYING
 * @author Bowser0000
 */
public class CreeperSolver {
    static final int[] CREEPER_COLORS = {0xe6194B, 0xf58231, 0xffe119, 0x3cb44b, 0x42d4f4, 0x4363d8, 0x911eb4, 0xf032e6, 0x000075, 0xaaffc3};
    static boolean drawCreeperLines = false;
    static Vec3 creeperLocation = new Vec3(0, 0, 0);
    static List<Vec3[]> creeperLines = new ArrayList<>();

    @SubscribeEvent
    public void onTick(ClientTickEvent event) {
        if(!SkyblockFeatures.config.creeperSolver || !Utils.inDungeons || Utils.GetMC().thePlayer==null) return;
        EntityPlayer p = Utils.GetMC().thePlayer;
        AxisAlignedBB radiusCube = new AxisAlignedBB(p.posX-15, p.posY-10,p.posZ-15,p.posX+15,p.posY+10,p.posZ+15);
        List<EntityCreeper> nearbyCreepers = Utils.GetMC().theWorld.getEntitiesWithinAABB(EntityCreeper.class, radiusCube);

        if(!nearbyCreepers.isEmpty() && !nearbyCreepers.get(0).isInvisible()) {
            EntityCreeper entity = nearbyCreepers.get(0);
            creeperLines.clear();
            if (!drawCreeperLines) creeperLocation = new Vec3(entity.posX, entity.posY + 1, entity.posZ);
            drawCreeperLines = true;
            BlockPos block1 = new BlockPos(entity.posX - 14, entity.posY - 7, entity.posZ - 13);
            BlockPos block2 = new BlockPos(entity.posX + 14, entity.posY + 10, entity.posZ + 13);
            Iterable<BlockPos> blocks = BlockPos.getAllInBox(block1, block2);

            for (BlockPos blockPos : blocks) {
                Block block = Utils.GetMC().theWorld.getBlockState(blockPos).getBlock();
                if (block == Blocks.sea_lantern || block == Blocks.prismarine) {
                    // Connect block to nearest block on opposite side
                    Vec3 startBlock = new Vec3(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
                    BlockPos oppositeBlock = getFirstBlockPosAfterVectors(startBlock, creeperLocation);
                    BlockPos endBlock = getNearbyBlock(oppositeBlock, Blocks.sea_lantern, Blocks.prismarine);
                    if (endBlock != null && startBlock.yCoord > 68 && endBlock.getY() > 68) { // Don't create line underground
                        // Add to list for drawing
                        Vec3[] insertArray = {startBlock, new Vec3(endBlock.getX() + 0.5, endBlock.getY() + 0.5, endBlock.getZ() + 0.5)};
                        creeperLines.add(insertArray);
                    }
                }
            }
        } else {
            drawCreeperLines = false;
        }
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        if (SkyblockFeatures.config.creeperSolver && !creeperLines.isEmpty() && drawCreeperLines) {
            for (int i = 0; i < creeperLines.size(); i++) {
                Vec3 pos1 = creeperLines.get(i)[0];
                Vec3 pos2 = creeperLines.get(i)[1];
                int color = CREEPER_COLORS[i%10];
                RenderUtil.drawOutlinedFilledBoundingBox(new AxisAlignedBB(pos1.xCoord - 0.51, pos1.yCoord - 0.51, pos1.zCoord - 0.51, pos1.xCoord + 0.51, pos1.yCoord + 0.51, pos1.zCoord + 0.51), new Color(color), event.partialTicks);
                RenderUtil.drawOutlinedFilledBoundingBox(new AxisAlignedBB(pos2.xCoord - 0.51, pos2.yCoord - 0.51, pos2.zCoord - 0.51, pos2.xCoord + 0.51, pos2.yCoord + 0.51, pos2.zCoord + 0.51), new Color(color), event.partialTicks);
            }
        }
    }

    public static BlockPos getFirstBlockPosAfterVectors(Vec3 pos1, Vec3 pos2) {
		double x = pos2.xCoord - pos1.xCoord;
		double y = pos2.yCoord - pos1.yCoord;
		double z = pos2.zCoord - pos1.zCoord;
		
		for (int i = 10; i < 20 * 10; i++) { // Start at least 1 strength away
			double newX = pos1.xCoord + ((x / 10) * i);
			double newY = pos1.yCoord + ((y / 10) * i);
			double newZ = pos1.zCoord + ((z / 10) * i);
			
			BlockPos newBlock = new BlockPos(newX, newY, newZ);
			if (Utils.GetMC().theWorld.getBlockState(newBlock).getBlock() != Blocks.air) {
				return newBlock;
			}
		}
		
		return null;
	}
	
	public static BlockPos getNearbyBlock(BlockPos pos, Block... blockTypes) {
		if (pos == null) return null;
		BlockPos pos1 = new BlockPos(pos.getX() - 2, pos.getY() - 3, pos.getZ() - 2);
		BlockPos pos2 = new BlockPos(pos.getX() + 2, pos.getY() + 3, pos.getZ() + 2);
		
		BlockPos closestBlock = null;
		double closestBlockDistance = 99;
		Iterable<BlockPos> blocks = BlockPos.getAllInBox(pos1, pos2);
		
		for (BlockPos block : blocks) {
			for (Block blockType : blockTypes) {
				if (Utils.GetMC().theWorld.getBlockState(block).getBlock() == blockType && block.distanceSq(pos) < closestBlockDistance) {
					closestBlock = block;
					closestBlockDistance = block.distanceSq(pos);
				}
			}
		}
		
		return closestBlock;
	}
}
