package mrfast.sbf.commands;

import gg.essential.api.commands.Command;
import gg.essential.api.commands.DefaultHandler;
import gg.essential.api.utils.GuiUtil;
import mrfast.sbf.features.items.ViewModel;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class ViewModelCommand extends Command {

    public ViewModelCommand() {
        super("vm");
    }
    
    @DefaultHandler
    public void handle() {
        GuiUtil.open(Objects.requireNonNull(new ViewModel.ViewModelScreen()));
    }

    @Nullable
    @Override
    public Set<Alias> getCommandAliases() {
        return Collections.singleton(new Alias("vm"));
    }
}