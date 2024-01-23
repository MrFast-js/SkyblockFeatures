package mrfast.sbf.commands;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.core.SkyblockInfo;
import mrfast.sbf.features.DeveloperFeatures;
import mrfast.sbf.utils.NetworkUtils;
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

public class DebugCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "debug";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/debug";
    }

    static List<String> arguments = Arrays.asList("mobs", "tiles", "entities", "item", "sidebar", "tab");

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
        if (args.length == 0) {
            invalidUsage();
            return;
        }

        int dist = 5;
        try {
            if (args.length > 1) {
                dist = Integer.parseInt(args[1].replaceAll("[^0-9]", ""));
            }
        } catch (Exception ignored) {

        }

        switch (args[0]) {
            case "mobs":
                getMobData(false, true, dist);
                break;
            case "tiles":
                getMobData(true, false, dist);
                break;
            case "sock":
                NetworkUtils.setupSocket();
                break;
            case "location":
            case "loc":
                Utils.sendMessage(ChatFormatting.GRAY + "Local:'" + SkyblockInfo.localLocation + "' Map:" + SkyblockInfo.map + " Location:'" + SkyblockInfo.location + "'");
                break;
            case "entities":
                getMobData(true, true, dist);
                break;
            case "item":
                ItemStack heldItem = Utils.GetMC().thePlayer.getHeldItem();
                if (heldItem != null) {
                    getItemData(heldItem);
                } else {
                    Utils.sendMessage(ChatFormatting.RED + "You must be holding an item!");
                }
                break;
            case "sidebar":
                getSidebarData();
                break;
            case "log":
                uploadLog();
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
        for (String arg : arguments) {
            usage.append(arg).append(" ");
        }
        Utils.sendMessage(usage.toString());
    }

    public static void getSidebarData() {
        StringBuilder output = new StringBuilder();
        List<String> lines = ScoreboardUtil.getSidebarLines(true);
        lines.add("==== Raw ====");
        lines.addAll(ScoreboardUtil.getSidebarLines(false));

        for (String line : lines) {
            output.append(line).append("\n");
        }

        Utils.copyToClipboard(output.toString());
    }

    public static void getTablistData() {
        StringBuilder output = new StringBuilder();
        int count = 0;
        for (NetworkPlayerInfo pi : TabListUtils.getTabEntries()) {
            count++;
            output.append(count).append(": ").append(Utils.GetMC().ingameGUI.getTabList().getPlayerName(pi)).append("\n");
        }
        Utils.copyToClipboard(output.toString());
    }

    public static void uploadLog() {
        File log = new File(new File(Utils.GetMC().mcDataDir, "logs"), "latest.log");
        try {
            List<String> lines = Files.readAllLines(log.toPath(), StandardCharsets.UTF_8);
            Utils.copyToClipboard(lines.stream().collect(Collectors.joining(System.lineSeparator())));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getItemData(ItemStack item) {
        Utils.copyToClipboard(DeveloperFeatures.prettyPrintNBTtoString(item.serializeNBT()));
    }

    public static void getMobData(boolean tileEntities, boolean mobs, int distance) {
        EntityPlayerSP player = Utils.GetMC().thePlayer;
        StringBuilder stringBuilder = new StringBuilder();

        if (mobs) {
            stringBuilder.append(copyMobEntities(player, distance));
        }
        if (tileEntities) {
            stringBuilder.append(copyTileEntities(player, distance));
        }

        Utils.copyToClipboard(stringBuilder.toString());
    }

    public static String copyMobEntities(EntityPlayerSP player, int distance) {
        StringBuilder stringBuilder = new StringBuilder();
        List<Entity> loadedEntitiesCopy = new LinkedList<>(Utils.GetMC().theWorld.loadedEntityList);
        ListIterator<Entity> loadedEntitiesCopyIterator;

        loadedEntitiesCopy.removeIf(entity -> entity.getDistanceToEntity(player) > distance);
        loadedEntitiesCopyIterator = loadedEntitiesCopy.listIterator();

        // Copy the NBT data from the loaded entities.
        while (loadedEntitiesCopyIterator.hasNext()) {
            Entity entity = loadedEntitiesCopyIterator.next();
            NBTTagCompound entityData = new NBTTagCompound();
            if (entity.equals(player)) continue;

            stringBuilder.append("Class: ").append(entity.getClass().getSimpleName()).append(System.lineSeparator());
            stringBuilder.append("ID: ").append(entity.getEntityId()).append(System.lineSeparator());
            if (entity.hasCustomName() || EntityPlayer.class.isAssignableFrom(entity.getClass())) {
                stringBuilder.append("Name: ").append(entity.getName()).append(System.lineSeparator());
            }

            stringBuilder.append("NBT Data:").append(System.lineSeparator());
            entity.writeToNBT(entityData);
            stringBuilder.append(DeveloperFeatures.prettyPrintNBTtoString(entityData));

            // Add spacing if necessary.
            if (loadedEntitiesCopyIterator.hasNext()) {
                stringBuilder.append(System.lineSeparator()).append(System.lineSeparator());
            }
        }
        return stringBuilder.toString();
    }

    public static String copyTileEntities(EntityPlayerSP player, int distance) {
        StringBuilder stringBuilder = new StringBuilder();

        List<TileEntity> loadedTileEntitiesCopy = new LinkedList<>(Utils.GetMC().theWorld.loadedTileEntityList);
        ListIterator<TileEntity> loadedTileEntitiesCopyIterator;

        loadedTileEntitiesCopy.removeIf(entity -> player.getPosition().distanceSq(entity.getPos()) > Math.pow(distance, 2));
        loadedTileEntitiesCopyIterator = loadedTileEntitiesCopy.listIterator();

        // Copy the NBT data from the loaded entities.
        while (loadedTileEntitiesCopyIterator.hasNext()) {
            TileEntity entity = loadedTileEntitiesCopyIterator.next();
            NBTTagCompound entityData = new NBTTagCompound();

            stringBuilder.append("Class: ").append(entity.getClass().getSimpleName()).append(System.lineSeparator());

            stringBuilder.append("NBT Data:").append(System.lineSeparator());
            entity.writeToNBT(entityData);
            stringBuilder.append(DeveloperFeatures.prettyPrintNBTtoString(entityData));

            // Add spacing if necessary.
            if (loadedTileEntitiesCopyIterator.hasNext()) {
                stringBuilder.append(System.lineSeparator()).append(System.lineSeparator());
            }
        }
        return stringBuilder.toString();
    }

}
