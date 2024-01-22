package mrfast.sbf.features.statDisplays

import mrfast.sbf.SkyblockFeatures
import mrfast.sbf.gui.components.Point
import mrfast.sbf.gui.components.UIElement
import mrfast.sbf.utils.GuiUtils
import mrfast.sbf.utils.Utils

class OverflowManaDisplay {
    var display: String = ""

    init {
        OverflowManaDisplayGUI().register()
    }

    private fun updateDisplay() {
        display = buildString {
            append("§3").append(Utils.nf.format(Utils.overflowMana)).append("ʬ")
        }
    }

    inner class OverflowManaDisplayGUI : UIElement("Overflow Mana Display", Point(0.6020833f, 0.96666664f)) {
        override fun drawElement() {
            updateDisplay()
            GuiUtils.drawText(display, 0f, 0f, GuiUtils.TextStyle.BLACK_OUTLINE)
        }

        override fun drawElementExample() {
            drawElement();
        }

        override fun getToggled(): Boolean = SkyblockFeatures.config.overFlowManaDisplay

        override fun getRequirement(): Boolean = Utils.inSkyblock

        override fun getHeight(): Int = Utils.GetMC().fontRendererObj.FONT_HEIGHT

        override fun getWidth(): Int = Utils.GetMC().fontRendererObj.getStringWidth(display)
    }
}
