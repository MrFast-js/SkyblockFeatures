package mrfast.sbf.commands;

import java.awt.Desktop;
import java.net.URI;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.utils.APIUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class FlipsCommand extends CommandBase {

	@Override
    public String getCommandName() {
        return "flips";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/flips [username]";
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
        String uuid = APIUtils.getUUID(username);
        try {
            Desktop.getDesktop().browse(new URI("https://sky.coflnet.com/player/"+uuid+"/flips"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        Utils.sendMessage(ChatFormatting.LIGHT_PURPLE+"Opening "+username+"'s Flipper stats");
	}
}