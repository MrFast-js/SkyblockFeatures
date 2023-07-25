package mrfast.skyblockfeatures.features.mining;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import mrfast.skyblockfeatures.SkyblockFeatures;
import mrfast.skyblockfeatures.events.BlockChangeEvent;
import mrfast.skyblockfeatures.utils.RenderUtil;
import mrfast.skyblockfeatures.utils.Utils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HighlightCobblestone {
    List<BlockPos> cobblestonePostitions = new ArrayList<BlockPos>();
    
    @SubscribeEvent
    public void onBlockPlace(BlockChangeEvent event) {
        if(!SkyblockFeatures.config.highlightCobblestone) return;
        if(event.getNew().getBlock() == Blocks.cobblestone && !cobblestonePostitions.contains(event.pos) && Utils.GetMC().thePlayer.getDistance(event.pos.getX(), event.pos.getY(), event.pos.getZ())<10) {
            if(Utils.GetMC().thePlayer.getHeldItem()!=null) {
                if(Utils.GetMC().thePlayer.getHeldItem().getDisplayName().contains("Cobblestone")) {
                    cobblestonePostitions.add(event.pos);
                }
            }
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        try {
            if(!SkyblockFeatures.config.highlightCobblestone) return;
            cobblestonePostitions.clear();
        } catch(Exception e) {

        }
    }

    @SubscribeEvent
    public void RenderWorldLastEvent(RenderWorldLastEvent event) {
        if(!SkyblockFeatures.config.highlightCobblestone) return;

        try {
            GlStateManager.disableDepth();
            for(BlockPos pos:cobblestonePostitions) {
                if(Utils.GetMC().theWorld.getBlockState(pos).getBlock() != Blocks.cobblestone) {
                    cobblestonePostitions.remove(pos);
                }
                AxisAlignedBB box = new AxisAlignedBB(pos, pos.add(1, 1, 1));
                RenderUtil.drawOutlinedFilledBoundingBox(box, Color.cyan, event.partialTicks);
            }
            GlStateManager.enableDepth();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }
}
