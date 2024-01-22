package mrfast.sbf.features.statDisplays

import mrfast.sbf.SkyblockFeatures
import mrfast.sbf.gui.components.Point
import mrfast.sbf.gui.components.UIElement
import mrfast.sbf.utils.GuiUtils
import mrfast.sbf.utils.Utils

class HealthDisplay {
    var display: String = ""

    init {
        HealthDisplayGUI().register()
    }

    private fun updateDisplay() {
        display = buildString {
            append("§c").append(Utils.nf.format(Utils.health.toLong())).append("/").append(Utils.nf.format(Utils.maxHealth.toLong()))
            if (Utils.health > Utils.maxHealth) append("§6").append(Utils.nf.format(Utils.health.toLong())).append("§c/").append(Utils.nf.format(Utils.maxHealth.toLong()))
        }
    }

    inner class HealthDisplayGUI : UIElement("Health Display", Point(0.39878336f, 0.8029036f)) {
        override fun drawElement() {
            updateDisplay()
            GuiUtils.drawText(display, 0f, 0f, GuiUtils.TextStyle.BLACK_OUTLINE)
        }

        override fun drawElementExample() {
            drawElement();
        }

        override fun getToggled(): Boolean = SkyblockFeatures.config.HealthDisplay

        override fun getRequirement(): Boolean = Utils.inSkyblock

        override fun getHeight(): Int = Utils.GetMC().fontRendererObj.FONT_HEIGHT

        override fun getWidth(): Int = Utils.GetMC().fontRendererObj.getStringWidth(display)
    }
}
