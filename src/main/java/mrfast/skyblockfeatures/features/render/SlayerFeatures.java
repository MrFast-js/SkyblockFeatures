package mrfast.skyblockfeatures.features.render;

import java.awt.Color;

import mrfast.skyblockfeatures.SkyblockFeatures;
import mrfast.skyblockfeatures.utils.RenderUtil;
import mrfast.skyblockfeatures.core.SkyblockInfo;
import mrfast.skyblockfeatures.utils.Utils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SlayerFeatures {
    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if(Utils.GetMC().theWorld == null || Utils.GetMC().thePlayer == null || SkyblockInfo.getInstance().getLocation()==null) return;
        if(!SkyblockFeatures.config.highlightBeacons && !SkyblockInfo.getInstance().getLocation().equals("combat_3")) return;

        for(TileEntity e:Utils.GetMC().theWorld.loadedTileEntityList) {
            if(e instanceof TileEntityBeacon) {
                BlockPos p = e.getPos();
                AxisAlignedBB aabb = new AxisAlignedBB(p.getX(), p.getY(), p.getZ(), p.getX()+1, p.getY()+1, p.getZ()+1);
                if(SkyblockFeatures.config.highlightBeaconsThroughWalls) GlStateManager.disableDepth();
                RenderUtil.drawOutlinedFilledBoundingBox(aabb, Color.red, event.partialTicks);
                GlStateManager.enableDepth();
            }
        }
    }
}
