package mrfast.sbf.features.misc;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

import mrfast.sbf.utils.GuiUtils;
import net.minecraft.item.ItemBed;
import org.lwjgl.input.Keyboard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.PricingData;
import mrfast.sbf.events.DrawSignEvent;
import mrfast.sbf.events.GuiContainerEvent;
import mrfast.sbf.events.SecondPassedEvent;
import mrfast.sbf.events.SlotClickedEvent;
import mrfast.sbf.events.GuiContainerEvent.TitleDrawnEvent;
import mrfast.sbf.utils.APIUtils;
import mrfast.sbf.utils.ItemUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class AuctionFeatures {
    public static HashMap<ItemStack, Double> items = new HashMap<ItemStack, Double>();
    public static List<Auction> selfItems = new ArrayList<>();
    public static double itemCount = 0;

    @SubscribeEvent
    public void onSeconds(SecondPassedEvent event) {
        if(SkyblockFeatures.config.auctionGuis) {
            selfItems.clear();
            if(pricePaidMap.isEmpty()) {
                readConfig();
            }
        }
    }

    @SubscribeEvent
    public void onSlotClick(SlotClickedEvent event) {
        // get price from when buying a BIN
        if (event.gui instanceof GuiChest && SkyblockFeatures.config.showPricePaid) {
            GuiChest gui = (GuiChest) event.gui;
            ContainerChest chest = (ContainerChest) gui.inventorySlots;
            IInventory inv = chest.getLowerChestInventory();
            String chestName = inv.getDisplayName().getUnformattedText().trim();
            boolean buySlot = SkyblockFeatures.config.autoAuctionFlipEasyBuy || event.slotId == 11;

            if(chestName.contains("Confirm Purchase") && buySlot) {
                int pricePaid = 0;
                for(String line:ItemUtils.getItemLore(inv.getStackInSlot(11))) {
                    if(!line.contains("Cost")) continue;
                    String numberOnly = Utils.cleanColor(line).replaceAll("[^0-9]", "");
                    if(!numberOnly.isEmpty()) {
                        try {
                            pricePaid = Integer.parseInt(numberOnly);
                        } catch (Exception e) {
                            // TODO: handle exception
                        }
                    }
                }

                String uuid = ItemUtils.getItemUUID(inv.getStackInSlot(13));
                if(uuid!=null && pricePaid!=0) {
                    pricePaidMap.put(uuid, pricePaid);
                    saveConfig();
                }
            }
        }
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent event) {
        if (!Utils.inSkyblock || !SkyblockFeatures.usingNEU || Utils.GetMC().thePlayer == null || Utils.GetMC().thePlayer.openContainer == null || !SkyblockFeatures.config.auctionGuis) return;

        itemCount = 0;
        for(ItemStack stack:Utils.GetMC().thePlayer.openContainer.inventoryItemStacks) {
            if(ItemUtils.getRarity(stack) != null) {
                itemCount++;
            }
        }
    }

    @SubscribeEvent
    public void onCloseWindow(GuiContainerEvent.CloseWindowEvent event) {
        if (!Utils.inSkyblock) return;
        items.clear();
        profitData = null;
        currentlySellingStack = null;
        gettingData = false;
    }
    ItemStack currentlySellingStack = null;
    @SubscribeEvent
    public void onSignDraw(DrawSignEvent event) {
        if(currentlySellingStack!=null && SkyblockFeatures.config.auctionGuis) {
            String auctionIdentifier = PricingData.getIdentifier(currentlySellingStack);
            if (auctionIdentifier != null) {
                Double lowestBin = PricingData.lowestBINs.containsKey(auctionIdentifier)?PricingData.lowestBINs.get(auctionIdentifier)*currentlySellingStack.stackSize:0;
                Double avgBin = PricingData.lowestBINs.containsKey(auctionIdentifier)?PricingData.averageLowestBINs.get(auctionIdentifier)*currentlySellingStack.stackSize:0;
                int yHeight = (Utils.GetMC().currentScreen.height/8);
                Float priceToSellAt = (float) Math.round(((lowestBin*0.6+avgBin*0.4))*0.99);
                String avgBinString = ChatFormatting.GOLD+Utils.nf.format(avgBin);
                String lowestBinString = ChatFormatting.GOLD+Utils.nf.format(lowestBin);

                String[] lines = {
                    ChatFormatting.WHITE+"Lowest BIN: "+lowestBinString,
                    ChatFormatting.WHITE+"Average BIN: "+avgBinString,
                    ChatFormatting.WHITE+"Suggested Listing Price: "+ChatFormatting.GOLD+Utils.nf.format(priceToSellAt)
                };
                GuiUtils.drawGraySquareWithBorder((Utils.GetMC().currentScreen.width/2)+60, 70, 6*("Suggested Listing Price: "+ lowestBin).length(),110);
                int lineCount = 0;
                for(String line:lines) {
                    Utils.GetMC().fontRendererObj.drawStringWithShadow(line, (Utils.GetMC().currentScreen.width/2)+70, lineCount*(Utils.GetMC().fontRendererObj.FONT_HEIGHT+1)+10+yHeight, -1);
                    lineCount++;
                }
            }
        }
    }

    public static class Auction {
        public Double profit;
        public ItemStack stack;
        public String identifier;
        public String uuid;
        public Slot slot;
        
        public Auction(Double profit,ItemStack stack,String identifier,Slot slot,String uuid) {
            this.profit = profit;
            this.stack = stack;
            this.identifier = identifier;
            this.uuid = uuid;
            this.slot = slot;
        }
    }

    @SubscribeEvent
    public void onDrawSlots(GuiContainerEvent.DrawSlotEvent.Pre event) {
        if (event.gui instanceof GuiChest) {
            if(event.slot.slotNumber == 0) selfItems.clear();
            GuiChest gui = (GuiChest) event.gui;
            ContainerChest chest = (ContainerChest) gui.inventorySlots;
            IInventory inv = chest.getLowerChestInventory();
            String chestName = inv.getDisplayName().getUnformattedText().trim();

            if (chestName.contains("Auctions") || chestName.contains("Bids")) {
                if (SkyblockFeatures.config.highlightAuctionProfit) {
                    if (event.slot.getHasStack()) {
                        ItemStack stack = event.slot.getStack();
                        int x = event.slot.xDisplayPosition;
                        int y = event.slot.yDisplayPosition;
                        float price = 0;
                        for(String line : ItemUtils.getItemLore(stack)) {
                            if(line.contains("bid:")) {
                                String b = Utils.cleanColor(line);
                                String a = b.replaceAll("[^0-9]", "");
                                price = Float.parseFloat(a);
                            }
                        }
                        String identifier = PricingData.getIdentifier(stack);
                        if (identifier != null && price != 0 && PricingData.lowestBINs.containsKey(identifier)) {
                            Double avgBinValue = PricingData.lowestBINs.get(identifier);
                            if(avgBinValue != null) {
                                double profit = (avgBinValue*stack.stackSize) - price;
                                if (price < (avgBinValue)) {
                                    if(profit > 100000) {
                                        // Draw Green Square
                                        Gui.drawRect(x, y, x + 16, y + 16, new Color(85, 255, 85).getRGB());
                                    }
                                }
                                items.put(stack, profit);
                            }
                        }
                    }
                }
            }
            if(SkyblockFeatures.config.auctionGuis) {
                if(chestName.contains("Your Bids")) {
                    if (event.slot.getHasStack()) {
                        ItemStack stack = event.slot.getStack();
                        float price = 0;
                        for(String line : ItemUtils.getItemLore(stack)) {
                            if(line.contains("bid:")) {
                                String b = Utils.cleanColor(line);
                                String a = b.replaceAll("[^0-9]", "");
                                price = Float.parseFloat(a);
                            }
                        }
                        String identifier = PricingData.getIdentifier(stack);
                        String uuid = ItemUtils.getItemUUID(stack);
                        
                        if (identifier != null && price != 0 && PricingData.lowestBINs.containsKey(identifier)) {
                            double BinValue = PricingData.lowestBINs.get(identifier)*stack.stackSize;
                            Double profit = (BinValue - price);
                            boolean dupe = false;
                            Auction auction = new Auction(profit, stack, identifier, event.slot, uuid);

                            for(Auction auc:selfItems) {
                                if (auc.stack == auction.stack || auc.identifier.equals(auction.identifier) || auc.profit.equals(auction.profit)) {
                                    dupe = true;
                                    break;
                                }
                            }

                            if(!dupe || SkyblockFeatures.usingNEU && selfItems.size() < itemCount) {
                                selfItems.add(auction);
                            }
                        }
                    }
                }

                if(chestName.contains("Manage Auctions")) {
                    if (event.slot.getHasStack()) {
                        ItemStack stack = event.slot.getStack();
                        float price = 0;
                        for(String line : ItemUtils.getItemLore(stack)) {
                            if(line.contains("bid:")) {
                                String b = Utils.cleanColor(line);
                                String a = b.replaceAll("[^0-9]", "");
                                price = Float.parseFloat(a);
                            }
                            if(line.contains("now:")) {
                                String b = Utils.cleanColor(line);
                                String a = b.replaceAll("[^0-9]", "");
                                price = Float.parseFloat(a);
                            }
                            if(line.contains("Sold for:")) {
                                String b = Utils.cleanColor(line);
                                String a = b.replaceAll("[^0-9]", "");
                                price = Float.parseFloat(a);
                            }
                        }
                        String identifier = PricingData.getIdentifier(stack);
                        String uuid = ItemUtils.getItemUUID(stack);

                        if (identifier != null && price != 0) {
                            Double profit = (double) price;
                            boolean dupe = false;
                            Auction auction = new Auction(profit, stack, identifier, event.slot, uuid);

                            for(Auction auc:selfItems) {
                                if (auc.stack == auction.stack || auc.identifier.equals(auction.identifier) || auc.profit.equals(auction.profit)) {
                                    dupe = true;
                                    break;
                                }
                            }
                            if(!SkyblockFeatures.usingNEU || !dupe && (selfItems.size() < itemCount || selfItems.isEmpty())) {
                                selfItems.add(auction);
                            }
                        }
                    }
                }
            }
        }
    }
    public static HashMap<String,Integer> pricePaidMap = new HashMap<>();
    static File pricePaidFile = null;
    JsonObject profitData = null;
    boolean gettingData = false;
    @SubscribeEvent
    public void onDrawContainerTitle(TitleDrawnEvent event) {
        if (event.gui instanceof GuiChest && SkyblockFeatures.config.auctionGuis) {
            GuiChest gui = (GuiChest) event.gui;
            ContainerChest chest = (ContainerChest) gui.inventorySlots;
            IInventory inv = chest.getLowerChestInventory();
            String chestName = inv.getDisplayName().getUnformattedText().trim();
            Double profit = (double) 0;
            for (Auction auction:selfItems) profit += auction.profit;

            if(chestName.contains("Auction Stats")) {
                if(profitData==null && !gettingData) {
                    gettingData = true;
                    new Thread(()->{
                        profitData = APIUtils.getJSONResponse("https://sky.coflnet.com/api/flip/stats/player/"+APIUtils.getUUID(Utils.GetMC().thePlayer.getName())+"?days=14");
                    }).start();
                }
                if(profitData!=null) {
                    Integer totalProfit = profitData.get("totalProfit").getAsInt();
                    JsonObject bestFlip = null;
                    JsonArray flips = profitData.get("flips").getAsJsonArray();
                    for(JsonElement a:flips) {
                        JsonObject flip = a.getAsJsonObject();
                        if(bestFlip==null) {
                            bestFlip=flip;
                        } else {
                            if(flip.get("profit").getAsInt()>bestFlip.get("profit").getAsInt()) {
                                bestFlip=flip;
                            }
                        }
                    }
                    
                    if(bestFlip==null) return;
                    List<String> lines = new ArrayList<>();

                    lines.add(ChatFormatting.WHITE+"7 Day Profit: "+ChatFormatting.GOLD+Utils.nf.format(totalProfit));
                    lines.add(ChatFormatting.WHITE+"Best Flip: ");
                    lines.add(" Item: "+bestFlip.get("itemName").getAsString().replace("âšš ", "").replace("Â", ""));
                    lines.add(" Paid: "+ChatFormatting.LIGHT_PURPLE+Utils.nf.format(bestFlip.get("pricePaid").getAsInt()));
                    lines.add(" Sold: "+ChatFormatting.YELLOW+Utils.nf.format(bestFlip.get("soldFor").getAsInt()));
                    lines.add(" Profit: "+ChatFormatting.GREEN+Utils.nf.format(bestFlip.get("profit").getAsInt()));

                    GuiUtils.drawSideMenu(lines, GuiUtils.TextStyle.DROP_SHADOW);
                }
            }
            // get price from when buying a auction
            if(chestName.contains("Auction View")) {
                try {
                    if(inv.getStackInSlot(29).getDisplayName().contains("Collect Auction") && SkyblockFeatures.config.showPricePaid) {
                        boolean canCollectItem = false;
                        int pricePaid = 0;
                        for(String line:ItemUtils.getItemLore(inv.getStackInSlot(29))) {
                            if(Utils.cleanColor(line).contains("collect the item")) {
                                canCollectItem=true;
                            } else {
                                String numberOnly = Utils.cleanColor(line).replaceAll("[^0-9]", "");
                                if(!numberOnly.isEmpty()) {
                                    try {
                                        pricePaid = Integer.parseInt(numberOnly);
                                    } catch (Exception e) {
                                        // TODO: handle exception
                                    }
                                }
                            }
                        }
    
                        if(canCollectItem && pricePaid!=0) {
                            String uuid = ItemUtils.getItemUUID(inv.getStackInSlot(13));
                            if(uuid!=null) {
                                pricePaidMap.put(uuid, pricePaid);
                                saveConfig();
                            }
                        }
                    }
                } catch (Exception e) {
                    // TODO: handle exception
                }

                ItemStack stack = gui.inventorySlots.getSlot(13).getStack();
                String auctionIdentifier = PricingData.getIdentifier(stack);

                if (auctionIdentifier != null && PricingData.lowestBINs.containsKey(auctionIdentifier)) {
                    ItemStack newBidStack = gui.inventorySlots.getSlot(29).getStack();
                    long newBid = 0;
                    for(String line : ItemUtils.getItemLore(newBidStack)) {
                        line = Utils.cleanColor(line);
                        if(line.contains("New Bid")) {
                            newBid = Long.parseLong(Utils.cleanColor(line).replaceAll("[^0-9]", ""));
                        }
                    }

                    Long lowestBin = (long) (PricingData.lowestBINs.get(auctionIdentifier)*stack.stackSize);
                    Long avgBin = (long) (PricingData.averageLowestBINs.get(auctionIdentifier)*stack.stackSize);
                    Long cost = 0L;
                    String endTime = ChatFormatting.WHITE+"Status: "+ChatFormatting.RED+"Ended";
                    boolean binAuc = false;
                    long resellProfit = 0L;
                    boolean hasTopBid = false;

                    for(String line : ItemUtils.getItemLore(stack)) {
                        line = Utils.cleanColor(line);
                        if(line.contains("Sold for")) {
                            endTime = ChatFormatting.WHITE+"Status: "+ChatFormatting.DARK_GREEN+ChatFormatting.BOLD+"Sold";
                        }
                        if(line.contains("Buy it now:")) {
                            binAuc = true;
                        }
                        if(line.contains("bid:")||line.contains("Buy it now:")||line.contains("Sold for:")) {
                            try {
                                cost = Long.parseLong(line.replaceAll("[^0-9]", ""));
                                resellProfit = lowestBin-cost;
                            } catch (Exception ignored) {}
                        }
                        if(line.contains("Ends in: ")) {
                            endTime = ChatFormatting.WHITE+"Ends: "+ChatFormatting.YELLOW+line.split(": ")[1];
                        }
                        if(line.contains(Utils.GetMC().thePlayer.getName())) {
                            hasTopBid = true;
                        }
                    }

                    String resellColor = resellProfit>0? ChatFormatting.GREEN+"":ChatFormatting.RED+"";
                    boolean Manipulated = lowestBin > avgBin + 300000 || cost > avgBin + 300000 || cost > lowestBin + 300000;

                    cost = (long) Math.floor(cost);
                    String avgBinString = ChatFormatting.GOLD+Utils.nf.format(avgBin);
                    String lowestBinString = ChatFormatting.GOLD+Utils.nf.format(lowestBin);
                    Long putupTax = (long) Math.floor(lowestBin*0.01);
                    long collectAuctionTax = cost>=1000000? (long) Math.floor(lowestBin * 0.01) :0;
                    Long totalTax = (long) Math.floor(putupTax+collectAuctionTax);

                    long profitAfterBid = resellProfit-newBid;

                    String resellTax = collectAuctionTax>0?(Utils.nf.format(totalTax))+ChatFormatting.GRAY+" (1%)":Utils.nf.format(putupTax)+ChatFormatting.GRAY+" (1%)";
                    List<String> lines = new ArrayList<>();

                    lines.add(ChatFormatting.WHITE+"Item Price: "+ChatFormatting.GOLD+Utils.nf.format(cost));
                    lines.add(ChatFormatting.WHITE+"Lowest BIN: "+lowestBinString);
                    lines.add(ChatFormatting.WHITE+"Average BIN: "+avgBinString);
                    if(!binAuc) lines.add(ChatFormatting.WHITE+"Taxes: "+ChatFormatting.GOLD+resellTax);
                    lines.add(endTime);
                    lines.add("");
                    if(!binAuc&&!hasTopBid) lines.add(ChatFormatting.WHITE+"Profit After Bid: "+resellColor+(Utils.nf.format(Math.floor(profitAfterBid))));
                    else lines.add(ChatFormatting.WHITE+"Resell Profit: "+resellColor+(Utils.nf.format(Math.floor(resellProfit))));

                    if(Manipulated) {
                        lines.add("");
                        lines.add(ChatFormatting.RED+""+ChatFormatting.BOLD+"Warning! This items price");
                        lines.add(ChatFormatting.RED+""+ChatFormatting.BOLD+"is higher than usual!");
                    }
                    if(stack.getDisplayName().contains("Minion Skin")) {
                        lines.add("");
                        lines.add(ChatFormatting.RED+""+ChatFormatting.BOLD+"Warning! Minion skins are");
                        lines.add(ChatFormatting.RED+""+ChatFormatting.BOLD+"often manipulated!!");
                    }
                    GuiUtils.drawSideMenu(lines, GuiUtils.TextStyle.DROP_SHADOW);
                }
            }

            if(chestName.contains("Create")) {
                boolean alreadyGotIt = false;
                for(Slot slot:gui.inventorySlots.inventorySlots) {
                    if (slot.getHasStack() && slot.getSlotIndex() == 13 && !alreadyGotIt) {
                        alreadyGotIt = true;
                        ItemStack stack = slot.getStack();
                        String auctionIdentifier = PricingData.getIdentifier(stack);
                        if (auctionIdentifier != null) {
                            Double lowestBin = PricingData.lowestBINs.get(auctionIdentifier);
                            Double avgBin = PricingData.averageLowestBINs.get(auctionIdentifier);
                            String avgBinString = ChatFormatting.RED+"Unknown";
                            if(avgBin!=null) avgBinString = ChatFormatting.GOLD+Utils.nf.format(avgBin*stack.stackSize);

                            String lowestBinString = ChatFormatting.RED+"Unknown";
                            if(lowestBin!=null) lowestBinString = ChatFormatting.GOLD+Utils.nf.format(lowestBin*stack.stackSize);

                            Float priceToSellAt = 0f;
                            if(lowestBin!=null&&avgBin!=null) priceToSellAt = (float) Math.round(((lowestBin*0.6+avgBin*0.4))*0.99);

                            JsonObject auctionData = PricingData.getItemAuctionInfo(auctionIdentifier);
                            if(auctionData==null) continue;
                            int volume = auctionData.get("sales").getAsInt();
                            int sellingFor = 0;
                            try {
                                sellingFor = Integer.parseInt(Utils.cleanColor(inv.getStackInSlot(31).getDisplayName()).split(" ")[2].replaceAll("[^0-9]", ""));
                            } catch (Exception e) {
                                // TODO: handle exception
                            }
                            // Estimating time to sell based on the price and average sales
                            double salesPerHour = Math.max(((24d/volume)*(sellingFor/lowestBin))*60*60,20);

                            String[] lines = {
                                ChatFormatting.WHITE+"Lowest BIN: "+lowestBinString,
                                ChatFormatting.WHITE+"Average BIN: "+avgBinString,
                                ChatFormatting.WHITE+"Suggested Listing Price: "+ChatFormatting.GOLD+Utils.nf.format(priceToSellAt),
                                ChatFormatting.WHITE+"Estimated Time To Sell: "+ChatFormatting.GREEN+Utils.secondsToTime((int) salesPerHour),
                            };
                            GuiUtils.drawSideMenu(Arrays.asList(lines), GuiUtils.TextStyle.DROP_SHADOW);
                            currentlySellingStack = stack;
                        }
                    }
                }
            }
            
            if(chestName.contains("Manage Auctions")) {
                int unclaimed = 0;
                int expired = 0;
                int coins = 0;
                int toCollect = 0;

                for (Auction auction : selfItems) {
                    ItemStack stack = auction.stack;
                    for(String line : ItemUtils.getItemLore(stack)) {
                        line = Utils.cleanColor(line);
                        if(line.contains("Ended")) {
                            unclaimed++;
                        }
                        if(line.contains("Sold for: ") || line.contains("Status: Sold")) {
                            unclaimed++;
                            try {
                                toCollect+=Integer.parseInt(line.replaceAll("[^0-9]", ""));
                            } catch (Exception e) {
                                //TODO: handle exception
                            }
                        }
                        if(line.contains("Expired")) {
                            expired++;
                        }
                        if((line.contains("Buy it now:") || line.contains("Top bid:"))) {
                            try {
                                coins+=Integer.parseInt(line.replaceAll("[^0-9]", ""));
                            } catch (Exception e) {
                                //TODO: handle exception
                            }
                        }
                    }
                }

                String[] lines = {
                    ChatFormatting.GREEN+""+(unclaimed/2)+ChatFormatting.WHITE+" Unclaimed",
                    ChatFormatting.RED+""+expired+ChatFormatting.WHITE+" Expired",
                    "",
                    ChatFormatting.WHITE+"Coins to collect: "+ChatFormatting.GOLD+Utils.nf.format(toCollect),
                    ChatFormatting.WHITE+"Total Ask Value: "+ChatFormatting.GOLD+Utils.nf.format(coins)
                };
                GuiUtils.drawSideMenu(Arrays.asList(lines), GuiUtils.TextStyle.DROP_SHADOW);
            }
            
            if(chestName.contains("Your Bids")) {
                int ended = 0;
                int winning = 0;
                int losing = 0;
                List<String> winningAuctions = new ArrayList<String>();
                for (Auction auction : selfItems) {
                    ItemStack stack = auction.stack;
                    for(String line : ItemUtils.getItemLore(stack)) {
                        line = Utils.cleanColor(line);
                        if(line.contains("Ended")) {
                            ended++;
                        }
                        if(line.contains(Utils.GetMC().thePlayer.getName()) && !winningAuctions.contains(auction.identifier)) {
                            winning++;
                            winningAuctions.add(auction.identifier);
                        }
                        else if(line.contains("Bidder")) {
                            int x = auction.slot.xDisplayPosition;
                            int y = auction.slot.yDisplayPosition;
                            if(SkyblockFeatures.config.highlightlosingAuction) Gui.drawRect(x, y, x + 16, y + 16, new Color(255, 35, 35).getRGB());
                            losing++;
                            profit -= auction.profit;
                        }
                    }
                }
                
                String[] lines = {
                    ChatFormatting.GREEN+""+winning+ChatFormatting.WHITE+" Winning Auctions",
                    ChatFormatting.RED+""+losing+ChatFormatting.WHITE+" Losing Auctions",
                    "",
                    ChatFormatting.WHITE+"Ended Auctions: "+ChatFormatting.GOLD+Utils.nf.format(ended),
                    ChatFormatting.WHITE+"Total Profit: "+ChatFormatting.GOLD+Utils.nf.format(profit)
                };
                GuiUtils.drawSideMenu(Arrays.asList(lines), GuiUtils.TextStyle.DROP_SHADOW);
            }
        }
    }
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public AuctionFeatures() {
        pricePaidFile = new File(SkyblockFeatures.modDir, "pricePaid.json");
        readConfig();
    }

    public static void readConfig() {
        if(Utils.GetMC().thePlayer==null) return;
        JsonObject file;
        try (FileReader in = new FileReader(pricePaidFile)) {
            file = gson.fromJson(in, JsonObject.class);
            for (Map.Entry<String, JsonElement> e : file.entrySet()) {
                pricePaidMap.put(e.getKey(), e.getValue().getAsInt());
            }
        } catch (Exception ignored) {
            
        }
    }

    public static void saveConfig() {
        try (FileWriter writer = new FileWriter(pricePaidFile)) {
            gson.toJson(pricePaidMap, writer);
        } catch (Exception ignored) {

        }
    }
}
