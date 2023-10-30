package mrfast.sbf.commands;

import com.google.common.collect.Lists;
import mrfast.sbf.features.dungeons.Reparty;
import mrfast.sbf.utils.Utils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

import java.util.List;

public class RepartyCommand extends CommandBase {

	@Override
	public String getCommandName() {
		return "reparty";
	}
	
	@Override
	public List<String> getCommandAliases() {
        return Lists.newArrayList("sfrp","rp");
    }

	@Override
	public String getCommandUsage(ICommandSender arg0) {
		return "/" + getCommandName() + " [name]";
	}

	public static String usage(ICommandSender arg0) {
		return new RepartyCommand().getCommandUsage(arg0);
	}

	@Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return (args.length >= 1) ? getListOfStringsMatchingLastWord(args, Utils.getListOfPlayerUsernames()) : null;
    }

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}
	
	@Override
	public void processCommand(ICommandSender arg0, String[] arg1) throws CommandException {
		Reparty.doReparty();
	}

}
