package mrfast.sbf.features.statDisplays

import mrfast.sbf.SkyblockFeatures
import mrfast.sbf.gui.components.Point
import mrfast.sbf.gui.components.UIElement
import mrfast.sbf.utils.GuiUtils
import mrfast.sbf.utils.TabListUtils
import mrfast.sbf.utils.Utils
import net.minecraft.client.Minecraft
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.client.renderer.GlStateManager
import java.util.regex.Pattern

class CryptDisplay {
    init {
        CryptDisplayGUI().register()
    }

    class CryptDisplayGUI : UIElement("Crypt Display", Point(0.0f, 0.24930556f)) {
        private val cryptsPattern = Pattern.compile("§r Crypts: §r§6(?<crypts>\\d+)§r")

        override fun drawElement() {
            var crypts = 0
            for (pi: NetworkPlayerInfo in TabListUtils.getTabEntries()) {
                try {
                    val name = Minecraft.getMinecraft().ingameGUI.tabList.getPlayerName(pi)
                    if (name.contains("Crypts:")) {
                        val matcher = cryptsPattern.matcher(name)
                        if (matcher.find()) {
                            crypts = matcher.group("crypts")?.toIntOrNull() ?: 0
                        }
                    }
                } catch (ignored: NumberFormatException) {
                }
            }

            val color = if (crypts >= 5) "§a" else "§c"

            val scale = 2f
            GlStateManager.scale(scale, scale, 0f)
            GuiUtils.drawText("$color Crypts: $crypts", 0f, 0f, GuiUtils.TextStyle.DROP_SHADOW)
            GlStateManager.scale(1 / scale, 1 / scale, 0f)
        }

        override fun drawElementExample() {
            val scale = 2f
            GlStateManager.scale(scale, scale, 0f)
            GuiUtils.drawText("§cCrypts: 3", 0f, 0f, GuiUtils.TextStyle.DROP_SHADOW)
            GlStateManager.scale(1 / scale, 1 / scale, 0f)
        }

        override fun getToggled(): Boolean = SkyblockFeatures.config.cryptCount

        override fun getRequirement(): Boolean = Utils.inDungeons && Utils.inSkyblock

        override fun getHeight(): Int = Utils.GetMC().fontRendererObj.FONT_HEIGHT * 2

        override fun getWidth(): Int = Utils.GetMC().fontRendererObj.getStringWidth("§6Estimated Secret C")
    }
}
