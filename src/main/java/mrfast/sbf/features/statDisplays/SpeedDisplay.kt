package mrfast.sbf.features.statDisplays

import mrfast.sbf.SkyblockFeatures
import mrfast.sbf.gui.components.Point
import mrfast.sbf.gui.components.UIElement
import mrfast.sbf.utils.GuiUtils
import mrfast.sbf.utils.Utils
import net.minecraft.client.Minecraft

class SpeedDisplay {
    var display: String = ""

    init {
        SpeedDisplayGUI().register()
    }

    private fun updateSpeed() {
        val walkSpeed = (Minecraft.getMinecraft().thePlayer?.capabilities?.walkSpeed ?: 0f) * 1000
        val text = walkSpeed.toString().substring(0, minOf(walkSpeed.toString().length, 3))
        display = if (text.endsWith(".")) text.substring(0, text.indexOf('.')) else "$text%"
    }

    inner class SpeedDisplayGUI : UIElement("Speed Display", Point(0.375f, 0.9777778f)) {
        override fun drawElement() {
            updateSpeed()
            GuiUtils.drawText(display, 0f, 0f, GuiUtils.TextStyle.BLACK_OUTLINE)
        }

        override fun drawElementExample() {
            drawElement()
        }

        override fun getToggled(): Boolean = SkyblockFeatures.config.SpeedDisplay

        override fun getRequirement(): Boolean = Utils.inSkyblock

        override fun getHeight(): Int = Utils.GetMC().fontRendererObj.FONT_HEIGHT

        override fun getWidth(): Int = Utils.GetMC().fontRendererObj.getStringWidth(display)
    }
}
