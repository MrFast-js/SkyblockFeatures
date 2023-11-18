package mrfast.sbf.commands;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.utils.ScoreboardUtil;
import mrfast.sbf.utils.TabListUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.util.Constants;

public class debugCommand extends CommandBase {

	@Override
    public String getCommandName() {
        return "debug";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/debug";
    }
    static List<String> arguments = Arrays.asList("mobs","tiles","entities","item","sidebar","tab");
    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return arguments;
    }

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	@Override
	public void processCommand(ICommandSender arg0, String[] args) throws CommandException {
        if(args.length==0) {
            invalidUsage();
            return;
        }

        int dist = 5;
        try {
            if(args.length>1) {
                dist = Integer.parseInt(args[1].replaceAll("[^0-9]",""));
            }
        } catch (Exception ignored) {

        }

        switch (args[0]) {
            case "mobs":
                getMobData(false,true,dist);
                break;
            case "tiles":
                getMobData(true,false,dist);
                break;
            case "entities":
                getMobData(true,true,dist);
                break;
            case "item":
                ItemStack heldItem = Utils.GetMC().thePlayer.getHeldItem();
                if(heldItem != null) {
                    getItemData(heldItem);
                } else {
                    Utils.SendMessage(ChatFormatting.RED + "You must be holding an item!");
                }
                break;
            case "sidebar":
                getSidebarData();
                break;
            case "tablist":
            case "tab":
                getTablistData();
                break;
            default:
                invalidUsage();
                break;
        }
	}

    public static void invalidUsage() {
        StringBuilder usage = new StringBuilder(ChatFormatting.RED + "Invalid Usage! " + ChatFormatting.YELLOW + "/debug ");
        for(String arg:arguments) {
            usage.append(arg).append(" ");
        }
        Utils.SendMessage(usage.toString());
    }

    public static void getSidebarData() {
        StringBuilder output = new StringBuilder();
        int count = 0;
        List<String> lines = ScoreboardUtil.getSidebarLines();

        Collections.reverse(lines);

        lines.add("\n\n");
        for(String line:new ArrayList<>(lines)) {
            lines.add(Utils.cleanColor(line).replaceAll("[^\\x00-\\x7F]", ""));
        }

        for (String line : lines) {
            count++;
            output.append(count).append(": ").append(line).append("\n");
        }

        uploadData(output.toString());
    }
    public static void getTablistData() {
        StringBuilder output = new StringBuilder();
        int count = 0;
        for (NetworkPlayerInfo pi : TabListUtils.getTabEntries()) {
            count++;
            output.append(count).append(": ").append(Utils.GetMC().ingameGUI.getTabList().getPlayerName(pi)).append("\n");
        }
        uploadData(output.toString());
    }

    public static void getItemData(ItemStack item) {
        uploadData(prettyPrintNBT(item.serializeNBT()));
    }

    public static void getMobData(boolean tileEntities, boolean mobs, int distance) {
        EntityPlayerSP player = Utils.GetMC().thePlayer;
        StringBuilder stringBuilder = new StringBuilder();

        if(mobs) {
            stringBuilder.append(copyMobEntities(player,distance));
        }
        if(tileEntities) {
            stringBuilder.append(copyTileEntities(player,distance));
        }

        uploadData(stringBuilder.toString());
    }

    public static String copyMobEntities(EntityPlayerSP player,int distance) {
        StringBuilder stringBuilder = new StringBuilder();
        List<Entity> loadedEntitiesCopy = new LinkedList<>(Utils.GetMC().theWorld.loadedEntityList);
        ListIterator<Entity> loadedEntitiesCopyIterator;

        loadedEntitiesCopy.removeIf(entity -> entity.getDistanceToEntity(player) > distance);
        loadedEntitiesCopyIterator = loadedEntitiesCopy.listIterator();

        // Copy the NBT data from the loaded entities.
        while (loadedEntitiesCopyIterator.hasNext()) {
            Entity entity = loadedEntitiesCopyIterator.next();
            NBTTagCompound entityData = new NBTTagCompound();
            if(entity.equals(player)) continue;

            stringBuilder.append("Class: ").append(entity.getClass().getSimpleName()).append(System.lineSeparator());
            stringBuilder.append("ID: ").append(entity.getEntityId()).append(System.lineSeparator());
            if (entity.hasCustomName() || EntityPlayer.class.isAssignableFrom(entity.getClass())) {
                stringBuilder.append("Name: ").append(entity.getName()).append(System.lineSeparator());
            }

            stringBuilder.append("NBT Data:").append(System.lineSeparator());
            entity.writeToNBT(entityData);
            stringBuilder.append(prettyPrintNBT(entityData));

            // Add spacing if necessary.
            if (loadedEntitiesCopyIterator.hasNext()) {
                stringBuilder.append(System.lineSeparator()).append(System.lineSeparator());
            }
        }
        return stringBuilder.toString();
    }

    public static String copyTileEntities(EntityPlayerSP player,int distance) {
        StringBuilder stringBuilder = new StringBuilder();

        List<TileEntity> loadedTileEntitiesCopy = new LinkedList<>(Utils.GetMC().theWorld.loadedTileEntityList);
        ListIterator<TileEntity> loadedTileEntitiesCopyIterator;

        loadedTileEntitiesCopy.removeIf(entity -> player.getPosition().distanceSq(entity.getPos()) > Math.pow(distance,2));
        loadedTileEntitiesCopyIterator = loadedTileEntitiesCopy.listIterator();

        // Copy the NBT data from the loaded entities.
        while (loadedTileEntitiesCopyIterator.hasNext()) {
            TileEntity entity = loadedTileEntitiesCopyIterator.next();
            NBTTagCompound entityData = new NBTTagCompound();

            stringBuilder.append("Class: ").append(entity.getClass().getSimpleName()).append(System.lineSeparator());

            stringBuilder.append("NBT Data:").append(System.lineSeparator());
            entity.writeToNBT(entityData);
            stringBuilder.append(prettyPrintNBT(entityData));

            // Add spacing if necessary.
            if (loadedTileEntitiesCopyIterator.hasNext()) {
                stringBuilder.append(System.lineSeparator()).append(System.lineSeparator());
            }
        }
        return stringBuilder.toString();
    }
    
    public static String prettyPrintNBT(NBTBase nbt) {
        final String INDENT = "    ";

        int tagID = nbt.getId();
        StringBuilder stringBuilder = new StringBuilder();

        // Determine which type of tag it is.
        if (tagID == Constants.NBT.TAG_END) {
            stringBuilder.append('}');

        } else if (tagID == Constants.NBT.TAG_BYTE_ARRAY || tagID == Constants.NBT.TAG_INT_ARRAY) {
            stringBuilder.append('[');
            if (tagID == Constants.NBT.TAG_BYTE_ARRAY) {
                NBTTagByteArray nbtByteArray = (NBTTagByteArray) nbt;
                byte[] bytes = nbtByteArray.getByteArray();

                for (int i = 0; i < bytes.length; i++) {
                    stringBuilder.append(bytes[i]);

                    // Don't add a comma after the last element.
                    if (i < (bytes.length - 1)) {
                        stringBuilder.append(", ").append(System.lineSeparator());
                    }
                }
            } else {
                NBTTagIntArray nbtIntArray = (NBTTagIntArray) nbt;
                int[] ints = nbtIntArray.getIntArray();

                for (int i = 0; i < ints.length; i++) {
                    stringBuilder.append(ints[i]);

                    // Don't add a comma after the last element.
                    if (i < (ints.length - 1)) {
                        stringBuilder.append(", ").append(System.lineSeparator());
                    }
                }
            }
            stringBuilder.append(']');

        } else if (tagID == Constants.NBT.TAG_LIST) {
            NBTTagList nbtTagList = (NBTTagList) nbt;

            stringBuilder.append('[');
            for (int i = 0; i < nbtTagList.tagCount(); i++) {
                NBTBase currentListElement = nbtTagList.get(i);

                stringBuilder.append(prettyPrintNBT(currentListElement));

                // Don't add a comma after the last element.
                if (i < (nbtTagList.tagCount() - 1)) {
                    stringBuilder.append(", ").append(System.lineSeparator());
                }
            }
            stringBuilder.append(']');

        } else if (tagID == Constants.NBT.TAG_COMPOUND) {
            NBTTagCompound nbtTagCompound = (NBTTagCompound) nbt;

            stringBuilder.append('{');
             if (!nbtTagCompound.hasNoTags()) {
                Iterator<String> iterator = nbtTagCompound.getKeySet().iterator();

                stringBuilder.append(System.lineSeparator());

                while (iterator.hasNext()) {
                    String key = iterator.next();
                    NBTBase currentCompoundTagElement = nbtTagCompound.getTag(key);

                    stringBuilder.append(key).append(": ").append(prettyPrintNBT(currentCompoundTagElement));

                    // Don't add a comma after the last element.
                    if (iterator.hasNext()) {
                        stringBuilder.append(",").append(System.lineSeparator());
                    }
                }

                // Indent all lines
                String indentedString = stringBuilder.toString().replaceAll(System.lineSeparator(), System.lineSeparator() + INDENT);
                stringBuilder = new StringBuilder(indentedString);
            }

            stringBuilder.append(System.lineSeparator()).append('}');
        }
        // This includes the tags: byte, short, int, long, float, double, and string
        else {
            stringBuilder.append(nbt);
        }

        return stringBuilder.toString();
    }

    private static void uploadData(String text) {
        new Thread(()-> {
            try {
                URL url = new URL("https://hst.sh/documents");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "text/plain");
                connection.setRequestProperty("User-Agent", "Insomnia/2023.5.7");
                connection.setDoOutput(true);

                try (OutputStream os = connection.getOutputStream()) {
                    os.write(text.getBytes());
                }

                int responseCode = connection.getResponseCode();
                String hostUrl = "";
                if (responseCode == 200) {
                    InputStream is = connection.getInputStream();
                    byte[] responseBody = new byte[is.available()];
                    is.read(responseBody);
                    String out = new String(responseBody);
                    JsonObject json = new Gson().fromJson(out, JsonObject.class);
                    hostUrl = "https://hst.sh/raw/" + json.get("key").getAsString();
                } else {
                    System.out.println("Request failed with code " + responseCode);
                }

                IChatComponent message = new ChatComponentText(ChatFormatting.GREEN + "Succesfully uploaded debug data!" + ChatFormatting.GOLD + ChatFormatting.BOLD + " Click here to open");
                message.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, hostUrl));
                Utils.SendMessage(message);

                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
