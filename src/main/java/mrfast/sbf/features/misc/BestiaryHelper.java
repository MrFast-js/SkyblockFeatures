package mrfast.sbf.features.misc;

import mrfast.sbf.core.SkyblockInfo;
import mrfast.sbf.core.SkyblockMobDetector;
import mrfast.sbf.events.RenderEntityOutlineEvent;
import mrfast.sbf.utils.Utils;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

import static mrfast.sbf.SkyblockFeatures.config;


public class BestiaryHelper {

    @SubscribeEvent
    public void onRenderEntityOutlines(RenderEntityOutlineEvent event) {
        if(Utils.GetMC().theWorld == null || Utils.GetMC().thePlayer == null || SkyblockInfo.getLocation()==null || !config.bestiaryHelper) return;
        if (event.type == RenderEntityOutlineEvent.Type.XRAY) return;

        for (Entity entity : Utils.GetMC().theWorld.loadedEntityList) {
            SkyblockMobDetector.SkyblockMob sbMob = SkyblockMobDetector.getSkyblockMob(entity);
            if (sbMob == null) continue;

            if (sbMob.skyblockMob == entity && sbMob.getSkyblockMobId() != null) {
                if (!config.highlightMobs.isEmpty()) {
                    if (sbMob.mobNameEntity.getCustomNameTag().contains(config.highlightMobs)) {
                        event.queueEntityToOutline(sbMob.skyblockMob, Color.green);
                    }
                }
            }
        }
    }
}
