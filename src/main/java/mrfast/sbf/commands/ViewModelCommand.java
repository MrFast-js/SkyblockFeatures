package mrfast.sbf.commands;

import java.util.Objects;

import mrfast.sbf.features.items.ViewModel;
import mrfast.sbf.utils.Utils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class ViewModelCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "vm";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/vm";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender arg0, String[] arg1) throws CommandException {
        Utils.openGui(new ViewModel.ViewModelScreen());
    }
}