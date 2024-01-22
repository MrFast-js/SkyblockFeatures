package mrfast.sbf.features.statDisplays

import mrfast.sbf.SkyblockFeatures
import mrfast.sbf.gui.components.Point
import mrfast.sbf.gui.components.UIElement
import mrfast.sbf.utils.GuiUtils
import mrfast.sbf.utils.Utils

class DefenseDisplay {
    var display: String = Utils.Defense.toString()

    init {
        DefenseDisplayGUI().register()
        updateDisplay()
    }

    private fun updateDisplay() {
        display = buildString {
            append("§a").append(Utils.nf.format(Utils.Defense.toLong()))
        }
    }

    inner class DefenseDisplayGUI : UIElement("Defense Display", Point(0.5651042f, 0.8037037f)) {
        override fun drawElement() {
            updateDisplay()
            GuiUtils.drawText(display, 0f, 0f, GuiUtils.TextStyle.BLACK_OUTLINE)
        }

        override fun drawElementExample() {
            drawElement()
        }

        override fun getToggled(): Boolean = SkyblockFeatures.config.DefenseDisplay

        override fun getRequirement(): Boolean = Utils.inSkyblock

        override fun getHeight(): Int = Utils.GetMC().fontRendererObj.FONT_HEIGHT

        override fun getWidth(): Int = Utils.GetMC().fontRendererObj.getStringWidth(display)
    }
}
