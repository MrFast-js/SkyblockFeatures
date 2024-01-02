package mrfast.sbf.commands;

import mrfast.sbf.core.SkyblockInfo;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class PingCommand extends CommandBase {

	@Override
    public String getCommandName() {
        return "ping";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/ping";
    }

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	@Override
	public void processCommand(ICommandSender arg0, String[] args) throws CommandException {
        SkyblockInfo.getPing();
	}
}
