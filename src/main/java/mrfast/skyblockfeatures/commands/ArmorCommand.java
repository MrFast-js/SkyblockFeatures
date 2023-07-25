package mrfast.skyblockfeatures.commands;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import mrfast.skyblockfeatures.SkyblockFeatures;
import mrfast.skyblockfeatures.utils.APIUtil;
import mrfast.skyblockfeatures.utils.ItemUtil;
import mrfast.skyblockfeatures.utils.Utils;

public class ArmorCommand extends CommandBase {

	@Override
	public String getCommandName() {
		return "armor";
	}
	
	@Override
	public List<String> getCommandAliases() {
        return Collections.singletonList("armour");
    }

	@Override
	public String getCommandUsage(ICommandSender arg0) {
		return "/" + getCommandName() + " [name]";
	}

	@Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return (args.length >= 1) ? getListOfStringsMatchingLastWord(args, Utils.getListOfPlayerUsernames()) : null;
    }
	
	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}
    String delimiter = EnumChatFormatting.RED.toString() + EnumChatFormatting.STRIKETHROUGH.toString() + "" + EnumChatFormatting.BOLD + "-------------------";
	
	@Override
	public void processCommand(ICommandSender arg0, String[] arg1) throws CommandException {
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
			boolean party = false;
			if (arg1.length == 0) {
				username = player.getName();
				uuid = player.getUniqueID().toString().replaceAll("[\\-]", "");
				player.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Checking armour of " + EnumChatFormatting.DARK_GREEN + username));
			} else {
				username = arg1[0];
				if(username.contains("!")) {
					username.replace("!", "replacement");
					party = true;
				}
				player.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Checking armour of " + EnumChatFormatting.DARK_GREEN + username));
				uuid = APIUtil.getUUID(username);
			}
			
			// Find stats of latest profile
			String latestProfile = APIUtil.getLatestProfileID(uuid, key);
			if (latestProfile == null) return;

			String profileURL = "https://api.hypixel.net/skyblock/profile?profile=" + latestProfile;
			System.out.println("Fetching profile...");
			JsonObject profileResponse = APIUtil.getJSONResponse(profileURL);
			if (!profileResponse.get("success").getAsBoolean()) {
				String reason = profileResponse.get("cause").getAsString();
				player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Failed with reason: " + reason));
				return;
			}

			String playerURL = "https://api.hypixel.net/player?uuid=" + uuid;
			System.out.println("Fetching player data...");
			JsonObject playerResponse = APIUtil.getJSONResponse(playerURL);
			if(!playerResponse.get("success").getAsBoolean()){
				String reason = profileResponse.get("cause").getAsString();
				player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Failed with reason: " + reason));
			}
			int secrets = playerResponse.get("player").getAsJsonObject().get("achievements").getAsJsonObject().get("skyblock_treasure_hunter").getAsInt();

			JsonObject dungeonsObject = profileResponse.get("profile").getAsJsonObject().get("members").getAsJsonObject().get(uuid).getAsJsonObject().get("dungeons").getAsJsonObject();
			JsonObject catacombsObject = dungeonsObject.get("dungeon_types").getAsJsonObject().get("catacombs").getAsJsonObject();
			double catacombs = Utils.xpToDungeonsLevel(catacombsObject.get("experience").getAsDouble());
			
			String armourBase64 = profileResponse.get("profile").getAsJsonObject().get("members").getAsJsonObject().get(uuid).getAsJsonObject().get("inv_armor").getAsJsonObject().get("data").getAsString();
			InputStream armourStream = new ByteArrayInputStream(Base64.getDecoder().decode(armourBase64));
			
			String inventoryBase64 = profileResponse.get("profile").getAsJsonObject().get("members").getAsJsonObject().get(uuid).getAsJsonObject().get("inv_contents").getAsJsonObject().get("data").getAsString();
			InputStream inventoryStream = new ByteArrayInputStream(Base64.getDecoder().decode(inventoryBase64));
			
			try {
				NBTTagCompound armour = CompressedStreamTools.readCompressed(armourStream);
				NBTTagList armourList = armour.getTagList("i", 10);

				String weapon = EnumChatFormatting.RED + "None";
				String weaponLore = EnumChatFormatting.RED + "None";

				if(!profileResponse.get("profile").getAsJsonObject().get("members").getAsJsonObject().get(uuid).getAsJsonObject().has("inv_contents")) {
					weapon = ChatFormatting.RED+"This player has there API disabled!";
					weaponLore = ChatFormatting.RED+"This player has there API disabled!";
				} else {
					NBTTagCompound inventory = CompressedStreamTools.readCompressed(inventoryStream);
					NBTTagList inventoryList = inventory.getTagList("i", 10);

					for (int i = 0; i < inventoryList.tagCount(); i++) {
						NBTTagCompound item = inventoryList.getCompoundTagAt(i);
						if (item.hasNoTags()) continue;
						NBTTagCompound display = item.getCompoundTag("tag").getCompoundTag("display");
						String itemName = item.getCompoundTag("tag").getCompoundTag("display").getString("Name");
						String itemLore = "";
						if (display.hasKey("Lore", ItemUtil.NBT_LIST)) {
							NBTTagList lore = display.getTagList("Lore", ItemUtil.NBT_STRING);
							
							List<String> loreAsList = new ArrayList<>();
							for (int lineNumber = 0; lineNumber < lore.tagCount(); lineNumber++) {
								loreAsList.add(lore.getStringTagAt(lineNumber));
							}
							
							itemLore = itemName+"\n"+String.join("\n",Collections.unmodifiableList(loreAsList));
						}
						// NBT is served boots -> helmet
						switch (i) {
							case 0:
								weapon = itemName;
								weaponLore = itemLore;
								break;
							default:
								System.err.println("An error has occurred.");
								break;
						}
					}
					inventoryStream.close();
				}
				
				String helmet = EnumChatFormatting.RED + "None";
				String chest = EnumChatFormatting.RED + "None";
				String legs = EnumChatFormatting.RED + "None";
				String boots = EnumChatFormatting.RED + "None";
                String helmetLore = EnumChatFormatting.RED + "None";
				String chestLore = EnumChatFormatting.RED + "None";
				String legsLore = EnumChatFormatting.RED + "None";
				String bootsLore = EnumChatFormatting.RED + "None";
				// Loop through armour
				for (int i = 0; i < armourList.tagCount(); i++) {
					NBTTagCompound armourPiece = armourList.getCompoundTagAt(i);
					if (armourPiece.hasNoTags()) continue;
					NBTTagCompound display = armourPiece.getCompoundTag("tag").getCompoundTag("display");
					String armourPieceName = armourPiece.getCompoundTag("tag").getCompoundTag("display").getString("Name");
                    String armourPieceLore = "";
                    if (display.hasKey("Lore", ItemUtil.NBT_LIST)) {
                        NBTTagList lore = display.getTagList("Lore", ItemUtil.NBT_STRING);
        
                        List<String> loreAsList = new ArrayList<>();
                        for (int lineNumber = 0; lineNumber < lore.tagCount(); lineNumber++) {
                            loreAsList.add(lore.getStringTagAt(lineNumber));
                        }
        
                        armourPieceLore = armourPieceName+"\n"+String.join("\n",Collections.unmodifiableList(loreAsList));
                    }
					// NBT is served boots -> helmet
					switch (i) {
						case 0:
							boots = armourPieceName;
                            bootsLore = armourPieceLore;
							break;
						case 1:
							legs = armourPieceName;
                            legsLore = armourPieceLore;
							break;
						case 2:
							chest = armourPieceName;
                            chestLore = armourPieceLore;
							break;
						case 3:
							helmet = armourPieceName;
                            helmetLore = armourPieceLore;
							break;
						default:
							System.err.println("An error has occurred.");
							break;
					}
				}
				armourStream.close();


                ChatComponentText nameComponent = new ChatComponentText(EnumChatFormatting.AQUA+" Data For: " +EnumChatFormatting.YELLOW+ username + "\n ");
				ChatComponentText kickComponent = new ChatComponentText("\n"+EnumChatFormatting.GREEN+"Click here to remove "+EnumChatFormatting.LIGHT_PURPLE+username+EnumChatFormatting.GREEN+" from the party");
                ChatComponentText weaponComponent = new ChatComponentText(EnumChatFormatting.DARK_AQUA + weapon + "\n ");
				ChatComponentText helmetComponent = new ChatComponentText(" "+EnumChatFormatting.DARK_AQUA + helmet + "\n ");
                ChatComponentText chestComponent = new ChatComponentText(EnumChatFormatting.DARK_AQUA + chest + "\n ");
                ChatComponentText legComponent = new ChatComponentText(EnumChatFormatting.DARK_AQUA + legs + "\n ");
                ChatComponentText bootComponent = new ChatComponentText(EnumChatFormatting.DARK_AQUA + boots + "\n ");

				weaponComponent.setChatStyle(weaponComponent.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(weaponLore))));
				helmetComponent.setChatStyle(helmetComponent.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(helmetLore))));
				chestComponent.setChatStyle(chestComponent.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(chestLore))));
                legComponent.setChatStyle(legComponent.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(legsLore))));
				kickComponent.setChatStyle(kickComponent.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/p kick "+username)));
                bootComponent.setChatStyle(bootComponent.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(bootsLore))));
                
				StringBuilder completionsHoverString = new StringBuilder();
				int highestFloor = catacombsObject.get("highest_tier_completed").getAsInt();
				JsonObject completionObj = catacombsObject.get("tier_completions").getAsJsonObject();
				int totalRuns = 0;
				for (int i = 0; i <= highestFloor; i++) {
					completionsHoverString
							.append(EnumChatFormatting.GOLD)
							.append(i == 0 ? "Entrance: " : "Floor " + i + ": ")
							.append(EnumChatFormatting.RESET)
							.append(completionObj.get(String.valueOf(i)).getAsInt())
							.append(i < highestFloor ? "\n": "");

					totalRuns = totalRuns + completionObj.get(String.valueOf(i)).getAsInt();
				}
				completionsHoverString.append("\n"+EnumChatFormatting.GOLD+"Total: "+ChatFormatting.RESET+totalRuns);
				ChatComponentText completions = new ChatComponentText(EnumChatFormatting.AQUA + " Floor Completions: "+ChatFormatting.GRAY+"(Hover)");

				completions.setChatStyle(completions.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(completionsHoverString.toString()))));

                player.addChatMessage(
                    new ChatComponentText(delimiter)
                    .appendText("\n")
                    .appendSibling(nameComponent)
					.appendText(ChatFormatting.GREEN+"☠ Cata Level: "+ChatFormatting.YELLOW+catacombs+"\n")
					.appendText(ChatFormatting.GREEN+" Total Secrets Found: "+ChatFormatting.YELLOW+secrets+"\n")
					.appendText(ChatFormatting.GREEN+" Average Secrets: "+ChatFormatting.YELLOW+(secrets/totalRuns)+"\n\n")
                    .appendSibling(helmetComponent)
                    .appendSibling(chestComponent)
                    .appendSibling(legComponent)
                    .appendSibling(bootComponent)
					.appendSibling(weaponComponent)
                    .appendText("\n")
					.appendSibling(completions)
					.appendText("\n")
                    .appendSibling(new ChatComponentText(delimiter))
					.appendSibling(party? kickComponent: new ChatComponentText("")));
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			
				// §c§l§i-----------------------
				// §bData For: [MVP++] Lapizzz
				// §a☠ Cata Level: §e29.83
				// §aTotal Secrets Found: §e5,173
				// 
				// Helmet
				// Chestplate
				// Leggings
				// Boots
				// Main Hand
				//
				// §bFloor Completions: §7(Hover)
				// §c§l§i-----------------------
		}).start();
	}
}
