package mrfast.sbf.features.statDisplays

import mrfast.sbf.SkyblockFeatures
import mrfast.sbf.gui.components.Point
import mrfast.sbf.gui.components.UIElement
import mrfast.sbf.utils.GuiUtils
import mrfast.sbf.utils.Utils
import kotlin.math.roundToInt

class EffectiveHealthDisplay {
    init {
        EffectiveHealthDisplayGUI().register()
    }

    private val effectiveHealth: Int
        get() = (Utils.health * (1f + Utils.Defense / 100f)).roundToInt()

    private inner class EffectiveHealthDisplayGUI : UIElement("Effective Health Display", Point(0.3703125f, 0.9539931f)) {
        override fun drawElement() {
            GuiUtils.drawText("§2${Utils.nf.format(effectiveHealth.toLong())}", 0f, 0f, GuiUtils.TextStyle.BLACK_OUTLINE)
        }

        override fun drawElementExample() {
            drawElement()
        }

        override fun getToggled(): Boolean = SkyblockFeatures.config.EffectiveHealthDisplay

        override fun getRequirement(): Boolean = Utils.inSkyblock

        override fun getHeight(): Int = Utils.GetMC().fontRendererObj.FONT_HEIGHT

        override fun getWidth(): Int = Utils.GetMC().fontRendererObj.getStringWidth(effectiveHealth.toString())
    }
}
