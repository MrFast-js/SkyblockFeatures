package mrfast.sbf.features.items;

import com.mojang.realmsclient.gui.ChatFormatting;
import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.events.UseItemAbilityEvent;
import mrfast.sbf.gui.GuiManager;
import mrfast.sbf.utils.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class FireVeilTimer {
    double time = 0;
    boolean startCounting = false;
    @SubscribeEvent
    public void onItemAbilityUse(UseItemAbilityEvent event) {
        if(!SkyblockFeatures.config.fireVeilTimer) return;
        if(event.ability.itemId.equals("FIRE_VEIL_WAND")) {
            startCounting = true;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if(!SkyblockFeatures.config.fireVeilTimer || event.phase != TickEvent.Phase.START) return;

        if(startCounting) {
            time+=0.05;
            if (time<=5d) {
                GuiManager.createTitle(ChatFormatting.RED+Utils.round(5d-time,1)+"s",1,false);
            } else {
                startCounting = false;
                time = 0;
            }
        }
    }


}
