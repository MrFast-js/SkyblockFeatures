package mrfast.skyblockfeatures.commands;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.mojang.realmsclient.gui.ChatFormatting;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import mrfast.skyblockfeatures.utils.APIUtil;
import mrfast.skyblockfeatures.utils.Utils;

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
        String username = "";
        if (arg1.length == 0) {
            username = Utils.GetMC().thePlayer.getName();
        } else {
            username = arg1[0];
        }
        String uuid = APIUtil.getUUID(username);
        try {
            Desktop.getDesktop().browse(new URI("https://sky.coflnet.com/player/"+uuid+"/flips"));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Utils.SendMessage(ChatFormatting.LIGHT_PURPLE+"Opening "+username+"'s Flipper stats");
	}
}