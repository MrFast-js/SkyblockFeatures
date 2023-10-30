package mrfast.sbf.features.statDisplays;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.utils.Utils;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
				if(trimmed.endsWith("❤")) {
					parseAndSetHealth(shortString);
				} else if(trimmed.endsWith("❈")) {
					parseAndSetDefence(shortString);
				} else if(trimmed.endsWith("✎")) {
					parseAndSetMana(shortString);
				}
				
				actionBar = actionBar.trim();

				event.message = new ChatComponentText(actionBar);
			}

			parseSecrets(actionBar);

			if(SkyblockFeatures.config.hidethings) {
				String[] arr = actionBar.split(" ");
                for (String s : arr) {
                    if (s.contains("❤")) {
                        actionBar = actionBar.replace(s, "");
                    }
                    if (s.contains("❈") || s.contains("Defense")) {
                        actionBar = actionBar.replace(s, "");
                    }
                    if (s.contains("✎") || s.contains("Mana")) {
                        actionBar = actionBar.replace(s, "");
                    }
                }
				event.message = new ChatComponentText(actionBar.replaceAll("\247.\\d+.*Defense", "").trim().replaceAll("\247.\\d+/\\d+✎ Mana", "").trim().replace(secrets+"/"+maxSecrets+" Secrets", "").trim());
            }
		}
	}

	private void parseAndSetHealth(String actionBarSegment) throws NumberFormatException {
		String[] split = actionBarSegment.split("/", 2);
		int currentHealth = Integer.parseInt(split[0]);
		int maxHealth = Integer.parseInt(split[1]);
		Utils.Health = currentHealth;
		Utils.maxHealth = maxHealth;
	}

	private void parseAndSetDefence(String actionBarSegment) throws NumberFormatException {
        Utils.Defence = Integer.parseInt(actionBarSegment);
	}

	private void parseAndSetMana(String actionBarSegment) throws NumberFormatException {
		String[] split = actionBarSegment.split("/", 2);
		int currentMana = Integer.parseInt(split[0]);
		int maxMana = Integer.parseInt(split[1]);
		Utils.Mana = currentMana;
		Utils.maxMana = maxMana;
	}
}