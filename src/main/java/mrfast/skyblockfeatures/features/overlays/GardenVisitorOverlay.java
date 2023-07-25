package mrfast.skyblockfeatures.features.overlays;

import java.util.ArrayList;
import java.util.List;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.skyblockfeatures.SkyblockFeatures;
import mrfast.skyblockfeatures.core.PricingData;
import mrfast.skyblockfeatures.events.GuiContainerEvent.TitleDrawnEvent;
import mrfast.skyblockfeatures.utils.ItemUtils;

import mrfast.skyblockfeatures.utils.Utils;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class GardenVisitorOverlay {
    @SubscribeEvent
    public void onDrawContainerTitle(TitleDrawnEvent event) {
        if (event.gui != null && event.gui instanceof GuiChest && SkyblockFeatures.config.GardenVisitorOverlay) {
            GuiChest gui = (GuiChest) Utils.GetMC().currentScreen;

            ContainerChest chest = (ContainerChest) gui.inventorySlots;
            IInventory inv = chest.getLowerChestInventory();
            if(inv.getStackInSlot(29)!=null) {
                if(inv.getStackInSlot(29).getDisplayName().contains("Accept Offer")) {
                    ItemStack item = inv.getStackInSlot(29);
                    Integer copperCount = 1;
                    Integer totalCost = 0;
                    Boolean gettingMaterials = true;
                    List<String> required = new ArrayList<>();
                    for(String line:ItemUtils.getItemLore(item)) {
                        String rawline = Utils.cleanColor(line);
                        if(rawline.contains("Copper")) {
                            copperCount = Integer.parseInt(rawline.replaceAll("[^0-9]", ""));
                        }
                        if(rawline.contains("Rewards")) gettingMaterials = false;
                        if(rawline.contains("x") && gettingMaterials) {
                            String itemName = "";
                            Integer itemCount = 1;
                            try {
                                itemName = rawline.substring(1, rawline.indexOf("x")-1).toUpperCase().replaceAll(" ", "_");
                                itemCount = Integer.parseInt(rawline.substring(rawline.indexOf("x")+1,rawline.length()));
                            } catch (Exception e) {
                                // TODO: handle exception
                            }
                            if(PricingData.bazaarPrices.containsKey(itemName)) {
                                totalCost += (PricingData.bazaarPrices.get(itemName).intValue()*itemCount);
                            }
                            required.add(" "+line);
                        }
                    }
                    int index = 0;
                    List<String> lines = new ArrayList<>();
                    lines.add(ChatFormatting.RED+"Items Required: ");
                    for(String requiredItem:required) lines.add(requiredItem);
                    lines.add(ChatFormatting.RED+"Copper Reward: "+ChatFormatting.GOLD+Utils.nf.format(copperCount));
                    lines.add(ChatFormatting.AQUA+"Cost to fill: "+ChatFormatting.GOLD+(totalCost!=0?Utils.nf.format(totalCost):ChatFormatting.RED+"Unknown Price"));
                    lines.add(ChatFormatting.RED+"Coins Per Copper: "+ChatFormatting.GOLD+Utils.nf.format(totalCost/copperCount));

                    Utils.drawGraySquareWithBorder(180, 0, 200, (int) (lines.size()*1.15*Utils.GetMC().fontRendererObj.FONT_HEIGHT),3);
                    for(String line:lines) {
                        Utils.GetMC().fontRendererObj.drawStringWithShadow(line, 190, (index*(Utils.GetMC().fontRendererObj.FONT_HEIGHT+1)+3), -1);
                        index++;
                    }
                }
            }
        }
    }
}
