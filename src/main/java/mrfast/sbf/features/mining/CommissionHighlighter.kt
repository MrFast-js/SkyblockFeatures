package mrfast.sbf.features.mining

import mrfast.sbf.SkyblockFeatures
import mrfast.sbf.events.GuiContainerEvent.DrawSlotEvent
import mrfast.sbf.utils.ItemUtils
import mrfast.sbf.utils.Utils
import net.minecraft.client.gui.Gui
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class CommissionHighlighter {
    @SubscribeEvent
    fun onDrawSlot(event: DrawSlotEvent.Pre) {
        if (!SkyblockFeatures.config.highlightCompletedCommissions || event.slot.stack==null) return

        if (Utils.cleanColor(event.slot.stack.displayName).startsWith("Commission")) {
            for (line in ItemUtils.getItemLore(event.slot.stack)) {
                if (line.contains("COMPLETED")) {
                    Gui.drawRect(event.slot.xDisplayPosition,
                            event.slot.yDisplayPosition,
                            event.slot.xDisplayPosition + 16,
                            event.slot.yDisplayPosition + 16,
                            SkyblockFeatures.config.highlightCompletedCommissionsColor.rgb)
                }
            }
        }
    }
}