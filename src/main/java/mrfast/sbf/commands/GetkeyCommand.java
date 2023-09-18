package mrfast.sbf.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.utils.Utils;

public class GetkeyCommand extends CommandBase {

	@Override
	public String getCommandName() {
		return "key";
	}

	@Override
	public String getCommandUsage(ICommandSender arg0) {
		return "/" + getCommandName();
	}

	public static String usage(ICommandSender arg0) {
		return new GetkeyCommand().getCommandUsage(arg0);
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	@Override
	public void processCommand(ICommandSender arg0, String[] arg1) throws CommandException {
	    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	    StringSelection stringSelection = new StringSelection(SkyblockFeatures.config.apiKey);
	    
	    if (SkyblockFeatures.config.apiKey.equals("")) {
			Utils.SendMessage(EnumChatFormatting.RED + "API key not set. Set your API key using /setkey.");
	    }
	    
	    clipboard.setContents(stringSelection, null);
		Utils.SendMessage(EnumChatFormatting.GREEN + "Your set API key is " + EnumChatFormatting.DARK_GREEN + SkyblockFeatures.config.apiKey);
	}

}
