package mrfast.skyblockfeatures.commands;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class ShrugCommand extends CommandBase {

	@Override
    public String getCommandName() {
        return "shrug";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/shrug [message]";
    }

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}
	
	@Override
	public void processCommand(ICommandSender arg0, String[] args) throws CommandException {
		String chatMsg = "¯\\_(ツ)_/¯";
        if (args.length > 0) chatMsg = String.join(" ", args) + " " + chatMsg;
        Minecraft.getMinecraft().thePlayer.sendChatMessage(chatMsg);
	}
}
