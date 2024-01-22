package mrfast.sbf.features.statDisplays.bars

import mrfast.sbf.SkyblockFeatures
import mrfast.sbf.gui.components.Point
import mrfast.sbf.gui.components.UIElement
import mrfast.sbf.utils.Utils
import net.minecraft.client.gui.Gui
import java.awt.Color

class ManaBar {
    init {
        ManaBarGui().register()
    }

    class ManaBarGui : UIElement("Mana Bar", Point(0.50865895f, 0.9157407f)) {
        override fun drawElement() {
            drawManaBar()
        }

        override fun drawElementExample() {
            drawManaBar()
        }

        private fun drawManaBar() {
            val max = Utils.maxMana
            var mana = Utils.mana
            val overflow = Utils.overflowMana
            val total = max + overflow
            val manaFillPerc = mana.toDouble() / total
            val overflowFillPerc = overflow.toDouble() / total

            val manaColor = Color(0x5555FF)
            val overflowColor = Color(0x55FFFF)

            Gui.drawRect(0, 0, 80, 10, Color.BLACK.rgb)

            Gui.drawRect(2, 2, (78.0 * manaFillPerc).toInt(), 8, manaColor.rgb)
            if (overflow != 0) {
                val fillPixels = (78.0 * overflowFillPerc).toInt() + 3
                Gui.drawRect(
                        minOf(76, maxOf(2, 2 + (78 - fillPixels))),
                        2, 78, 8, overflowColor.rgb
                )
            }
        }

        override fun getToggled(): Boolean = SkyblockFeatures.config.ManaBar

        override fun getRequirement(): Boolean = Utils.inSkyblock

        override fun getHeight(): Int = 11

        override fun getWidth(): Int = 81
    }
}
