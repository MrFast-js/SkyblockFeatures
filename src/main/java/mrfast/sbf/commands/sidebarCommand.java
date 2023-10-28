package mrfast.sbf.commands;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.utils.ScoreboardUtil;
import mrfast.sbf.utils.TabListUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class sidebarCommand extends CommandBase {

	@Override
    public String getCommandName() {
        return "sidebar";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/sidebar";
    }

    @Override
    public List<String> getCommandAliases() {
        return Collections.singletonList("sidebar");
    }

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	@Override
	public void processCommand(ICommandSender arg0, String[] args) throws CommandException {
        StringBuilder output = new StringBuilder("----------===== TAB LIST =====----------\n\n");
        int count = 0;
		for (NetworkPlayerInfo pi : TabListUtils.getTabEntries()) {
            count++;
            output.append(count).append(": ").append(Utils.GetMC().ingameGUI.getTabList().getPlayerName(pi)).append("\n");
        }
        output.append("\n----------===== SIDEBAR =====----------\n\n");
        count = 0;
        List<String> lines = ScoreboardUtil.getSidebarLines();
        Collections.reverse(lines);
        for (String line : lines) {
            count++;
            output.append(count).append(": ").append(line).append("\n");
        }
        GuiScreen.setClipboardString(output.toString());
        Utils.SendMessage(ChatFormatting.GREEN+"Scoreboards copied to clipboard");
	}

    public static <T> T getRandomElement(T[] arr){
        return arr[ThreadLocalRandom.current().nextInt(arr.length)];
    }
}
