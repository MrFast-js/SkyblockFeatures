package mrfast.skyblockfeatures.features.render;

import mrfast.skyblockfeatures.SkyblockFeatures;
import mrfast.skyblockfeatures.core.SkyblockInfo;
import mrfast.skyblockfeatures.utils.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class DynamicFullbright {
    @SubscribeEvent
    public void onTick(ClientTickEvent event) {
        if(SkyblockFeatures.config.DynamicFullbright) {
            String loc = SkyblockInfo.getInstance().getMap();
            try {
                if(loc.equals("Dynamic") || loc.equals("Crystal Hollows") || Utils.inDungeons) {
                    Utils.GetMC().gameSettings.gammaSetting=(SkyblockFeatures.config.DynamicFullbrightDisabled/10);
                } else {
                    Utils.GetMC().gameSettings.gammaSetting=SkyblockFeatures.config.DynamicFullbrightElsewhere/10;
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
        if(SkyblockFeatures.config.fullbright) {
            Utils.GetMC().gameSettings.gammaSetting=100;
        }
    }
}
