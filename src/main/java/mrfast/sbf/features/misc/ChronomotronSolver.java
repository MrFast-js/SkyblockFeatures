package mrfast.sbf.features.misc;

import java.util.ArrayList;
import java.util.List;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.events.SlotClickedEvent;
import mrfast.sbf.events.GuiContainerEvent.TitleDrawnEvent;
import mrfast.sbf.utils.Utils;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ChronomotronSolver {
    List<String> ChronomotronOrder = new ArrayList<>();
    static int lastChronomatronRound = 0;
    static List<String> chronomatronPattern = new ArrayList<>();
    static int chronomatronMouseClicks = 0;
    static int slotToClick = 0;
    @SubscribeEvent
    public void onSlotClick(SlotClickedEvent event) {
        if(!SkyblockFeatures.config.enchantingSolvers) return;
        if (event.inventoryName.startsWith("Chronomatron (")) {
            IInventory inventory = event.inventory;
            ItemStack item = event.item;
            if (item == null) return;
            if (inventory.getStackInSlot(49).getDisplayName().startsWith("§7Timer: §a") && (item.getItem() == Item.getItemFromBlock(Blocks.stained_glass) || item.getItem() == Item.getItemFromBlock(Blocks.stained_hardened_clay))) {
                chronomatronMouseClicks++;
            }
        }
    }
    
    @SubscribeEvent
    public void onGuiBackground(TitleDrawnEvent event) {
        if(!SkyblockFeatures.config.enchantingSolvers) return;
        if(event.displayName.startsWith("Chronomatron (")) {
            List<Slot> invSlots = event.container.inventorySlots;
            if (invSlots.size() > 48 && invSlots.get(49).getStack() != null) {
                if (invSlots.get(49).getStack().getDisplayName().startsWith("§7Timer: §a") && invSlots.get(4).getStack() != null) {
                    int round = invSlots.get(4).getStack().stackSize;
                    int timerSeconds = Integer.parseInt(Utils.cleanColor(invSlots.get(49).getStack().getDisplayName()).replaceAll("[^\\d]", ""));
                    if (round != lastChronomatronRound && timerSeconds == round + 2) {
                        lastChronomatronRound = round;
                        for (int i = 10; i <= 43; i++) {
                            ItemStack stack = invSlots.get(i).getStack();
                            if (stack == null) continue;
                            if (stack.getItem() == Item.getItemFromBlock(Blocks.stained_hardened_clay)) {
                                chronomatronPattern.add(stack.getDisplayName());
                                break;
                            }
                        }
                    }
                    if (chronomatronMouseClicks < chronomatronPattern.size()) {
                        for (int i = 10; i <= 43; i++) {
                            ItemStack glass = invSlots.get(i).getStack();
                            if (glass == null) continue;
                            if (!glass.getDisplayName().equals(chronomatronPattern.get(chronomatronMouseClicks))) {
                                event.container.putStackInSlot(i, new ItemStack(Blocks.stained_glass_pane, 1, 15).setStackDisplayName(" "));
                            }
                        }
                    }
                } else if (invSlots.get(49).getStack().getDisplayName().equals("§aRemember the pattern!")) {
                    chronomatronMouseClicks = 0;
                }
            }
        }
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if(!SkyblockFeatures.config.enchantingSolvers) return;
        lastChronomatronRound = 0;
        chronomatronPattern.clear();
        chronomatronMouseClicks = 0;
    }
}
