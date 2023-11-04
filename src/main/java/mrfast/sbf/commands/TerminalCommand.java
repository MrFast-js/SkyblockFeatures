package mrfast.sbf.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import com.mojang.realmsclient.gui.ChatFormatting;
import mrfast.sbf.utils.GuiUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryBasic;
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
	public static List<Integer> clicked = new ArrayList<Integer>();
	public static long start = 0;
	public static int[] paneSlots = {
		11, 12, 13, 14, 15,
		20, 21, 22, 23, 24,
		29, 30, 31, 32, 33
	};
	public static int[] mazeSlots = {
		5, 6, 7, 16,
		25,24,23,22,21,20,19,
		28, 37, 38, 39, 40
	};
	public static double mazeIndex = 1;
	public static double orderNumber = 1;
	// Color Key
	// gray 15
	// red 14
	// green 5
	@Override
	public void processCommand(ICommandSender arg0, String[] arg1) throws CommandException {
		start = 0;
		double termId = Utils.randomNumber(1,3);
		if(termId==1) {
			InventoryBasic Terminal = new InventoryBasic(ChatFormatting.GREEN+"✯ Correct Panes", true, 45);
			clicked.clear();
			for(int i = 0; i < 45; i++) {
				Terminal.setInventorySlotContents(i, new ItemStack(Blocks.stained_glass_pane, 1, 15).setStackDisplayName(ChatFormatting.RESET+""));
			}

			int startingSlot = new Random().nextInt(paneSlots.length);
			Terminal.setInventorySlotContents(paneSlots[startingSlot], new ItemStack(Blocks.stained_glass_pane, 1, 5).setStackDisplayName(ChatFormatting.RESET+""));

			for(int slot : paneSlots) {
				if(paneSlots[startingSlot] == slot) continue;
				Terminal.setInventorySlotContents(slot, new ItemStack(Blocks.stained_glass_pane, 1, 14).setStackDisplayName(ChatFormatting.RESET+""));
			}

			GuiUtils.openGui(new GuiChest(Utils.GetMC().thePlayer.inventory, Terminal));
		} else if(termId==2) {
			mazeIndex = 1;
			InventoryBasic Terminal = new InventoryBasic(ChatFormatting.GREEN+"✯ Solve Maze", true, 54);
			clicked.clear();
			for(int i = 0; i < 54; i++) {
				Terminal.setInventorySlotContents(i, new ItemStack(Blocks.stained_glass_pane, 1, 15).setStackDisplayName(ChatFormatting.RESET+""));
			}

			int startingSlot = 49;
			int endingSlot = 4;
			Terminal.setInventorySlotContents(startingSlot, new ItemStack(Blocks.stained_glass_pane, 1, 5).setStackDisplayName(ChatFormatting.RESET+""));
			Terminal.setInventorySlotContents(endingSlot, new ItemStack(Blocks.stained_glass_pane, 1, 14).setStackDisplayName(ChatFormatting.RESET+""));

			for(int slot : mazeSlots) {
				Terminal.setInventorySlotContents(slot, new ItemStack(Blocks.stained_glass_pane, 1, 0).setStackDisplayName(ChatFormatting.RESET+""));
			}

			GuiUtils.openGui(new GuiChest(Utils.GetMC().thePlayer.inventory, Terminal));
		} else if(termId==3) {
			TerminalCommand.orderNumber = 1;
			InventoryBasic Terminal = new InventoryBasic(ChatFormatting.GREEN+"✯ Click in order", true, 36);
			for(int i = 0; i < 36; i++) {
				Terminal.setInventorySlotContents(i, new ItemStack(Blocks.stained_glass_pane, 1, 15).setStackDisplayName(ChatFormatting.RESET+""));
			}
			List<Integer> numbers = Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12,13,14);
			Collections.shuffle(numbers);
			int index = 0;
			
			// Top Row
			for(int i=10;i<17;i++) {
				int randNum = numbers.get(index);
				Terminal.setInventorySlotContents(i, new ItemStack(Blocks.stained_glass_pane, randNum, 14).setStackDisplayName(ChatFormatting.GREEN+""+randNum));
				index++;
			}
			// Bottom Row
			for(int i=19;i<26;i++) {
				int randNum = numbers.get(index);
				Terminal.setInventorySlotContents(i, new ItemStack(Blocks.stained_glass_pane, randNum, 14).setStackDisplayName(ChatFormatting.GREEN+""+randNum));
				index++;
			}

			GuiUtils.openGui(new GuiChest(Utils.GetMC().thePlayer.inventory, Terminal));
		}
	}
}
