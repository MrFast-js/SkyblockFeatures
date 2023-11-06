package mrfast.sbf.commands;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.utils.Utils;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;

public class getNbtCommand extends CommandBase {

	@Override
    public String getCommandName() {
        return "getnbt";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/getnbt";
    }

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	@Override
	public void processCommand(ICommandSender arg0, String[] args) throws CommandException {
		copyMobData(Utils.GetMC().thePlayer, Utils.GetMC().theWorld.loadedEntityList);
	}

    public static final int ENTITY_COPY_RADIUS = 3;

    public static void copyMobData(EntityPlayerSP player, List<Entity> loadedEntities) {
        List<Entity> loadedEntitiesCopy = new LinkedList<>(loadedEntities);
        ListIterator<Entity> loadedEntitiesCopyIterator;
        StringBuilder stringBuilder = new StringBuilder();

        // We only care about mobs.
        loadedEntitiesCopy.removeIf(entity -> entity.getDistanceToEntity(player) > ENTITY_COPY_RADIUS);

        loadedEntitiesCopyIterator = loadedEntitiesCopy.listIterator();

        // Copy the NBT data from the loaded entities.
        while (loadedEntitiesCopyIterator.hasNext()) {
            Entity entity = loadedEntitiesCopyIterator.next();
            NBTTagCompound entityData = new NBTTagCompound();

            stringBuilder.append("Class: ").append(entity.getClass().getSimpleName()).append(System.lineSeparator());
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
        List<TileEntity> loadedTileEntitiesCopy = new LinkedList<>(Utils.GetMC().theWorld.loadedTileEntityList);
        ListIterator<TileEntity> loadedTileEntitiesCopyIterator;

        // We only care about mobs.
        loadedTileEntitiesCopy.removeIf(entity -> player.getPosition().distanceSq(entity.getPos()) > ENTITY_COPY_RADIUS*2);

        loadedTileEntitiesCopyIterator = loadedTileEntitiesCopy.listIterator();
        stringBuilder.append("------------------------------ Tile Entitys ------------------------------\n");
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

        copyStringToClipboard(stringBuilder.toString(), ChatFormatting.GREEN + "Entity data was copied to clipboard!");
    }

    public static void copyStringToClipboard(String string, String successMessage) {
        writeToClipboard(string, successMessage);
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

    // Internal methods
    private static void writeToClipboard(String text, String successMessage) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection output = new StringSelection(text);

        try {
            clipboard.setContents(output, output);
            Utils.SendMessage(successMessage);
        } catch (IllegalStateException exception) {
            Utils.SendMessage(ChatFormatting.RED+"Clipboard not available.");
        }
    }
}
