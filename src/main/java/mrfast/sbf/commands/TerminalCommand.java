package mrfast.sbf.commands;

import java.util.*;

import com.mojang.realmsclient.gui.ChatFormatting;
import mrfast.sbf.events.SlotClickedEvent;
import mrfast.sbf.features.termPractice.TerminalManager;
import mrfast.sbf.utils.GuiUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.block.BlockStainedGlassPane;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class TerminalCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "terminal";
    }

    @Override
    public String getCommandUsage(ICommandSender arg0) {
        return "/terminal";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender arg0, String[] arg1) throws CommandException {
        int termId = Utils.randomNumber(1, 4);
        if(arg1.length>0) {
            try {
                termId = Integer.parseInt(arg1[0].replaceAll("[^0-9]", ""));
            } catch (Exception ignored) {}
        }
        GuiChest terminal = TerminalManager.createTerminal(termId);
        if (terminal != null) {
            GuiUtils.openGui(terminal);
        }
    }
}
