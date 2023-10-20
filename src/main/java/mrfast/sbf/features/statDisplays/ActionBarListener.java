package mrfast.sbf.features.statDisplays;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.utils.Utils;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ActionBarListener {

	public static String latestExpMessage = "";
	public static String latestthingwingy = "";

	static String xppercent = "";
	String expGained = "";
	String skillName = "";

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
				String colorsStripped = trimmed.replaceAll("\247.", "").replaceAll(",", "");;

				if(trimmed.isEmpty())
					continue;

				if(trimmed.endsWith("❤")) {
					parseAndSetHealth(colorsStripped.substring(0, colorsStripped.length() - 1).replaceAll(",", ""));
				} else if(trimmed.endsWith("❈")) {
					parseAndSetDefence(colorsStripped.substring(0, colorsStripped.length() - 1).replaceAll(",", ""));
				} else if(trimmed.endsWith("✎")) {
					parseAndSetMana(colorsStripped.substring(0, colorsStripped.length() - 1).replaceAll(",", ""));
				}
				
				actionBar = actionBar.trim();

				event.message = new ChatComponentText(actionBar);
			}

			parseSecrets(actionBar);

			if(SkyblockFeatures.config.hidethings) {
				String[] arr = actionBar.split(" ");
				for(int i=0;i<arr.length;i++) {
					if(arr[i].contains("❤")) {
						actionBar = actionBar.replace(arr[i],"");
					}
					if(arr[i].contains("❈") || arr[i].contains("Defense")) {
						actionBar = actionBar.replace(arr[i],"");
					}
					if(arr[i].contains("✎") || arr[i].contains("Mana")) {
						actionBar = actionBar.replace(arr[i],"");
					}
				}
				event.message = new ChatComponentText(actionBar.replaceAll("\247.\\d+.*Defense", "").trim().replaceAll("\247.\\d+/\\d+✎ Mana", "").trim().replace(secrets+"/"+maxSecrets+" Secrets", "").trim());
				return;
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
		int defence = Integer.parseInt(actionBarSegment);
		Utils.Defence = defence;
	}

	private void parseAndSetMana(String actionBarSegment) throws NumberFormatException {
		String[] split = actionBarSegment.split("/", 2);
		int currentMana = Integer.parseInt(split[0]);
		int maxMana = Integer.parseInt(split[1]);
		Utils.Mana = currentMana;
		Utils.maxMana = maxMana;
	}
}