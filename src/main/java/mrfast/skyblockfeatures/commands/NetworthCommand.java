package mrfast.skyblockfeatures.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import mrfast.skyblockfeatures.SkyblockFeatures;
import mrfast.skyblockfeatures.utils.APIUtil;
import mrfast.skyblockfeatures.utils.Utils;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;

public class NetworthCommand extends CommandBase {

	@Override
	public String getCommandName() {
		return "networth";
	}
	
	@Override
	public List<String> getCommandAliases() {
        return Collections.singletonList("nw");
    }

	@Override
	public String getCommandUsage(ICommandSender arg0) {
		return "/" + getCommandName() + " [name]";
	}

	public static String usage(ICommandSender arg0) {
		return new NetworthCommand().getCommandUsage(arg0);
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
		new Thread(() -> {
			EntityPlayer player = (EntityPlayer) arg0;
			
			// Check key
			String key = SkyblockFeatures.config.apiKey;
			if (key.equals("")) {
				player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "API key not set."));
			}
			
			// Get UUID for Hypixel API requests
			String username;
			String uuid;
			if (arg1.length == 0) {
				username = player.getName();
				uuid = player.getUniqueID().toString().replaceAll("[\\-]", "");
			} else {
				username = arg1[0];
				uuid = APIUtil.getUUID(username);
			}
			Utils.SendMessage(EnumChatFormatting.GREEN + "Checking networth of " + EnumChatFormatting.DARK_GREEN + username+ChatFormatting.AQUA+" (Skycrypt API)");
			
			// Find stats of latest profile
			String latestProfile = APIUtil.getLatestProfileID(uuid, key);
			if (latestProfile == null) return;
			
			String profileURL = "https://sky.shiiyu.moe/api/v2/profile/"+uuid;
			System.out.println("Fetching profile... "+profileURL);
			JsonObject profileResponse = APIUtil.getJSONResponse(profileURL);

			if (profileResponse.has("error")) {
				String reason = profileResponse.get("error").getAsString();
				Utils.SendMessage(EnumChatFormatting.RED + "Failed with reason: " + reason);
				return;
			}

			profileResponse = profileResponse.get("profiles").getAsJsonObject();
			
			System.out.println("Player Data ");
			JsonObject networthJson = profileResponse.get(latestProfile).getAsJsonObject().get("data").getAsJsonObject().get("networth").getAsJsonObject();
			JsonObject types = networthJson.get("types").getAsJsonObject();
			System.out.println("Got networth player data");
			NumberFormat nf = NumberFormat.getIntegerInstance(Locale.US);
			double purse = networthJson.get("purse").getAsDouble();
			double Bank = networthJson.get("bank").getAsDouble();
			double Sacks = types.get("sacks").getAsJsonObject().get("total").getAsDouble();
			double Armor = types.get("armor").getAsJsonObject().get("total").getAsDouble();
			double Equipment = types.get("equipment").getAsJsonObject().get("total").getAsDouble();
			double Wardrobe = types.get("wardrobe").getAsJsonObject().get("total").getAsDouble();
			double Inventory = types.get("inventory").getAsJsonObject().get("total").getAsDouble();
			double enderchest = types.get("enderchest").getAsJsonObject().get("total").getAsDouble();
			double accessories = types.get("accessories").getAsJsonObject().get("total").getAsDouble();
			double personal_vault = types.get("personal_vault").getAsJsonObject().get("total").getAsDouble();
			double storage = types.get("storage").getAsJsonObject().get("total").getAsDouble();
			double pets = types.get("pets").getAsJsonObject().get("total").getAsDouble();
			double total = networthJson.get("networth").getAsDouble();

			player.addChatMessage(new ChatComponentText(EnumChatFormatting.AQUA.toString()+EnumChatFormatting.STRIKETHROUGH.toString() + "" + EnumChatFormatting.BOLD + "-------------------\n" +
														EnumChatFormatting.AQUA + " " + username + "'s Networth:\n" +
														EnumChatFormatting.GREEN + " Purse: " + EnumChatFormatting.GOLD + nf.format(purse) +percentOf(purse,total)+ "\n" +
														EnumChatFormatting.GREEN + " Bank: " + EnumChatFormatting.GOLD + nf.format(Bank) +percentOf(Bank,total)+ "\n" +
														EnumChatFormatting.GREEN + " Sacks: " + EnumChatFormatting.GOLD + nf.format(Sacks) +percentOf(Sacks,total)+ "\n" +
														EnumChatFormatting.GREEN + " Armor: " + EnumChatFormatting.GOLD + nf.format(Armor) +percentOf(Armor,total)+ "\n" +
														EnumChatFormatting.GREEN + " Equipment: " + EnumChatFormatting.GOLD + nf.format(Equipment) +percentOf(Equipment,total)+ "\n" +
														EnumChatFormatting.GREEN + " Wardrobe: " + EnumChatFormatting.GOLD + nf.format(Wardrobe) +percentOf(Wardrobe,total)+ "\n" +
														EnumChatFormatting.GREEN + " Inventory: " + EnumChatFormatting.GOLD + nf.format(Inventory) +percentOf(Inventory,total)+ "\n" +
														EnumChatFormatting.GREEN + " Enderchest: " + EnumChatFormatting.GOLD + nf.format(enderchest) +percentOf(enderchest,total)+ "\n" +
														EnumChatFormatting.GREEN + " Accessories: " + EnumChatFormatting.GOLD + nf.format(accessories) +percentOf(accessories,total)+ "\n" +
														EnumChatFormatting.GREEN + " Vault: " + EnumChatFormatting.GOLD + nf.format(personal_vault) +percentOf(personal_vault,total)+ "\n" +
														EnumChatFormatting.GREEN + " Storage: " + EnumChatFormatting.GOLD + nf.format(storage) +percentOf(storage,total)+ "\n" +
														EnumChatFormatting.GREEN + " Pets: " + EnumChatFormatting.GOLD + nf.format(pets) +percentOf(pets,total)+ "\n" +
														EnumChatFormatting.GREEN + " Total Networth: " + EnumChatFormatting.GOLD + nf.format(total) + "\n" +
														EnumChatFormatting.AQUA.toString()+EnumChatFormatting.STRIKETHROUGH.toString() + " " + EnumChatFormatting.BOLD + "-------------------"));
		}).start();
	}

	public static String percentOf(Double num,Double OutOf) {
		double lowPercent = num/OutOf;
		double percent = Math.floor(lowPercent*10000)/100;
		return ChatFormatting.GRAY+" ("+percent+"%)";
	}
	public static String percentOf(Integer num,Double OutOf) {
		Double lowPercent = num.doubleValue()/OutOf.doubleValue();
		Double percent = Math.floor(lowPercent*10000)/100;
		return ChatFormatting.GRAY+" ("+percent+"%)";
	}
	public static String percentOf(Integer num,Integer OutOf) {
		Double lowPercent = num.doubleValue()/OutOf.doubleValue();
		Double percent = Math.floor(lowPercent*10000)/100;
		return ChatFormatting.GRAY+" ("+percent+"%)";
	}
	/*
	String profileURL = "https://soopy.dev/api/v2/player_skyblock/"+uuid+"?networth=true";
	System.out.println("Fetching profile... "+profileURL);
	JsonObject profileResponse = APIUtil.getJSONResponse(profileURL);
	if (profileResponse.has("error")) {
		String reason = profileResponse.get("error").getAsString();
		player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Failed with reason: " + reason));
		return;
	}
	System.out.println(profileResponse);
	String latestProfile = profileResponse.get("data").getAsJsonObject().get("stats").getAsJsonObject().get("currentProfileId").getAsString();
	
	System.out.println("Player Data ");
	JsonObject playerJson = profileResponse.get("data").getAsJsonObject().get("profiles").getAsJsonObject().get(latestProfile).getAsJsonObject().get("members").getAsJsonObject().get(uuid).getAsJsonObject();
	JsonObject networthJson = playerJson.get("skyhelperNetworth").getAsJsonObject();
	JsonObject catagories = networthJson.get("categories").getAsJsonObject();
	System.out.println("Got networth player data");
	NumberFormat nf = NumberFormat.getIntegerInstance(Locale.US);
	double coins = catagories.get("coins").getAsDouble();
	double Armor = catagories.get("armor").getAsDouble();
	double Equipment = catagories.get("equipment").getAsDouble();
	double Wardrobe = catagories.get("wardrobe").getAsDouble();
	double Inventory = catagories.get("inventory").getAsDouble();
	double enderchest = catagories.get("enderchest").getAsDouble();
	double accessories = catagories.get("accessories").getAsDouble();
	double personal_vault = catagories.get("personal_vault").getAsDouble();
	double storage = catagories.get("storage").getAsDouble();
	double Sacks = catagories.get("sacks").getAsDouble();
	double essence = catagories.get("essence").getAsDouble();
	double pets = catagories.get("pets").getAsDouble();

	double total = networthJson.get("total").getAsDouble();

	player.addChatMessage(new ChatComponentText(EnumChatFormatting.AQUA.toString()+EnumChatFormatting.STRIKETHROUGH.toString() + "" + EnumChatFormatting.BOLD + "-------------------\n" +
												EnumChatFormatting.AQUA + " " + username + "'s Networth:\n" +
												EnumChatFormatting.GREEN + " Coins: " + EnumChatFormatting.GOLD + nf.format(coins) +percentOf(coins,total)+ "\n" +
												EnumChatFormatting.GREEN + " Sacks: " + EnumChatFormatting.GOLD + nf.format(Sacks) +percentOf(Sacks,total)+ "\n" +
												EnumChatFormatting.GREEN + " Armor: " + EnumChatFormatting.GOLD + nf.format(Armor) +percentOf(Armor,total)+ "\n" +
												EnumChatFormatting.GREEN + " Equipment: " + EnumChatFormatting.GOLD + nf.format(Equipment) +percentOf(Equipment,total)+ "\n" +
												EnumChatFormatting.GREEN + " Wardrobe: " + EnumChatFormatting.GOLD + nf.format(Wardrobe) +percentOf(Wardrobe,total)+ "\n" +
												EnumChatFormatting.GREEN + " Inventory: " + EnumChatFormatting.GOLD + nf.format(Inventory) +percentOf(Inventory,total)+ "\n" +
												EnumChatFormatting.GREEN + " Enderchest: " + EnumChatFormatting.GOLD + nf.format(enderchest) +percentOf(enderchest,total)+ "\n" +
												EnumChatFormatting.GREEN + " Accessories: " + EnumChatFormatting.GOLD + nf.format(accessories) +percentOf(accessories,total)+ "\n" +
												EnumChatFormatting.GREEN + " Essence: " + EnumChatFormatting.GOLD + nf.format(essence) +percentOf(essence,total)+ "\n" +
												EnumChatFormatting.GREEN + " Vault: " + EnumChatFormatting.GOLD + nf.format(personal_vault) +percentOf(personal_vault,total)+ "\n" +
												EnumChatFormatting.GREEN + " Storage: " + EnumChatFormatting.GOLD + nf.format(storage) +percentOf(storage,total)+ "\n" +
												EnumChatFormatting.GREEN + " Pets: " + EnumChatFormatting.GOLD + nf.format(pets) +percentOf(pets,total)+ "\n" +
												EnumChatFormatting.GREEN + " Total Networth: " + EnumChatFormatting.GOLD + nf.format(total) + "\n" +
												EnumChatFormatting.AQUA.toString()+EnumChatFormatting.STRIKETHROUGH.toString() + " " + EnumChatFormatting.BOLD + "-------------------"));
	*/
}
