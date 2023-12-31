package mrfast.sbf.features.dungeons;

import com.mojang.realmsclient.gui.ChatFormatting;
import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.events.SlotClickedEvent;
import mrfast.sbf.utils.ItemUtils;
import mrfast.sbf.utils.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SalvageProtection {
    @SubscribeEvent
    public void onSlotClick(SlotClickedEvent event) {
        if(event.chestName.startsWith("Salvage Items") && event.slot!=null && SkyblockFeatures.config.salvageProtection) {
            try {
                long itemWorth = ItemUtils.getEstimatedItemValue(event.item);
                if (itemWorth > SkyblockFeatures.config.salvageProtectionMinValue) {
                    event.setCanceled(true);
                    Utils.playSound("mob.villager.no", 0.1f);
                    Utils.sendMessage(ChatFormatting.RED + "Blocked salvaging item with value greater than threshold");
                }
            } catch (Exception e) {

            }
        }
    }
}
