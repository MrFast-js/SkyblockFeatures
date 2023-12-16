package mrfast.sbf.commands;

import java.util.*;

import com.mojang.realmsclient.gui.ChatFormatting;
import mrfast.sbf.events.SlotClickedEvent;
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

    public static List<Integer> clicked = new ArrayList<Integer>();
    public static long start = 0;
    public static int[] paneSlots = {
            11, 12, 13, 14, 15,
            20, 21, 22, 23, 24,
            29, 30, 31, 32, 33
    };
    public static int[] itemSlots = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };
    public static int[] color3x3Slots = {
            12, 13, 14,
            21, 22, 23,
            30, 31, 32
    };
    public static double mazeIndex = 1;
    public static double orderNumber = 1;

    @Override
    public void processCommand(ICommandSender arg0, String[] arg1) throws CommandException {
        start = 0;
        int termId = Utils.randomNumber(1, 4);
        GuiChest terminal = createTerminal(termId);
        if (terminal != null) {
            GuiUtils.openGui(terminal);
        }
    }

    public static void handleTerminalClick(SlotClickedEvent event) {
        if (event.inventoryName.contains(ChatFormatting.GOLD + "✯")) {
            if (event.inventoryName.contains("Correct Panes")) {
                for (int slot : TerminalCommand.paneSlots) {
                    if (event.slot == null) continue;
                    if (event.slot.slotNumber == slot) {
                        if (event.item.getUnlocalizedName().contains("red")) {
                            Utils.playSound("note.pling", 2);
                            TerminalCommand.clicked.add(event.slot.slotNumber);
                            event.inventory.setInventorySlotContents(event.slot.slotNumber, new ItemStack(Blocks.stained_glass_pane, 1, 5).setStackDisplayName(ChatFormatting.RESET + ""));
                            if (TerminalCommand.clicked.size() == 14) {
                                Utils.sendMessage(ChatFormatting.GREEN + "You completed 'Correct all the panes!' in " + Utils.round((System.currentTimeMillis() - TerminalCommand.start) / 1000d, 2) + "s");
                                Utils.GetMC().thePlayer.closeScreen();
                            }
                            if (TerminalCommand.start == 0) {
                                TerminalCommand.start = System.currentTimeMillis();
                            }
                        }
                        if (event.item.getUnlocalizedName().contains("lime") && TerminalCommand.clicked.contains(event.slot.slotNumber)) {
                            Utils.playSound("note.pling", 2);
                            TerminalCommand.clicked.remove((Integer) event.slot.slotNumber);
                            event.inventory.setInventorySlotContents(event.slot.slotNumber, new ItemStack(Blocks.stained_glass_pane, 1, 14).setStackDisplayName(ChatFormatting.RESET + ""));
                        }
                    }
                }
            }

            if (event.inventoryName.contains("What starts with")) {
                if (event.item.getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane)) return;

                if (TerminalCommand.start == 0) {
                    TerminalCommand.start = System.currentTimeMillis();
                }
                String startingChar = event.inventoryName.split("'")[1];
                if (event.item.getDisplayName().startsWith(startingChar)) {
                    if (!event.item.isItemEnchanted()) {
                        event.item.addEnchantment(Enchantment.efficiency, 1);
                        Utils.playSound("note.pling", 2);
                    }
                } else {
                    Utils.GetMC().thePlayer.closeScreen();
                    Utils.sendMessage(ChatFormatting.RED + "You failed 'What starts with'");
                }
                int possible = 0;
                int actual = 0;

                for (int i = 0; i < 54; i++) {
                    ItemStack stack = event.inventory.getStackInSlot(i);
                    if (stack.getDisplayName().startsWith(startingChar)) {
                        possible++;
                    }
                    if (stack.isItemEnchanted() && stack.getDisplayName().startsWith(startingChar)) {
                        actual++;
                    }
                }

                if (possible == actual) {
                    Utils.sendMessage(ChatFormatting.GREEN + "You completed 'What starts with!' in " + Utils.round((System.currentTimeMillis() - TerminalCommand.start) / 1000d, 2) + "s");
                    Utils.GetMC().thePlayer.closeScreen();
                }
            }

            if (event.inventoryName.contains("Click in order") && event.item.getUnlocalizedName().contains("red")) {
                if (event.item.stackSize == TerminalCommand.orderNumber) {
                    if (TerminalCommand.orderNumber == 14) {
                        Utils.sendMessage(ChatFormatting.GREEN + "You completed 'Click in order!' in " + Utils.round((System.currentTimeMillis() - TerminalCommand.start) / 1000d, 2) + "s");
                        Utils.GetMC().thePlayer.closeScreen();
                        TerminalCommand.orderNumber = 1;
                    }
                    event.inventory.setInventorySlotContents(event.slot.slotNumber, new ItemStack(Blocks.stained_glass_pane, event.item.stackSize, 5).setStackDisplayName(ChatFormatting.RESET + ""));
                    Utils.playSound("note.pling", 2);
                    TerminalCommand.orderNumber++;
                    if (TerminalCommand.start == 0) {
                        TerminalCommand.start = System.currentTimeMillis();
                    }
                } else {
                    Utils.GetMC().thePlayer.closeScreen();
                    Utils.sendMessage(ChatFormatting.RED + "You failed 'Click in order!'");
                }
            }

            if (event.inventoryName.contains("Change all to same color")) {
                // green -> blue -> red -> orange -> yellow
                List<Integer> colors = Arrays.asList(5, 3, 14, 1, 4);
                int color = event.item.getMetadata();
                if (!colors.contains(color)) return;
                Utils.playSound("note.pling", 2);

                if (TerminalCommand.start == 0) {
                    TerminalCommand.start = System.currentTimeMillis();
                }
                int nextIndex = colors.indexOf(color) + 1;
                if (nextIndex > colors.size() - 1) {
                    nextIndex = 0;
                }
                int nextColor = colors.get(nextIndex);

                ItemStack stack = new ItemStack(Blocks.stained_glass_pane, 1, nextColor)
                        .setStackDisplayName(ChatFormatting.RESET + "");

                event.inventory.setInventorySlotContents(event.slot.slotNumber, stack);
                List<Integer> colorSet = new ArrayList<>();
                for (int color3x3Slot : color3x3Slots) {
                    colorSet.add(event.inventory.getStackInSlot(color3x3Slot).getMetadata());
                }
                boolean completeTerminal = true;
                int first = colorSet.get(0);
                for (int slot : colorSet) {
                    if (first != slot) {
                        completeTerminal = false;
                        break;
                    }
                }
                if (completeTerminal) {
                    Utils.GetMC().thePlayer.closeScreen();
                    Utils.sendMessage(ChatFormatting.GREEN + "You completed 'Change all to same color!' in " + Utils.round((System.currentTimeMillis() - TerminalCommand.start) / 1000d, 2) + "s");
                }
            }
        }
        try {
            if (event.inventoryName.contains("✯")) event.setCanceled(true);
        } catch (Exception e) {
            //TODO: handle exception
        }
    }

    public GuiChest createTerminal(int terminalId) {
        InventoryBasic terminal = null;

        switch (terminalId) {
            case 1:
                terminal = CorrectPanesTerminal();
                break;
            case 2:
                terminal = ThreeByThreeTerminal();
                break;
            case 3:
                terminal = ClickInOrderTerminal();
                break;
            case 4:
                terminal = StartsWithTerminal();
                break;
        }
        if (terminal != null) {
            return new GuiChest(Utils.GetMC().thePlayer.inventory, terminal);
        }
        return null;
    }

    private static InventoryBasic StartsWithTerminal() {
        List<Integer> allowedItems = Arrays.asList(334, 44, 275, 301, 299, 298, 300, 147, 155, 327, 21, 153, 73, 16, 56, 14, 279, 293, 276, 3, 23, 175, 61, 20, 41, 266, 429, 65, 84, 257, 256, 69, 91, 17, 162, 378, 103, 335, 5, 392, 319, 86, 168, 411, 414, 66, 38, 40, 331, 6, 397, 332, 427, 280, 29, 274, 273, 272, 287, 50, 106, 326, 295, 111, 126, 35, 268);

        List<Integer> itemsAdded = new ArrayList<>();
        for (int paneSlot : itemSlots) {
            int itemId = allowedItems.get(Utils.randomNumber(0, allowedItems.size() - 1));
            while (itemsAdded.contains(itemId)) {
                itemId = allowedItems.get(Utils.randomNumber(0, allowedItems.size() - 1));
            }
            itemsAdded.add(itemId);
        }
        HashMap<String, Integer> letterRep = new HashMap<>();
        for (Integer i : itemsAdded) {
            Item item = Item.getItemById(i);
            ItemStack stack = new ItemStack(item);
            String startingLetter = stack.getDisplayName().substring(0, 1);
            letterRep.putIfAbsent(startingLetter, 0);
            letterRep.put(startingLetter, letterRep.get(startingLetter) + 1);
        }
        String mostRepeatedLetter = null;
        int maxCount = 0;

        for (Map.Entry<String, Integer> entry : letterRep.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostRepeatedLetter = entry.getKey();
            }
        }

        InventoryBasic terminal = new InventoryBasic(ChatFormatting.GOLD + "✯ What starts with '" + mostRepeatedLetter + "'", true, 54);
        for (int i = 0; i < terminal.getSizeInventory(); i++) {
            terminal.setInventorySlotContents(i, new ItemStack(Blocks.stained_glass_pane, 1, 15).setStackDisplayName(ChatFormatting.RESET + ""));
        }
        for (int i = 0; i < itemsAdded.size(); i++) {
            int slotId = itemSlots[i];
            Item item = Item.getItemById(itemsAdded.get(i));
            ItemStack stack = new ItemStack(item);
            terminal.setInventorySlotContents(slotId, stack);
        }

        return terminal;
    }

    private static InventoryBasic CorrectPanesTerminal() {
        InventoryBasic terminal;
        terminal = new InventoryBasic(ChatFormatting.GOLD + "✯ Correct Panes", true, 45);
        clicked.clear();
        for (int i = 0; i < 45; i++) {
            terminal.setInventorySlotContents(i, new ItemStack(Blocks.stained_glass_pane, 1, 15).setStackDisplayName(ChatFormatting.RESET + ""));
        }

        int startingSlot = new Random().nextInt(paneSlots.length);
        terminal.setInventorySlotContents(paneSlots[startingSlot], new ItemStack(Blocks.stained_glass_pane, 1, 5).setStackDisplayName(ChatFormatting.RESET + ""));

        for (int slot : paneSlots) {
            if (paneSlots[startingSlot] == slot) continue;
            terminal.setInventorySlotContents(slot, new ItemStack(Blocks.stained_glass_pane, 1, 14).setStackDisplayName(ChatFormatting.RESET + ""));
        }
        return terminal;
    }

    private static InventoryBasic ThreeByThreeTerminal() {
        InventoryBasic terminal;
        mazeIndex = 1;
        terminal = new InventoryBasic(ChatFormatting.GOLD + "✯ Change all to same color", true, 45);
        clicked.clear();
        for (int i = 0; i < 45; i++) {
            terminal.setInventorySlotContents(i, new ItemStack(Blocks.stained_glass_pane, 1, 15).setStackDisplayName(ChatFormatting.RESET + ""));
        }

        for (int slot : color3x3Slots) {
            // green -> blue -> red -> orange -> yellow
            List<Integer> colors = Arrays.asList(5, 3, 14, 1, 4);
            int selectedColor = colors.get(Utils.randomNumber(0, colors.size() - 1));

            terminal.setInventorySlotContents(slot, new ItemStack(Blocks.stained_glass_pane, 1, selectedColor).setStackDisplayName(ChatFormatting.RESET + ""));
        }

        GuiUtils.openGui(new GuiChest(Utils.GetMC().thePlayer.inventory, terminal));
        return terminal;
    }

    private static InventoryBasic ClickInOrderTerminal() {
        TerminalCommand.orderNumber = 1;
        InventoryBasic Terminal = new InventoryBasic(ChatFormatting.GOLD + "✯ Click in order", true, 36);
        for (int i = 0; i < 36; i++) {
            Terminal.setInventorySlotContents(i, new ItemStack(Blocks.stained_glass_pane, 1, 15).setStackDisplayName(ChatFormatting.RESET + ""));
        }
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14);
        Collections.shuffle(numbers);
        int index = 0;

        // Top Row
        for (int i = 10; i < 17; i++) {
            int randNum = numbers.get(index);
            Terminal.setInventorySlotContents(i, new ItemStack(Blocks.stained_glass_pane, randNum, 14).setStackDisplayName(ChatFormatting.RESET + ""));
            index++;
        }
        // Bottom Row
        for (int i = 19; i < 26; i++) {
            int randNum = numbers.get(index);
            Terminal.setInventorySlotContents(i, new ItemStack(Blocks.stained_glass_pane, randNum, 14).setStackDisplayName(ChatFormatting.RESET + ""));
            index++;
        }
        return Terminal;
    }
}
