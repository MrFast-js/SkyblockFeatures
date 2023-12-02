package mrfast.sbf.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

import java.util.HashMap;
import java.util.Map;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.utils.Utils;

public class DungeonsCommand extends CommandBase {

	private static final Map<Integer, String> numberWords = new HashMap<>();
    static {
        numberWords.put(1, "ONE");
        numberWords.put(2, "TWO");
        numberWords.put(3, "THREE");
        numberWords.put(4, "FOUR");
        numberWords.put(5, "FIVE");
		numberWords.put(6, "SIX");
        numberWords.put(7, "SEVEN");
        // Add more numbers as needed
    }

	@Override
	public String getCommandName() {
		return "jd";
	}

	@Override
	public String getCommandUsage(ICommandSender arg0) {
		return "/" + getCommandName() + " [dungeon]";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	
	@Override
	public void processCommand(ICommandSender arg0, String[] arg1) throws CommandException {
		if(arg1.length!=1) {
			Utils.sendMessage(ChatFormatting.RED+"Invalid Command Usage! Example Usage /jd m4");
			return;
		}
		boolean masterMode = arg1[0].toLowerCase().contains("m");
		int floorInt = parseInt(arg1[0].replaceAll("[^0-9]", ""));
		String dungeonType = (masterMode?"MASTER_":"")+"CATACOMBS";
		String dungeonString = dungeonType+"_FLOOR_"+numberWords.get(floorInt);
		
		Utils.GetMC().thePlayer.sendChatMessage("/joindungeon "+dungeonString);
	}
}
