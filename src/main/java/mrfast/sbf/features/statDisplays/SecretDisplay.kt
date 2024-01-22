package mrfast.sbf.features.statDisplays

import mrfast.sbf.SkyblockFeatures
import mrfast.sbf.gui.components.Point
import mrfast.sbf.gui.components.UIElement
import mrfast.sbf.utils.GuiUtils
import mrfast.sbf.utils.Utils
import org.jetbrains.annotations.NotNull

class SecretDisplay {
    init {
        SecretDisplayGui().register()
    }

    inner class SecretDisplayGui : UIElement("Dungeon Secret", Point(0.59876317f, 0.9574074f)) {
        override fun drawElement() {
            val secrets = ActionBarListener.secrets
            val maxSecrets = ActionBarListener.maxSecrets

            val text = getSecrets(secrets, maxSecrets)

            GuiUtils.drawCenteredText(this, text, GuiUtils.TextStyle.BLACK_OUTLINE)
        }

        override fun drawElementExample() {
            val text = arrayListOf<String>()

            text.add("§7Secrets")
            text.add("§c1§7/§c9")

            GuiUtils.drawCenteredText(this, text, GuiUtils.TextStyle.BLACK_OUTLINE)
        }

        override fun getToggled(): Boolean {
            return SkyblockFeatures.config.SecretsDisplay
        }

        override fun getRequirement(): Boolean {
            return Utils.inDungeons && Utils.inSkyblock
        }

        override fun getHeight(): Int {
            return Utils.GetMC().fontRendererObj.FONT_HEIGHT * 2
        }

        override fun getWidth(): Int {
            return Utils.GetMC().fontRendererObj.getStringWidth("§7Secrets")
        }
    }

    @NotNull
    private fun getSecrets(secrets: Int, maxSecrets: Int): List<String> {
        val text = ArrayList<String>()

        val color: String = when {
            secrets == maxSecrets -> "§a"
            secrets > maxSecrets / 2 -> "§e"
            else -> "§c"
        }

        text.add("§7Secrets")

        if (secrets == -1) {
            text.add("§7None")
        } else {
            text.add("$color$secrets§7/$color$maxSecrets")
        }
        return text
    }
}
