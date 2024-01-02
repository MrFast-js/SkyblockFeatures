package mrfast.sbf.commands;

import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class ColorTestCommand extends CommandBase {

	@Override
    public String getCommandName() {
        return "colortest";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/colortest [message]";
    }

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}
	
	@Override
	public void processCommand(ICommandSender arg0, String[] args) throws CommandException {
        Utils.sendMessage(String.join(" ", args).replaceAll("&","§"));
	}
}
