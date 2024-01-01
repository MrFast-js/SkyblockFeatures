package mrfast.sbf.features.render;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.events.SkyblockMobEvent;
import mrfast.sbf.utils.RenderUtil;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ButterflyHighlight {
    @SubscribeEvent
    public void onRenderWorld(SkyblockMobEvent.Render event) {
        if(event.getSbMob().skyblockMobId.equals("Butterfly") && SkyblockFeatures.config.highlightButterfly ) {
            Entity armorstand = event.getSbMob().skyblockMob;
            AxisAlignedBB aabb = new AxisAlignedBB(armorstand.posX - 0.5, armorstand.posY, armorstand.posZ - 0.5, armorstand.posX + 0.5, armorstand.posY + 1, armorstand.posZ + 0.5);

            RenderUtil.drawOutlinedFilledBoundingBox(aabb,SkyblockFeatures.config.highlightButterflyColor,event.partialTicks);
        }
    }
}
