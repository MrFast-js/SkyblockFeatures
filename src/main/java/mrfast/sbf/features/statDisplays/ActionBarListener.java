package mrfast.sbf.features.statDisplays;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.utils.Utils;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ActionBarListener {

	public static int maxSecrets;
	public static int secrets = -1;
	private static final Pattern PATTERN_SECRETS = Pattern.compile("§7([0-9]+)/([0-9]+) Secrets");

	public void parseSecrets(String message) {
        Matcher matcher = PATTERN_SECRETS.matcher(message);
        if (!matcher.find()) {
            secrets = -1;
			return;
        }

        secrets = Integer.parseInt(matcher.group(1));
        maxSecrets = Integer.parseInt(matcher.group(2));
    }

	@SubscribeEvent
	public void onEvent(ClientChatReceivedEvent event) {
		if (event.type == 2) {
			String actionBar = event.message.getFormattedText();
			String[] actionBarSplit = actionBar.split(" ");
			for (String piece : actionBarSplit) {
				String trimmed = piece.trim();
				String colorsStripped = Utils.cleanColor(trimmed).replaceAll(",", "");

                if(trimmed.isEmpty()) continue;
				String shortString = colorsStripped.substring(0, colorsStripped.length() - 1).replaceAll(",", "");
				if(colorsStripped.endsWith("❤")) {
					parseAndSetHealth(shortString);
				} else if(colorsStripped.endsWith("❈")) {
					parseAndSetDefense(shortString);
				} else if(colorsStripped.endsWith("✎")) {
					parseAndSetMana(shortString);
				} else if(colorsStripped.endsWith("ʬ")) {
					parseAndSetOverflow(shortString);
				}

				actionBar = actionBar.trim();

				event.message = new ChatComponentText(actionBar);
			}

			parseSecrets(actionBar);

			if(SkyblockFeatures.config.cleanerActionBar) {
				String[] arr = actionBar.split(" ");
				// Remove Numbers wit
                for (String s : arr) {
                    if (s.contains("❤") && SkyblockFeatures.config.hideHealthFromBar) {
                        actionBar = actionBar.replace(s, "");
                    }
                    if ((s.contains("❈") || s.contains("Defense")) && SkyblockFeatures.config.hideDefenseFromBar) {
                        actionBar = actionBar.replace(s, "");
                    }
                    if ((s.contains("✎") || s.contains("Mana")) && SkyblockFeatures.config.hideManaFromBar)  {
                        actionBar = actionBar.replace(s, "");
                    }
					if (s.contains("ʬ") && SkyblockFeatures.config.hideOverflowManaFromBar)  {
						actionBar = actionBar.replace(s, "");
					}
                }

				if (SkyblockFeatures.config.hideSecretsFromBar) {
					actionBar = actionBar.replaceAll(secrets+"/"+maxSecrets+" Secrets", "");
				}

				event.message = new ChatComponentText(actionBar.trim());
            }
		}
	}

	private void parseAndSetHealth(String actionBarSegment) throws NumberFormatException {
		String[] split = actionBarSegment.split("/", 2);
		int currentHealth = Integer.parseInt(split[0]);
		int maxHealth = Integer.parseInt(split[1]);
		Utils.health = currentHealth;
		Utils.maxHealth = maxHealth;
	}

	private void parseAndSetDefense(String actionBarSegment) throws NumberFormatException {
        Utils.Defense = Integer.parseInt(actionBarSegment);
	}

	private void parseAndSetMana(String actionBarSegment) throws NumberFormatException {
		String[] split = actionBarSegment.split("/", 2);
		int currentMana = Integer.parseInt(split[0]);
		int maxMana = Integer.parseInt(split[1]);
		Utils.mana = currentMana;
		Utils.maxMana = maxMana;
	}

	private void parseAndSetOverflow(String actionBarSegment) throws NumberFormatException {
		Utils.overflowMana = Integer.parseInt(actionBarSegment);
	}
}