package mrfast.sbf.features.dungeons.solvers.terminals;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.events.GuiContainerEvent;
import mrfast.sbf.events.SlotClickedEvent;
import mrfast.sbf.utils.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.world.WorldEvent;
import java.awt.*;
import java.util.HashMap;

public class ClickInOrderSolver {
    static int currentNumber = 1;
    HashMap<Slot,Integer> orderMap = new HashMap<>();

    @SubscribeEvent
    public void onSlotClick(SlotClickedEvent event) {
        if (event.chestName.contains("Click in order")) {
            if(event.slot.getStack()==null) return;

            if(event.slot.getStack().getMetadata()==14) {
                currentNumber++;
            }
        }
    }

    @SubscribeEvent
    public void onWindowClose(GuiContainerEvent.CloseWindowEvent event) {
        currentNumber = 1;
        orderMap.clear();
    }
    @SubscribeEvent
    public void onWorldChanges(WorldEvent.Load event) {
        currentNumber = 1;
        orderMap.clear();;
    }

    @SubscribeEvent
    public void onSlotDraw(GuiContainerEvent.DrawSlotEvent.Pre event) {
        if(event.chestName==null) return;
        if(event.chestName.contains("Click in order")) {
            if(!event.slot.getHasStack()) return;
            if(event.slot.getStack().getMetadata()==14) {
                orderMap.put(event.slot,event.slot.getStack().stackSize);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onScreenDraw(GuiContainerEvent.TitleDrawnEvent event) {
        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) return;

        GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;
        ContainerChest cont = (ContainerChest) chest.inventorySlots;
        String name = cont.getLowerChestInventory().getName();

        // Using contains rather than equals so it works for practice terminals aswell
        if (name.contains("Click in order") && SkyblockFeatures.config.clickInOrderSolver) {
            for (Slot slot : orderMap.keySet()) {
                if(slot.getStack()==null) continue;
                int order = orderMap.get(slot);
                int x = slot.xDisplayPosition;
                int y = slot.yDisplayPosition;
                Color color = null;
                if (order == currentNumber) {
                    color = SkyblockFeatures.config.clickInOrderSolverCurrent;
                }
                if (order == currentNumber + 1) {
                    color = SkyblockFeatures.config.clickInOrderSolverNext;
                }
                if (order == currentNumber + 2) {
                    color = SkyblockFeatures.config.clickInOrderSolverNext2;
                }

                if (color != null) {
                    Gui.drawRect(x, y, x + 16, y + 16, color.getRGB());
                }
                GuiUtils.drawText(order + "", x + 4, y + 4, GuiUtils.TextStyle.DROP_SHADOW);
            }
        }
    }
}
