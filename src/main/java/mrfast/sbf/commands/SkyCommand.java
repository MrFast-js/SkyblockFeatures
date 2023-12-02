package mrfast.sbf.commands;

import java.awt.Desktop;
import java.net.URI;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.utils.Utils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class SkyCommand extends CommandBase {

	@Override
    public String getCommandName() {
        return "sky";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/sky [username]";
    }

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}
	
	@Override
	public void processCommand(ICommandSender arg0, String[] arg1) throws CommandException {
        String username;
        if (arg1.length == 0) {
            username = Utils.GetMC().thePlayer.getName();
        } else {
            username = arg1[0];
        }

        try {
            Desktop.getDesktop().browse(new URI("https://sky.shiiyu.moe/stats/"+username));
        } catch (Exception e) {
            e.printStackTrace();
        }

        Utils.sendMessage(ChatFormatting.LIGHT_PURPLE+"Opening "+username+"'s Skycrypt stats");
	}
}