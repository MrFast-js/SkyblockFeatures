package mrfast.sbf.commands;

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
import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.utils.APIUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;

public class AccessoriesCommand extends CommandBase {

	@Override
	public String getCommandName() {
		return "accessories";
	}

	@Override
	public String getCommandUsage(ICommandSender arg0) {
		return "/accessories [name]";
	}

	@Override
    public List<String> getCommandAliases() {
        return Collections.singletonList("acc");
    }

	@Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return (args.length >= 1) ? getListOfStringsMatchingLastWord(args, Utils.getListOfPlayerUsernames()) : null;
    }
	
	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}
	HashMap<String, List<String>> itemLores = new HashMap<String, List<String>>();
	@Override
	public void processCommand(ICommandSender arg0, String[] arg1) throws CommandException {
		String title = ChatFormatting.GREEN+"✯ "+arg1[0]+"'s Accessory Bag";
		if(title.length() > 30) title = title.substring(0, 30);
		InventoryBasic TargetInventory = new InventoryBasic(title, true, 54);

		new Thread(() -> {
			// Check key
			String key = SkyblockFeatures.config.apiKey;
			if (key.equals("")) {
				Utils.SendMessage(EnumChatFormatting.RED + "API key not set. Use /setkey.");
			}
			
			// Get UUID for Hypixel API requests
			String username;
			String uuid;
			username = arg1[0] != null?arg1[0]:Utils.GetMC().thePlayer.getName();
			Utils.SendMessage(EnumChatFormatting.GREEN + "Checking Accessory Bag of " + EnumChatFormatting.DARK_GREEN + username);
			uuid = APIUtils.getUUID(username);
			
			// Find stats of latest profile
			String latestProfile = APIUtils.getLatestProfileID(uuid, key);
			if (latestProfile == null) return;

			String profileURL = "https://api.hypixel.net/skyblock/profile?profile=" + latestProfile;
			System.out.println("Fetching profile...");
			JsonObject profileResponse = APIUtils.getJSONResponse(profileURL);
			System.out.println(profileResponse.toString()+"    "+profileURL);
			if(profileResponse.toString().equals("{}")) {
				Utils.SendMessage(EnumChatFormatting.RED + "Hypixel API is having problems!");
				return;
			}
			
			for(int i = 0; i < 54; i++) {
				TargetInventory.setInventorySlotContents(i, new ItemStack(Blocks.stained_glass_pane, 1, 15).setStackDisplayName(ChatFormatting.RESET+""));
			}

			if(profileResponse.get("profile").getAsJsonObject().get("members").getAsJsonObject().get(uuid).getAsJsonObject().has("talisman_bag")) {
				String inventoryBase64 = profileResponse.get("profile").getAsJsonObject().get("members").getAsJsonObject().get(uuid).getAsJsonObject().get("talisman_bag").getAsJsonObject().get("data").getAsString();
				Inventory items = new Inventory(inventoryBase64);
				List<ItemStack> a = decodeItem(items);
				int index = 0;
				for(ItemStack item: a) {
					if(item==null) continue;
					if(index<53) {
						TargetInventory.setInventorySlotContents(index, item);
						index++;
					}
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

	public static List<ItemStack> decodeItem(Inventory inventory)
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

            Collections.rotate(itemStack, -9);

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
