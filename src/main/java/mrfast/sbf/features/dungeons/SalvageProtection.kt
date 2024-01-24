package mrfast.sbf.features.dungeons

import com.mojang.realmsclient.gui.ChatFormatting
import mrfast.sbf.SkyblockFeatures
import mrfast.sbf.events.SlotClickedEvent
import mrfast.sbf.utils.ItemUtils
import mrfast.sbf.utils.Utils
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class SalvageProtection {
    @SubscribeEvent
    fun onSlotClick(event: SlotClickedEvent) {
        if (event.chestName.startsWith("Salvage Items") && event.slot != null && event.slot != null && SkyblockFeatures.config.salvageProtection) {
            val itemWorth = ItemUtils.getEstimatedItemValue(event.item)
            if (itemWorth > SkyblockFeatures.config.salvageProtectionMinValue) {
                event.setCanceled(true)
                Utils.playSound("mob.villager.no", 0.1)
                Utils.sendMessage(ChatFormatting.RED.toString() + "Blocked salvaging item with value greater than threshold")
            }
        }
    }
}
