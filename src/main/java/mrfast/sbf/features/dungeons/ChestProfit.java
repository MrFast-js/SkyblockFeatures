package mrfast.sbf.features.dungeons;

import java.util.ArrayList;
import java.util.HashMap;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.PricingData;
import mrfast.sbf.events.GuiContainerEvent.TitleDrawnEvent;
import mrfast.sbf.utils.GuiUtils;
import mrfast.sbf.utils.ItemUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ChestProfit {

    @SubscribeEvent
    public void onGUIDrawnEvent(TitleDrawnEvent event) {
        if (event.gui == null || !SkyblockFeatures.config.dungeonChestProfit || !Utils.inSkyblock) return;
        if (event.gui instanceof GuiChest) {
            HashMap<ItemStack,Double> items = new HashMap<ItemStack,Double>();    
            ContainerChest chest = (ContainerChest) event.gui.inventorySlots;
            IInventory inv = chest.getLowerChestInventory();
            if (inv.getDisplayName().getUnformattedText().endsWith(" Chest")) {
                int chestValue = 0;
                int price = 0;
                ItemStack openChest = inv.getStackInSlot(31);
                if (openChest != null && openChest.getDisplayName().equals("§aOpen Reward Chest")) {
                    for (String unclean : ItemUtils.getItemLore(openChest)) {
                        String line = Utils.cleanColor(unclean);
                        if (line.contains("FREE")) {
                            break;
                        } else if (line.contains(" Coins")) {
                            price = Integer.parseInt(line.replaceAll("[^0-9]", ""));
                            break;
                        }
                    }

                    for (int i = 11; i < 16; i++) {
                        ItemStack lootSlot = inv.getStackInSlot(i);
                        String identifier = PricingData.getIdentifier(lootSlot);
                        if (identifier != null) {
                            Double value = PricingData.averageLowestBINs.get(identifier);
                            if (value == null || identifier.contains("ENCHANTMENT_")) {
                                value = PricingData.bazaarPrices.get(identifier);
                            }
                            chestValue += value;
                            items.put(lootSlot, value);
                        }
                    }
                }
                if (!items.isEmpty()) {
                    ArrayList<String> lines = new ArrayList<>();
                    GlStateManager.color(1, 1, 1, 1);
                    GlStateManager.disableLighting();

                    double profit = chestValue - price;
                    for (ItemStack item : items.keySet()) {
                        String name = item.getDisplayName().contains("Enchanted")?ItemUtils.getItemLore(item).get(0):item.getDisplayName();
                        lines.add(name + "§f: §a" + Utils.nf.format(items.get(item)));
                    }
                    lines.add("");
                    lines.add("Profit: §" + (profit > 0 ? "a" : "c")+Utils.nf.format(profit));

                    GuiUtils.drawSideMenu(lines, GuiUtils.TextStyle.DROP_SHADOW);
                }
            }
        }
    }
}
