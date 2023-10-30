package mrfast.sbf.features.render;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.SkyblockInfo;
import mrfast.sbf.utils.RenderUtil;
import mrfast.sbf.utils.Utils;
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
        if(!SkyblockFeatures.config.highlightBeacons || !SkyblockInfo.getInstance().map.equals("The End")) return;

        for(TileEntity e:Utils.GetMC().theWorld.loadedTileEntityList) {
            if(e instanceof TileEntityBeacon) {
                BlockPos p = e.getPos();
                AxisAlignedBB aabb = new AxisAlignedBB(p.getX(), p.getY(), p.getZ(), p.getX()+1, p.getY()+1, p.getZ()+1);
                if(SkyblockFeatures.config.highlightBeaconsThroughWalls) GlStateManager.disableDepth();
                RenderUtil.drawOutlinedFilledBoundingBox(aabb, SkyblockFeatures.config.highlightBeaconsColor, event.partialTicks);
                GlStateManager.enableDepth();
            }
        }
    }
}
