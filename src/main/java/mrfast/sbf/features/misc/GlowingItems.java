package mrfast.sbf.features.misc;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.SkyblockInfo;
import mrfast.sbf.events.RenderEntityOutlineEvent;
import mrfast.sbf.utils.ItemRarity;
import mrfast.sbf.utils.ItemUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class GlowingItems {
    @SubscribeEvent
    public void onRenderEntityOutlines(RenderEntityOutlineEvent event) {
        if(Utils.GetMC().theWorld == null || Utils.GetMC().thePlayer == null || SkyblockInfo.getLocation()==null || !SkyblockFeatures.config.glowingItems) return;
        if (event.type == RenderEntityOutlineEvent.Type.NO_XRAY) return;

        for (Entity entity : Utils.GetMC().theWorld.loadedEntityList) {
            if (entity instanceof EntityItem) {
                ItemRarity itemRarity = ItemUtils.getRarity(((EntityItem)entity).getEntityItem());
                event.queueEntityToOutline(entity,itemRarity.getColor());
            }
        }
    }
}
