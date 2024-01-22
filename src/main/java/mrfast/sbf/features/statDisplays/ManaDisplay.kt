package mrfast.sbf.features.statDisplays

import mrfast.sbf.SkyblockFeatures
import mrfast.sbf.gui.components.Point
import mrfast.sbf.gui.components.UIElement
import mrfast.sbf.utils.GuiUtils
import mrfast.sbf.utils.Utils

class ManaDisplay {
    var display: String = Utils.mana.toString() + "/" + Utils.maxMana

    init {
        ManaDisplayGUI().register()
        updateDisplay()
    }

    private fun updateDisplay() {
        display = buildString {
            append("§9").append(Utils.nf.format(Utils.mana.toLong())).append("/").append(Utils.nf.format(Utils.maxMana.toLong()))
        }
    }

    inner class ManaDisplayGUI : UIElement("Mana Display", Point(0.47864583f, 0.80324066f)) {
        override fun drawElement() {
            updateDisplay();
            GuiUtils.drawText(display, 0f, 0f, GuiUtils.TextStyle.BLACK_OUTLINE)
        }

        override fun drawElementExample() {
            drawElement()
        }

        override fun getToggled(): Boolean = SkyblockFeatures.config.ManaDisplay

        override fun getRequirement(): Boolean = Utils.inSkyblock

        override fun getHeight(): Int = Utils.GetMC().fontRendererObj.FONT_HEIGHT

        override fun getWidth(): Int = Utils.GetMC().fontRendererObj.getStringWidth(display)
    }
}
