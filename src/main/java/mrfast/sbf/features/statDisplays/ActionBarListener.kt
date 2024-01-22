package mrfast.sbf.features.statDisplays

import mrfast.sbf.SkyblockFeatures
import mrfast.sbf.utils.Utils
import net.minecraft.util.ChatComponentText
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.regex.Matcher
import java.util.regex.Pattern

class ActionBarListener {
    companion object {
        var maxSecrets: Int = 0
        var secrets: Int = -1
        private val PATTERN_SECRETS: Pattern = Pattern.compile("§7([0-9]+)/([0-9]+) Secrets")
    }

    private fun parseSecrets(message: String) {
        val matcher: Matcher = PATTERN_SECRETS.matcher(message)
        if (!matcher.find()) {
            secrets = -1
            return
        }

        secrets = Integer.parseInt(matcher.group(1))
        maxSecrets = Integer.parseInt(matcher.group(2))
    }

    @SubscribeEvent
    fun onEvent(event: ClientChatReceivedEvent) {
        if (event.type.toInt() == 2) {
            var actionBar: String = event.message.formattedText
            val actionBarSplit: List<String> = actionBar.split(" ")

            for (piece in actionBarSplit) {
                val trimmed: String = piece.trim()
                val colorsStripped: String = Utils.cleanColor(trimmed).replace(",", "")

                if (trimmed.isEmpty()) continue
                val shortString: String = colorsStripped.substring(0, colorsStripped.length - 1).replace(",", "")

                when {
                    colorsStripped.endsWith("❤") -> parseAndSetHealth(shortString)
                    colorsStripped.endsWith("❈") -> parseAndSetDefense(shortString)
                    colorsStripped.endsWith("✎") -> parseAndSetMana(shortString)
                    colorsStripped.endsWith("ʬ") -> parseAndSetOverflow(shortString)
                }

                actionBar = actionBar.trim()
                event.message = ChatComponentText(actionBar)
            }

            parseSecrets(actionBar)

            if (SkyblockFeatures.config.cleanerActionBar) {
                val arr: List<String> = actionBar.split(" ")

                for (s in arr) {
                    when {
                        s.contains("❤") && SkyblockFeatures.config.hideHealthFromBar -> actionBar = actionBar.replace(s, "")
                        (s.contains("❈") || s.contains("Defense")) && SkyblockFeatures.config.hideDefenseFromBar -> actionBar = actionBar.replace(s, "")
                        (s.contains("✎") || s.contains("Mana")) && SkyblockFeatures.config.hideManaFromBar -> actionBar = actionBar.replace(s, "")
                        s.contains("ʬ") && SkyblockFeatures.config.hideOverflowManaFromBar -> actionBar = actionBar.replace(s, "")
                    }
                }

                if (SkyblockFeatures.config.hideSecretsFromBar) {
                    actionBar = actionBar.replace("$secrets/$maxSecrets Secrets", "")
                }

                event.message = ChatComponentText(actionBar.trim())
            }
        }
    }

    private fun parseAndSetHealth(actionBarSegment: String) {
        val split: List<String> = actionBarSegment.split("/")
        val currentHealth: Int = split[0].toInt()
        val maxHealth: Int = split[1].toInt()
        Utils.health = currentHealth
        Utils.maxHealth = maxHealth
    }

    private fun parseAndSetDefense(actionBarSegment: String) {
        Utils.Defense = actionBarSegment.toInt()
    }

    private fun parseAndSetMana(actionBarSegment: String) {
        val split: List<String> = actionBarSegment.split("/")
        val currentMana: Int = split[0].toInt()
        val maxMana: Int = split[1].toInt()
        Utils.mana = currentMana
        Utils.maxMana = maxMana
    }

    private fun parseAndSetOverflow(actionBarSegment: String) {
        Utils.overflowMana = actionBarSegment.toInt()
    }
}
