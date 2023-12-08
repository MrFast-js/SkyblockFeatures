package mrfast.sbf.features.overlays.menuOverlay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.PricingData;
import mrfast.sbf.events.GuiContainerEvent;
import mrfast.sbf.utils.GuiUtils;
import mrfast.sbf.utils.ItemUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TradingOverlay {
    List<Integer> topSelfSlotIds = new ArrayList<>(Arrays.asList(0,1,2,3));
    List<Integer> topOtherSlotIds = new ArrayList<>(Arrays.asList(5,6,7,8));

    @SubscribeEvent
    public void onTitleDrawn(GuiContainerEvent.TitleDrawnEvent event) {
        if (event.gui instanceof GuiChest && SkyblockFeatures.config.tradeOverlay) {
            GuiChest gui = (GuiChest) event.gui;
            ContainerChest chest = (ContainerChest) gui.inventorySlots;
            IInventory inv = chest.getLowerChestInventory();
            String chestName = inv.getDisplayName().getUnformattedText().trim();
            if (chestName.contains("You                  ")) {
                List<Integer> selfSlots = new ArrayList<>();
                List<Integer> otherSlots = new ArrayList<>();
                for(int slot:topSelfSlotIds) {
                    selfSlots.add(slot);
                    selfSlots.add(slot+9);
                    selfSlots.add(slot+18);
                    selfSlots.add(slot+27);
                }
                for(int slot:topOtherSlotIds) {
                    otherSlots.add(slot);
                    otherSlots.add(slot+9);
                    otherSlots.add(slot+18);
                    otherSlots.add(slot+27);
                }
                HashMap<String,Double> selfItemsAndValues = new HashMap<>();
                HashMap<String,Double> otherItemsAndValues = new HashMap<>();

                double totalOther = 0;
                double totalSelf = 0;

                for(int slotId = 0;slotId<inv.getSizeInventory();slotId++) {
                    if(inv.getStackInSlot(slotId)==null) continue;
                    double value = 0;
                    String id = PricingData.getIdentifier(inv.getStackInSlot(slotId));
                    boolean coins = inv.getStackInSlot(slotId).getDisplayName().contains("coins");
                    if(inv.getStackInSlot(slotId)!=null) {
                        if(id==null && !coins) continue;
                    } else {
                        if(id==null) continue;
                    }
                    if(coins) {
                        String line = Utils.cleanColor(inv.getStackInSlot(slotId).getDisplayName());
                        line = line.replace("k", "000").replace("M", "000000").replace("B", "000000000");
                        double coinValue = Double.parseDouble(line.replaceAll("[^0-9]", ""));
                        if(line.contains(".")) coinValue/=10;
                        if(selfSlots.contains(slotId)) {
                            value = coinValue;
                        }
                        if(otherSlots.contains(slotId)) {
                            value = coinValue;
                        }
                    }
                    else if(PricingData.bazaarPrices.containsKey(id)) {
                        value = PricingData.bazaarPrices.get(id);
                    }
                    else if(PricingData.lowestBINs.containsKey(id)) {
                        value = ItemUtils.getEstimatedItemValue(inv.getStackInSlot(slotId))*inv.getStackInSlot(slotId).stackSize;
                    }
                    if(selfSlots.contains(slotId)) {
                        totalSelf+=value*(coins?1:inv.getStackInSlot(slotId).stackSize);
                        selfItemsAndValues.put(inv.getStackInSlot(slotId).getDisplayName(), value);
                    }
                    if(otherSlots.contains(slotId)) {
                        totalOther+=value*(coins?1:inv.getStackInSlot(slotId).stackSize);
                        otherItemsAndValues.put(inv.getStackInSlot(slotId).getDisplayName(), value);
                    }
                }

                drawOtherPersonValue(totalOther,otherItemsAndValues);
                drawSelfPersonValue(totalSelf,selfItemsAndValues);
            }
        }
    }

    public void drawOtherPersonValue(Double total,HashMap<String,Double> items) {
        List<String> lines = new ArrayList<>(Arrays.asList(ChatFormatting.WHITE+"Total Value: "+ChatFormatting.GOLD+Utils.shortenNumber(total),""));
        for(String itemName:items.keySet()) {
            String name = itemName;
            if(itemName.length()>21) name = itemName.substring(0, 20)+"..";
            lines.add(name+" "+ChatFormatting.DARK_GRAY+ChatFormatting.ITALIC+"("+Utils.shortenNumber(items.get(itemName))+")");
        }
        GuiUtils.drawSideMenu(lines, GuiUtils.TextStyle.DROP_SHADOW);
    }

    public void drawSelfPersonValue(Double total,HashMap<String,Double> items) {
        List<String> lines = new ArrayList<>(Arrays.asList(ChatFormatting.WHITE+"Total Value: "+ChatFormatting.GOLD+Utils.shortenNumber(total),""));
        for(String itemName:items.keySet()) {
            String name = itemName;
            if(itemName.length()>21) name = itemName.substring(0, 20)+"..";
            lines.add(name+" "+ChatFormatting.DARK_GRAY+ChatFormatting.ITALIC+"("+Utils.shortenNumber(items.get(itemName))+")");
        }
        GuiUtils.drawSideMenu(lines, GuiUtils.TextStyle.DROP_SHADOW,true);
    }
}
