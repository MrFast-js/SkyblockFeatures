package mrfast.skyblockfeatures.features.misc;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;

import mrfast.skyblockfeatures.SkyblockFeatures;
import mrfast.skyblockfeatures.events.SlotClickedEvent;
import mrfast.skyblockfeatures.events.GuiContainerEvent.TitleDrawnEvent;
import mrfast.skyblockfeatures.features.items.HideGlass;
import mrfast.skyblockfeatures.utils.Utils;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class UltrasequencerSolver {
    static Slot[] clickInOrderSlots = new Slot[36];
    static int lastUltraSequencerClicked = 0;
    static HashMap<Slot,ItemStack> answers = new HashMap<>();
    static int clickIndex = 1;
    
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if(!SkyblockFeatures.config.enchantingSolvers) return;

        if (Utils.GetMC().currentScreen instanceof GuiChest) {
            if (Utils.GetMC().thePlayer == null) return;
            ContainerChest chest = (ContainerChest) Utils.GetMC().thePlayer.openContainer;
            List<Slot> invSlots = ((GuiChest) Utils.GetMC().currentScreen).inventorySlots.inventorySlots;
            String chestName = chest.getLowerChestInventory().getDisplayName().getUnformattedText().trim();

            if (chestName.startsWith("Ultrasequencer (")) {
                if (invSlots.get(49).getStack() != null && invSlots.get(49).getStack().getDisplayName().equals("§aRemember the pattern!")) {
                    answers.clear();
                    for (int i = 9; i <= 44; i++) {
                        if (invSlots.get(i) == null || invSlots.get(i).getStack() == null) continue;
                        String itemName = Utils.cleanColor(invSlots.get(i).getStack().getDisplayName());
                        if (itemName.matches("\\d+")) {
                            int number = Integer.parseInt(itemName);
                            clickInOrderSlots[number - 1] = invSlots.get(i);
                            answers.put(invSlots.get(i), invSlots.get(i).getStack());
                        }
                    }
                }
            }
        }
    }
    @SubscribeEvent
    public void onSlotClick(SlotClickedEvent event) {
        if(!SkyblockFeatures.config.enchantingSolvers) return;
        if (Utils.GetMC().currentScreen instanceof GuiChest) {
            if (Utils.GetMC().thePlayer == null) return;
            ContainerChest chest = (ContainerChest) Utils.GetMC().thePlayer.openContainer;
            String chestName = chest.getLowerChestInventory().getDisplayName().getUnformattedText().trim();
            if (chestName.startsWith("Ultrasequencer (")) {
                if(HideGlass.isEmptyGlassPane(event.slot.getStack())) event.setCanceled(true);

                try {
                    int slotInt = Integer.parseInt(Utils.cleanColor(event.slot.getStack().getDisplayName()).replaceAll("[^0-9]", ""));
                    if(slotInt!=clickIndex) event.setCanceled(true);
                    else {
                        clickIndex++;
                    }
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
        }
    }

    @SubscribeEvent
    public void onGuiRender(TitleDrawnEvent event) {
        if(!SkyblockFeatures.config.enchantingSolvers) return;
        if (event.displayName.startsWith("Ultrasequencer (")) {
            List<Slot> invSlots = event.container.inventorySlots;
            if (invSlots.size() > 48 && invSlots.get(49).getStack() != null) {
                if (invSlots.get(49).getStack().getDisplayName().startsWith("§7Timer: §a")) {
                    for (Slot slot : invSlots) {
                        if(answers.containsKey(slot)) {
                            if(slot.getStack()!=null) {
                                try {
                                    int slotInt = Integer.parseInt(Utils.cleanColor(slot.getStack().getDisplayName()).replaceAll("[^0-9]", ""));
                                    if(slotInt==clickIndex) {
                                        sloX = slot.xDisplayPosition;
                                        slotY = slot.yDisplayPosition;
                                    }
                                    if(slotInt==clickIndex+1) {
                                        nextsloX = slot.xDisplayPosition;
                                        nextslotY = slot.yDisplayPosition;
                                    }
                                } catch (Exception e) {
                                    // TODO: handle exception
                                }
                            }
                            event.container.putStackInSlot(slot.slotNumber, answers.get(slot));
                        }
                    }
                } else {
                    clickIndex = 1;
                    sloX = 0;
                    nextsloX = 0;
                }
            }
        }
    }
    int sloX = 0;
    int slotY = 0;

    int nextsloX = 0;
    int nextslotY = 0;

    @SubscribeEvent
    public void onDrawSlot(GuiScreenEvent.DrawScreenEvent.Post event) {
        if(sloX==0) return;
        Utils.drawLineInGui(sloX+400, slotY+168, event.mouseX, event.mouseY, new Color(0x00FF00), 2,1);
        if(nextsloX==0) return;
        Utils.drawLineInGui(sloX+400, slotY+168, nextsloX+400, nextslotY+168, new Color(0xFFFF00), 2,0.6);
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        clickIndex = 1;
        clickInOrderSlots = new Slot[36];
        sloX = 0;
        nextsloX = 0;
    }
}
