package mrfast.sbf.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.text.DecimalFormat;
import java.util.List;

import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.utils.APIUtils;
import mrfast.sbf.utils.Utils;

public class DungeonsCommand extends CommandBase {

	@Override
	public String getCommandName() {
		return "dungeons";
	}

	@Override
	public String getCommandUsage(ICommandSender arg0) {
		return "/" + getCommandName() + " [name]";
	}

	public static String usage(ICommandSender arg0) {
		return new DungeonsCommand().getCommandUsage(arg0);
	}

	@Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return (args.length >= 1) ? getListOfStringsMatchingLastWord(args, Utils.getListOfPlayerUsernames()) : null;
    }

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	
	@Override
	public void processCommand(ICommandSender arg0, String[] arg1) throws CommandException {
		// MULTI THREAD DRIFTING
		new Thread(() -> {
			EntityPlayer player = (EntityPlayer) arg0;
			
			// Check key
			String key = SkyblockFeatures.config.apiKey;
			if (key.equals("")) {
				player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "API key not set. Use /setkey."));
			}
			
			// Get UUID for Hypixel API requests
			String username;
			String uuid;
			if (arg1.length == 0) {
				username = player.getName();
				uuid = player.getUniqueID().toString().replaceAll("[\\-]", "");
				player.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Checking dungeon stats of " + EnumChatFormatting.DARK_GREEN + username));
			} else {
				username = arg1[0];
				player.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Checking dungeon stats of " + EnumChatFormatting.DARK_GREEN + username));
				uuid = APIUtils.getUUID(username);
			}
			
			// Find stats of latest profile
			String latestProfile = APIUtils.getLatestProfileID(uuid, key);
			if (latestProfile == null) return;
			
			String profileURL = "https://api.hypixel.net/skyblock/profile?profile=" + latestProfile;
			System.out.println("Fetching profile...");
			JsonObject profileResponse = APIUtils.getJSONResponse(profileURL);
			if(profileResponse.has("cause")) {
				String reason = profileResponse.get("cause").getAsString();
				player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Failed with reason: " + reason));
				return;
			}

			String playerURL = "https://api.hypixel.net/player?uuid=" + uuid;
			System.out.println("Fetching player data...");
			JsonObject playerResponse = APIUtils.getJSONResponse(playerURL);
			if(playerResponse.has("cause")){
				player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "This player has not played on Hypixel."));
			}
			
			System.out.println("Fetching dungeon stats...");
			JsonObject dungeonsObject = profileResponse.get("profile").getAsJsonObject().get("members").getAsJsonObject().get(uuid).getAsJsonObject().get("dungeons").getAsJsonObject();
			if (!dungeonsObject.get("dungeon_types").getAsJsonObject().get("catacombs").getAsJsonObject().has("experience")) {
				player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "This player has not played dungeons."));
				return;
			}

			JsonObject catacombsObject = dungeonsObject.get("dungeon_types").getAsJsonObject().get("catacombs").getAsJsonObject();
			double catacombs = Utils.xpToDungeonsLevel(catacombsObject.get("experience").getAsDouble());
			double healer = Utils.xpToDungeonsLevel(dungeonsObject.get("player_classes").getAsJsonObject().get("healer").getAsJsonObject().get("experience").getAsDouble());
			double mage = Utils.xpToDungeonsLevel(dungeonsObject.get("player_classes").getAsJsonObject().get("mage").getAsJsonObject().get("experience").getAsDouble());
			double berserk = Utils.xpToDungeonsLevel(dungeonsObject.get("player_classes").getAsJsonObject().get("berserk").getAsJsonObject().get("experience").getAsDouble());
			double archer = Utils.xpToDungeonsLevel(dungeonsObject.get("player_classes").getAsJsonObject().get("archer").getAsJsonObject().get("experience").getAsDouble());
			double tank = Utils.xpToDungeonsLevel(dungeonsObject.get("player_classes").getAsJsonObject().get("tank").getAsJsonObject().get("experience").getAsDouble());
			String selectedClass = Utils.convertToTitleCase(dungeonsObject.get("selected_dungeon_class").getAsString());
			int secrets = playerResponse.get("player").getAsJsonObject().get("achievements").getAsJsonObject().get("skyblock_treasure_hunter").getAsInt();
			double totalRuns = 0;
			int highestFloor = catacombsObject.get("highest_tier_completed").getAsInt();
			JsonObject completionObj = catacombsObject.get("tier_completions").getAsJsonObject();

			String delimiter = EnumChatFormatting.AQUA.toString() + EnumChatFormatting.STRIKETHROUGH.toString() + "" + EnumChatFormatting.BOLD + "-------------------";

			StringBuilder completionsHoverString = new StringBuilder();

			for (int i = 0; i <= highestFloor; i++) {
				completionsHoverString
						.append(EnumChatFormatting.GOLD)
						.append(i == 0 ? "Entrance: " : "Floor " + i + ": ")
						.append(EnumChatFormatting.RESET)
						.append(completionObj.get(String.valueOf(i)).getAsInt())
						.append(i < highestFloor ? "\n": "");

				totalRuns = totalRuns + completionObj.get(String.valueOf(i)).getAsDouble();
			}
			completionsHoverString.append("\n"+EnumChatFormatting.GOLD+"Total: "+ChatFormatting.RESET+totalRuns);
			ChatComponentText completions = new ChatComponentText(EnumChatFormatting.GOLD + " Highest Floor Completed: " + highestFloor);

			completions.setChatStyle(completions.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(completionsHoverString.toString()))));

			DecimalFormat f = new DecimalFormat("##.00");
			ChatComponentText classLevels = new ChatComponentText(
					EnumChatFormatting.GOLD + " Selected Class: " + selectedClass + "\n\n" +
						 EnumChatFormatting.RED + " Catacombs Level: " + catacombs + "\n" +
						 EnumChatFormatting.YELLOW + " Healer Level: " + healer + "\n" +
						 EnumChatFormatting.LIGHT_PURPLE + " Mage Level: " + mage + "\n" +
						 EnumChatFormatting.RED + " Berserk Level: " + berserk + "\n" +
						 EnumChatFormatting.GREEN + " Archer Level: " + archer + "\n" +
						 EnumChatFormatting.BLUE + " Tank Level: " + tank + "\n\n" +
						 EnumChatFormatting.WHITE + " Secrets Found: " + secrets + "\n"+
						 EnumChatFormatting.DARK_AQUA + " Average Secrets: "+ f.format(secrets / totalRuns) +"\n\n");

			player.addChatMessage(
					new ChatComponentText(delimiter)
					.appendText("\n")
					.appendSibling(classLevels)
					.appendSibling(completions)
					.appendText("\n")
					.appendSibling(new ChatComponentText(delimiter))
				);
		}).start();
	}
}
