package mrfast.sbf.features.overlays.menuOverlay;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.SkyblockInfo;
import mrfast.sbf.events.SkyblockMobEvent;
import mrfast.sbf.utils.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PestHighlighter {
    @SubscribeEvent
    public void onRender(SkyblockMobEvent.Render event) {
        if (!SkyblockInfo.map.equals("Garden") || !SkyblockFeatures.config.highlightPests || event.getSbMob().getSkyblockMobId()==null) return;

        if(event.getSbMob().getSkyblockMobId().endsWith("Pest")) {
            highlightPest(event.getSbMob().skyblockMob,event.partialTicks);
        }
    }

    private void highlightPest(Entity armorStand, float partialTicks) {
        AxisAlignedBB aabb = new AxisAlignedBB(armorStand.posX - 0.5, armorStand.posY, armorStand.posZ - 0.5, armorStand.posX + 0.5, armorStand.posY + 1, armorStand.posZ + 0.5);

        if(SkyblockFeatures.config.highlightPestThroughWalls) GlStateManager.disableDepth();
        RenderUtil.drawOutlinedFilledBoundingBox(aabb, SkyblockFeatures.config.highlightPestColor, partialTicks);
        if(SkyblockFeatures.config.highlightPestThroughWalls)  GlStateManager.enableDepth();
    }
}
