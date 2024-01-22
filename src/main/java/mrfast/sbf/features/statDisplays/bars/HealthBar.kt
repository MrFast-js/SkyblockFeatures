package mrfast.sbf.features.statDisplays.bars

import mrfast.sbf.SkyblockFeatures
import mrfast.sbf.gui.components.Point
import mrfast.sbf.gui.components.UIElement
import mrfast.sbf.utils.Utils
import net.minecraft.client.gui.Gui
import java.awt.Color

class HealthBar {
    init {
        HealthBarGui().register()
    }

    class HealthBarGui : UIElement("Health Bar", Point(0.40605482f, 0.9166667f)) {
        override fun drawElement() {
            drawHealthBar()
        }

        override fun drawElementExample() {
            drawHealthBar()
        }

        private fun drawHealthBar() {
            val max = Utils.maxHealth
            var health = Utils.health
            var absorption = 0
            if (health > max) {
                absorption = health - max
                health = max
            }
            val total = max + absorption
            val healthFillPerc = health.toDouble() / total
            val absorbFillPerc = absorption.toDouble() / total

            val healthColor = Color.RED
            val absorbColor = Color(0xFFAA00)

            Gui.drawRect(0, 0, 80, 10, Color.BLACK.rgb)

            Gui.drawRect(2, 2, (78.0 * healthFillPerc).toInt(), 8, healthColor.rgb)
            if (absorption != 0) {
                val fillPixels = (78.0 * absorbFillPerc).toInt() + 3
                Gui.drawRect(
                        minOf(76, maxOf(2, 2 + (78 - fillPixels))),
                        2, 78, 8, absorbColor.rgb
                )
            }
        }

        override fun getToggled(): Boolean = SkyblockFeatures.config.HealthBar

        override fun getRequirement(): Boolean = Utils.inSkyblock

        override fun getHeight(): Int = 11

        override fun getWidth(): Int = 81
    }
}
