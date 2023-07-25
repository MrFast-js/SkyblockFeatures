package mrfast.skyblockfeatures.commands;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;

import gg.essential.api.utils.GuiUtil;
import mrfast.skyblockfeatures.SkyblockFeatures;
import mrfast.skyblockfeatures.utils.APIUtils;
import mrfast.skyblockfeatures.utils.Utils;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;

public class InventoryCommand extends CommandBase {

	@Override
	public String getCommandName() {
		return "inventory";
	}

	@Override
	public String getCommandUsage(ICommandSender arg0) {
		return "/inventory [name]";
	}

	@Override
    public List<String> getCommandAliases() {
        return Collections.singletonList("inv");
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
	HashMap<String, List<String>> itemLores = new HashMap<String, List<String>>();
	@Override
	public void processCommand(ICommandSender arg0, String[] arg1) throws CommandException {
		InventoryBasic TargetInventory = new InventoryBasic(ChatFormatting.GREEN+"✯ "+arg1[0]+"'s Inventory", true, 54);

		new Thread(() -> {
			EntityPlayer player = (EntityPlayer) arg0;
			
			// Check key
			String key = SkyblockFeatures.config.apiKey;
			if (key.equals("")) {
				Utils.SendMessage(EnumChatFormatting.RED + "API key not set. Use /setkey.");
			}
			
			// Get UUID for Hypixel API requests
			String username;
			String uuid;
			if (arg1.length == 0) {
				username = player.getName();
				uuid = player.getUniqueID().toString().replaceAll("[\\-]", "");
			} else {
				username = arg1[0];
				uuid = APIUtils.getUUID(username);
			}
			Utils.SendMessage(EnumChatFormatting.GREEN + "Checking inventory of " + EnumChatFormatting.DARK_GREEN + username);
			
			// Find stats of latest profile
			String latestProfile = APIUtils.getLatestProfileID(uuid, key);
			if (latestProfile == null) return;

			String profileURL = "https://api.hypixel.net/skyblock/profile?profile=" + latestProfile;
			System.out.println("Fetching profile...");
			JsonObject profileResponse = APIUtils.getJSONResponse(profileURL);
			if(profileResponse.has("cause")) {
				String reason = profileResponse.get("cause").getAsString();
				Utils.SendMessage(EnumChatFormatting.RED + "Failed with reason: " + reason);
				return;
			}
			
			for(int i = 0; i < 54; i++) {
				TargetInventory.setInventorySlotContents(i, new ItemStack(Blocks.stained_glass_pane, 1, 15).setStackDisplayName(ChatFormatting.RESET+""));
			}

			if(profileResponse.get("profile").getAsJsonObject().get("members").getAsJsonObject().get(uuid).getAsJsonObject().has("inv_contents")) {
				String inventoryBase64 = profileResponse.get("profile").getAsJsonObject().get("members").getAsJsonObject().get(uuid).getAsJsonObject().get("inv_contents").getAsJsonObject().get("data").getAsString();
				Inventory items = new Inventory(inventoryBase64);
				List<ItemStack> a = decodeItem(items,true);

				int index = 8;
				for(ItemStack item: a) {
					if(index > 34 && index <= 44)
						index+=10;
					else index+=1;

					TargetInventory.setInventorySlotContents(index, item);
				}
			} else {
				ItemStack noApi = new ItemStack(Blocks.stained_glass_pane, 1, 14).setStackDisplayName(ChatFormatting.RED+"This Player Has Their API Disabled");
				TargetInventory.setInventorySlotContents(31, noApi);
			}

			if(profileResponse.get("profile").getAsJsonObject().get("members").getAsJsonObject().get(uuid).getAsJsonObject().has("inv_armor")) {
				String inventoryBase64 = profileResponse.get("profile").getAsJsonObject().get("members").getAsJsonObject().get(uuid).getAsJsonObject().get("inv_armor").getAsJsonObject().get("data").getAsString();
				Inventory items = new Inventory(inventoryBase64);
				List<ItemStack> a = decodeItem(items,true);

				int index = 0;
				for(ItemStack item: a) {
					index+=1;
					// Leggings
					if(index==1) TargetInventory.setInventorySlotContents(3, item);
					// Chestplate
					if(index==2) TargetInventory.setInventorySlotContents(5, item);
					// Helmet
					if(index==3) TargetInventory.setInventorySlotContents(6, item);
					// Boots
					if(index==4) TargetInventory.setInventorySlotContents(2, item);
				}
			}

			GuiUtil.open(Objects.requireNonNull(new GuiChest(Utils.GetMC().thePlayer.inventory, TargetInventory)));
		}).start();
	}

	public static class Inventory
    {
        private final String data;

        public Inventory(String data)
        {
            this.data = data;
        }

        public String getData()
        {
            return this.data.replace("\\u003d", "=");
        }
    }

	public static List<ItemStack> decodeItem(Inventory inventory,Boolean offset)
    {
        if (inventory != null)
        {
            List<ItemStack> itemStack = new ArrayList<>();
            byte[] decode = Base64.getDecoder().decode(inventory.getData());

            try
            {
                NBTTagCompound compound = CompressedStreamTools.readCompressed(new ByteArrayInputStream(decode));
                NBTTagList list = compound.getTagList("i", 10);

                for (int i = 0; i < list.tagCount(); ++i)
                {
                    itemStack.add(ItemStack.loadItemStackFromNBT(list.getCompoundTagAt(i)));
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
			if(offset) Collections.rotate(itemStack, -9);

            return itemStack;
        }
        else
        {
            List<ItemStack> itemStack = new ArrayList<>();
            ItemStack barrier = new ItemStack(Blocks.barrier);
            barrier.setStackDisplayName(EnumChatFormatting.RESET + "" + EnumChatFormatting.RED + "Item is not available!");

            for (int i = 0; i < 36; ++i)
            {
                itemStack.add(barrier);
            }
            return itemStack;
        }
    }
}
