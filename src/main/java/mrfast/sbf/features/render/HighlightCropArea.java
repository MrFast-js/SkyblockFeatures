package mrfast.sbf.features.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.SkyblockInfo;
import mrfast.sbf.events.BlockChangeEvent;
import mrfast.sbf.events.SecondPassedEvent;
import mrfast.sbf.utils.RenderUtil;
import mrfast.sbf.utils.ScoreboardUtil;
import mrfast.sbf.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HighlightCropArea {
    List<AxisAlignedBB> cropAreas = new ArrayList<>();
    List<BlockPos> blocksToDestroy = new ArrayList<>();
    boolean update = true;
    int seconds = 0;
    @SubscribeEvent
    public void secondPassed(SecondPassedEvent event) {
        if(SkyblockInfo.getInstance()==null || !SkyblockFeatures.config.GardenBlocksToRemove || Utils.GetMC().theWorld==null) return;
        try {if(!SkyblockInfo.getInstance().map.equals("Garden")) return;} catch (Exception e) {}
    
        for(String line:ScoreboardUtil.getSidebarLines()) {
            if(line.contains("Cleanup") && blocksToDestroy.size()==0) {
                update = true;
            }
        }
        seconds++;
        if(seconds>10) {
            seconds=0;
            blocksToDestroy.clear();
        }
    }

    @SubscribeEvent
    public void worldChange(WorldEvent.Load event) {
        if(!SkyblockFeatures.config.GardenBlocksToRemove) return;
        update = false;
        cropAreas.clear();
        blocksToDestroy.clear();
    }
    
    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        if(SkyblockInfo.getInstance()==null || !SkyblockFeatures.config.GardenBlocksToRemove || Utils.GetMC().theWorld==null) return;
        try {if(!SkyblockInfo.getInstance().map.equals("Garden")) return;} catch (Exception e) {}
        
        if(cropAreas.size()==0) {
            for(int x=-192;x<192;x+=96) {
                for(int z=-192;z<192;z+=96) {
                    AxisAlignedBB box = new AxisAlignedBB(x-48,65,z-48,x+48,116,z+48);
                    cropAreas.add(box);
                }
            }
        }
        if(update) {
            update = false;
            blocksToDestroy.clear();
            for(AxisAlignedBB cropArea:cropAreas) {
                if(cropArea.isVecInside(Utils.GetMC().thePlayer.getPositionVector())) {
                    for(double x=cropArea.minX;x<cropArea.maxX;x++) {
                        for(double z=cropArea.minZ;z<cropArea.maxZ;z++) {
                            for(double y=71;y<cropArea.maxY;y++) {
                                BlockPos pos = new BlockPos(x, y, z);
                                Block block = Utils.GetMC().theWorld.getBlockState(pos).getBlock();
                                if(block!=Blocks.air && block!=Blocks.grass && block!=Blocks.dirt) {
                                    if(!blocksToDestroy.contains(pos)) {
                                        blocksToDestroy.add(pos);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        for(BlockPos pos:blocksToDestroy) {
            if(Utils.GetMC().thePlayer.getDistanceSq(pos)>200) continue;
            RenderUtil.drawBoundingBox(new AxisAlignedBB(pos, pos.add(1, 1, 1)), Color.red, event.partialTicks);
        }
        
    }

    @SubscribeEvent
    public void onBlockChange(BlockChangeEvent event) {
        if(SkyblockInfo.getInstance()==null || !SkyblockFeatures.config.GardenBlocksToRemove || Utils.GetMC().theWorld==null) return;
        try {if(!SkyblockInfo.getInstance().map.equals("Garden")) return;} catch (Exception e) {}

        if(blocksToDestroy.contains(event.pos)) {
            blocksToDestroy.remove(blocksToDestroy.indexOf(event.pos));
        }
    }
}
