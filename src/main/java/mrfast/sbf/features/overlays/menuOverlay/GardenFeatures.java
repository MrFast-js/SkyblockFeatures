package mrfast.sbf.features.overlays.menuOverlay;

import java.util.ArrayList;
import java.util.List;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.PricingData;
import mrfast.sbf.events.GuiContainerEvent.TitleDrawnEvent;
import mrfast.sbf.events.SecondPassedEvent;
import mrfast.sbf.gui.GuiManager;
import mrfast.sbf.utils.ItemUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class GardenFeatures {
    @SubscribeEvent
    public void onDrawContainerTitle(TitleDrawnEvent event) {
        if (event.gui instanceof GuiChest && SkyblockFeatures.config.GardenVisitorOverlay) {
            GuiChest gui = (GuiChest) Utils.GetMC().currentScreen;

            ContainerChest chest = (ContainerChest) gui.inventorySlots;
            IInventory inv = chest.getLowerChestInventory();
            if(inv.getStackInSlot(29)!=null) {
                if(inv.getStackInSlot(29).getDisplayName().contains("Accept Offer")) {
                    ItemStack item = inv.getStackInSlot(29);
                    int copperCount = 1;
                    int totalCost = 0;
                    boolean gettingMaterials = true;
                    List<String> required = new ArrayList<>();
                    for(String line:ItemUtils.getItemLore(item)) {
                        String rawline = Utils.cleanColor(line);
                        if(rawline.contains("Copper")) {
                            copperCount = Integer.parseInt(rawline.replaceAll("[^0-9]", ""));
                        }
                        if(rawline.contains("Rewards")) gettingMaterials = false;
                        if(rawline.contains("x") && gettingMaterials) {
                            String itemName = "";
                            int itemCount = 1;
                            try {
                                itemName = rawline.substring(1, rawline.indexOf("x")-1).toUpperCase().replaceAll(" ", "_");
                                itemCount = Integer.parseInt(rawline.substring(rawline.indexOf("x")+1));
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
                    lines.add(ChatFormatting.YELLOW+"Items Required: ");
                    lines.addAll(required);
                    lines.add(ChatFormatting.AQUA+"Coins Per Copper: "+ChatFormatting.GOLD+Utils.nf.format(totalCost/copperCount));
                    lines.add(ChatFormatting.AQUA+"Copper Reward: "+ChatFormatting.RED+Utils.nf.format(copperCount));
                    lines.add(ChatFormatting.AQUA+"Cost to fill: "+ChatFormatting.GOLD+(totalCost!=0?Utils.nf.format(totalCost):ChatFormatting.RED+"Unknown Price"));

                    Utils.drawGraySquareWithBorder(180, 0, 200, (int) (lines.size()*1.25*Utils.GetMC().fontRendererObj.FONT_HEIGHT),3);
                    for(String line:lines) {
                        Utils.GetMC().fontRendererObj.drawStringWithShadow(line, 190, (index*(Utils.GetMC().fontRendererObj.FONT_HEIGHT+1)+3), -1);
                        index++;
                    }
                }
            }
        }
    }

    public static Double speedPerUpgrade = 0.2;
    public static Double multiDropPerUpgrade = 0.03;
    public static Double fuelCapPerUpgrade = 30000.0;
    public static Double orgMatPerUpgrade = 20000.0;
    public static Double costRedPerUpgrade = .01;

    // read upgrades from composter upgrades menu
    public static Double fuelInitial = 100000.0;
    public static Double orgMatInitial = 40000.0;
    public Long whenFuelRunsOut = 0L;
    public Long whenMatRunsOut = 0L;

    public boolean composterRunning = false;

    @SubscribeEvent
    public void onDrawTitle(TitleDrawnEvent event) {
        if (event.gui instanceof GuiChest && SkyblockFeatures.config.composterOverlay) {
            GuiChest gui = (GuiChest) Utils.GetMC().currentScreen;
            ContainerChest chest = (ContainerChest) gui.inventorySlots;
            IInventory inv = chest.getLowerChestInventory();
            String chestName = inv.getDisplayName().getUnformattedText().trim();
            if(chestName.contains("Composter") && chestName.contains("Upgrades")) {
                SkyblockFeatures.config.speedLvl = (int) getTier(inv.getStackInSlot(20));
                SkyblockFeatures.config.multiLvl = (int) getTier(inv.getStackInSlot(21));
                SkyblockFeatures.config.fuelLvl = (int) getTier(inv.getStackInSlot(22));
                SkyblockFeatures.config.orgLvl = (int) getTier(inv.getStackInSlot(23));
                SkyblockFeatures.config.costLvl = (int) getTier(inv.getStackInSlot(24));
            }

            if(chestName.contains("Composter") && !chestName.contains(" ")) {
                double coinPerSeedBox = PricingData.bazaarPrices.get("BOX_OF_SEEDS")/25600; // 25600 organic matter
                double coinPerOil = PricingData.bazaarPrices.get("OIL_BARREL")/10000; // 10000 fuel
                int secondsPerCompost = (int) ((60*10)/(1+(SkyblockFeatures.config.speedLvl*speedPerUpgrade)));
                Double compostPerHour = ((60*60)/secondsPerCompost)*(1+(multiDropPerUpgrade*SkyblockFeatures.config.multiLvl));
                Double costReduction = (1-(SkyblockFeatures.config.costLvl*costRedPerUpgrade));

                Double sellPricePerCompost = PricingData.bazaarPrices.get("COMPOST");
                Double totalWithReduction = (coinPerOil*2000)+(coinPerSeedBox*4000)*costReduction;
                double profitPerCompost = sellPricePerCompost-totalWithReduction;
                double thing1 = (orgMatInitial+SkyblockFeatures.config.orgLvl*orgMatPerUpgrade)/(costReduction*compostPerHour*4000);
                double thing2 = (fuelInitial+SkyblockFeatures.config.fuelLvl*fuelCapPerUpgrade)/(costReduction*compostPerHour*2000);

                double profitPerHour = compostPerHour*sellPricePerCompost;
                double profitPerRefill = Math.min(thing1,thing2)*profitPerHour;

                String time = Utils.secondsToTime(secondsPerCompost);

                double fuelUsagePerHour = costReduction*compostPerHour*2000.0;
                double organicUsagePerHour = costReduction*compostPerHour*4000.0;

                int currentFuelStorage = Integer.parseInt((Utils.cleanColor(ItemUtils.getItemLore(inv.getStackInSlot(7)).get(0).split("/")[0])).replaceAll("[^0-9]", ""));
                int timeTillFuelRunOut = (int) ((currentFuelStorage/fuelUsagePerHour)*60.0*60.0);
                int currentMatStorage = Integer.parseInt((Utils.cleanColor(ItemUtils.getItemLore(inv.getStackInSlot(1)).get(0).split("/")[0])).replaceAll("[^0-9]", ""));
                int timeTillMatRunOut = (int) ((currentMatStorage/organicUsagePerHour)*60.0*60.0);

                composterRunning = currentFuelStorage > 2000 && currentMatStorage > 4000;

                whenFuelRunsOut = (timeTillFuelRunOut* 1000L)+System.currentTimeMillis();
                whenMatRunsOut = (timeTillFuelRunOut* 1000L)+System.currentTimeMillis();

                String timeTillFuelGone = Utils.secondsToTime(timeTillFuelRunOut);
                String timeTillMatGone = Utils.secondsToTime(timeTillMatRunOut);

                String[] lines = {
                        ChatFormatting.WHITE+"Profit Per Compost: "+ChatFormatting.GOLD+Utils.nf.format((int) profitPerCompost),
                        ChatFormatting.WHITE+"Profit Per Refill: "+ChatFormatting.GOLD+Utils.nf.format((int) profitPerRefill),
                        ChatFormatting.WHITE+"Profit Per Hour: "+ChatFormatting.GOLD+Utils.nf.format((int) profitPerHour),
                        ChatFormatting.WHITE+"Time Per Compost: "+ChatFormatting.GREEN+time,
                        ChatFormatting.WHITE+"Matter Runs Out: "+ChatFormatting.YELLOW+timeTillMatGone,
                        ChatFormatting.WHITE+"Fuel Runs Out: "+ChatFormatting.YELLOW+timeTillFuelGone
                };

                Utils.drawGraySquareWithBorder(180, 0, 150, (int) ((lines.length+2.2)*Utils.GetMC().fontRendererObj.FONT_HEIGHT),3);
                int lineCount = 0;
                for(String line:lines) {
                    Utils.GetMC().fontRendererObj.drawStringWithShadow(line, 190, lineCount*(Utils.GetMC().fontRendererObj.FONT_HEIGHT+1)+10, -1);
                    lineCount++;
                }
            }
        }
    }

    @SubscribeEvent
    public void onSecond(SecondPassedEvent event) {
        if(!composterRunning || !SkyblockFeatures.config.composterOverlay) return;
        if(whenFuelRunsOut!= 0L && (whenFuelRunsOut-(5*60*1000))<System.currentTimeMillis()) {
            GuiManager.createTitle(ChatFormatting.RED+"Refill Composter Fuel", 30);
            whenFuelRunsOut = 0L;
        }
        if(whenMatRunsOut!= 0L && (whenMatRunsOut-(5*60*1000))<System.currentTimeMillis()) {
            GuiManager.createTitle(ChatFormatting.RED+"Refill Organic Matter", 30);
            whenMatRunsOut = 0L;
        }
    }

    public double getTier(ItemStack stack) {
        List<String> lore = ItemUtils.getItemLore(stack);
        int nextTier = 0;
        for(String line:lore) {
            line = Utils.cleanColor(line);
            if(line.contains("Next Tier")) {
                try {
                    nextTier = Integer.parseInt(line.replaceAll("[^0-9]", ""));
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
        }
        String name = stack.getDisplayName();
        if(name.contains("Speed")) return ((double) nextTier /20)-1;
        if(name.contains("Multi")) return ((double) nextTier /3)-1;
        if(name.contains("Fuel")) return ((double) (nextTier - 100000) /30000)-1;
        if(name.contains("Organic")) return ((double) (nextTier - 40000) /20000)-1;
        if(name.contains("Cost")) return ((double) nextTier)-1;
        return 0;
    }
}
