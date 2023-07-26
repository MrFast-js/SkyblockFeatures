package mrfast.sbf.features.misc;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public static int sec = 0;
    public static double itemCount = 0;

    @SubscribeEvent
    public void onSeconds(SecondPassedEvent event) {
        if(SkyblockFeatures.config.auctionGuis) {
            selfItems.clear();
            if(pricePaidMap.size()==0) {
                readConfig();
            }
        }
    }

    boolean canRefresh = true;

    @SubscribeEvent
    public void onSlotClick(SlotClickedEvent event) {
        // get price from when buying a BIN
        if (event.gui !=null && event.gui instanceof GuiChest && SkyblockFeatures.config.showPricePaid) {
            GuiChest gui = (GuiChest) event.gui;
            ContainerChest chest = (ContainerChest) gui.inventorySlots;
            IInventory inv = chest.getLowerChestInventory();
            String chestName = inv.getDisplayName().getUnformattedText().trim();
            Boolean buySlot = SkyblockFeatures.config.autoAuctionFlipEasyBuy?true:event.slotId==11;

            if(chestName.contains("Confirm Purchase") && buySlot) {
                int pricePaid = 0;
                for(String line:ItemUtils.getItemLore(inv.getStackInSlot(11))) {
                    if(!line.contains("Cost")) continue;
                    String numberOnly = Utils.cleanColor(line).replaceAll("[^0-9]", "");
                    if(numberOnly.length()>0) {
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
    public void onKeyInput(GuiScreenEvent.KeyboardInputEvent keyboardInputEvent) {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (screen instanceof GuiChest && Keyboard.isKeyDown(SkyblockFeatures.reloadAH.getKeyCode()) && canRefresh){
            canRefresh = false;
            ContainerChest ch = (ContainerChest) ((GuiChest)screen).inventorySlots;
            if (!ch.getLowerChestInventory().getName().contains("Auctions")) return;
            int selectedSlot = 0;
            for(int i=0;i<=45;i+=9) {
                ItemStack item = ch.getSlot(i).getStack();
                List<String> lore = ItemUtils.getItemLore(item);
                for(String line:lore) {
                    if(line.contains("Currently")) selectedSlot = i;
                }
            }
            int slot = selectedSlot;
            Utils.setTimeout(()->{
                if(slot<45) {
                    Utils.GetMC().playerController.windowClick(Utils.GetMC().thePlayer.openContainer.windowId, slot+9, 0, 3, Utils.GetMC().thePlayer);
                    Utils.setTimeout(()->{
                        Utils.GetMC().playerController.windowClick(Utils.GetMC().thePlayer.openContainer.windowId,slot, 0, 3, Utils.GetMC().thePlayer);
                    }, 200);
                    Utils.setTimeout(()->{
                        canRefresh = true;
                    }, 500);
                } else {
                    Utils.GetMC().playerController.windowClick(Utils.GetMC().thePlayer.openContainer.windowId, slot-9, 0, 3, Utils.GetMC().thePlayer);
                    Utils.setTimeout(()->{
                        Utils.GetMC().playerController.windowClick(Utils.GetMC().thePlayer.openContainer.windowId,slot, 0, 3, Utils.GetMC().thePlayer);
                    }, 200);
                    Utils.setTimeout(()->{
                        canRefresh = true;
                    }, 500);
                }
            }, 100);
        }
    }
    ItemStack hoverItemStack = null;
    
    @SubscribeEvent
    public void onTick(ClientTickEvent event) {
        if (!Utils.inSkyblock || !SkyblockFeatures.usingNEU || Utils.GetMC().thePlayer == null || Utils.GetMC().thePlayer.openContainer == null) return;
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
        sec=0;
        currentlySellingStack = null;
        gettingData = false;
        canRefresh = true;
    }
    ItemStack currentlySellingStack = null;
    @SubscribeEvent
    public void onSignDraw(DrawSignEvent event) {
        if(currentlySellingStack!=null && SkyblockFeatures.config.auctionGuis) {
            String auctionIdentifier = PricingData.getIdentifier(currentlySellingStack);
            if (auctionIdentifier != null) {
                Double lowestBin = PricingData.lowestBINs.get(auctionIdentifier)*currentlySellingStack.stackSize;
                Double avgBin = PricingData.averageLowestBINs.get(auctionIdentifier)*currentlySellingStack.stackSize;
                int yHeight = (Utils.GetMC().currentScreen.height/8);
                Utils.drawGraySquareWithBorder((Utils.GetMC().currentScreen.width/2)+60, yHeight, 6*("Suggested Listing Price: "+lowestBin.toString()).length(), 5*Utils.GetMC().fontRendererObj.FONT_HEIGHT,3);
                Float priceToSellAt = (float) Math.round(((lowestBin*0.6+avgBin*0.4))*0.99);
                String avgBinString = avgBin != null?ChatFormatting.GOLD+Utils.nf.format(avgBin):ChatFormatting.RED+"Unknown";
                String lowestBinString = lowestBin != null?ChatFormatting.GOLD+Utils.nf.format(lowestBin):ChatFormatting.RED+"Unknown";
                String[] lines = {
                    ChatFormatting.WHITE+"Lowest BIN: "+lowestBinString,
                    ChatFormatting.WHITE+"Average BIN: "+avgBinString,
                    ChatFormatting.WHITE+"Suggested Listing Price: "+ChatFormatting.GOLD+Utils.nf.format(priceToSellAt),
                };
                int lineCount = 0;
                for(String line:lines) {
                    Utils.GetMC().fontRendererObj.drawStringWithShadow(line, (Utils.GetMC().currentScreen.width/2)+70, lineCount*(Utils.GetMC().fontRendererObj.FONT_HEIGHT+1)+10+yHeight, -1);
                    lineCount++;
                }
            }
        }
    }

    public class Auction {
        public Double profit;
        public ItemStack stack;
        public String identifer;
        public String uuid;
        public Slot slot;
        
        public Auction(Double p,ItemStack s,String i,Slot sl,String uuid) {
            profit = p;
            stack = s;
            identifer = i;
            this.uuid = uuid;
            slot = sl;
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
                                Double profit = (avgBinValue*stack.stackSize) - price;
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
                            Double BinValue = PricingData.lowestBINs.get(identifier)*stack.stackSize;
                            if(BinValue != null) {
                                Double profit = (BinValue - price);
                                Boolean dupe = false;
                                Auction auction = new Auction(profit, stack, identifier,event.slot,uuid);

                                for(Auction auc:selfItems) {
                                    if(auc.stack == auction.stack || auc.identifer == auction.identifer || auc.profit == auction.profit) {
                                        dupe = true;
                                    }
                                }

                                if(!dupe || (SkyblockFeatures.usingNEU && (selfItems.size()<itemCount || selfItems.size() == 0))) {
                                    selfItems.add(auction);
                                }
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
                            Boolean dupe = false;
                            Auction auction = new Auction(profit, stack, identifier,event.slot,uuid);

                            for(Auction auc:selfItems) {
                                if(auc.stack == auction.stack || auc.identifer == auction.identifer || auc.profit == auction.profit) {
                                    dupe = true;
                                }
                            }
                            if(!SkyblockFeatures.usingNEU || (SkyblockFeatures.usingNEU && !dupe && (selfItems.size()<itemCount || selfItems.size() == 0))) {
                                selfItems.add(auction);
                            }
                        }
                    }
                }
            }
        }
    }
    static HashMap<String,Integer> pricePaidMap = new HashMap<>();
    static File pricePaidFile = null;
    JsonObject profitData = null;
    boolean gettingData = false;
    @SubscribeEvent
    public void onDrawContainerTitle(TitleDrawnEvent event) {
        if (event.gui !=null && event.gui instanceof GuiChest && SkyblockFeatures.config.auctionGuis) {
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
                        profitData = APIUtils.getJSONResponse("https://sky.coflnet.com/api/flip/stats/player/"+APIUtils.getUUID(Utils.GetMC().thePlayer.getName())+"?days=7");
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
                    String nameOfItem = bestFlip.get("itemName").getAsString().replace("âšš ", "").replaceAll("Â", "").replaceAll("âœª", "✪");
                    if(nameOfItem.length()>20) {
                        nameOfItem = nameOfItem.substring(0, 20)+ChatFormatting.WHITE+"...";
                    }
                    List<String> lines = new ArrayList<>();

                    lines.add(ChatFormatting.WHITE+"7 Day Profit: "+ChatFormatting.GOLD+Utils.nf.format(totalProfit));
                    lines.add(ChatFormatting.WHITE+"Best Flip: ");
                    lines.add(" Item: "+bestFlip.get("itemName").getAsString().replace("âšš ", "").replace("Â", ""));
                    lines.add(" Paid: "+ChatFormatting.LIGHT_PURPLE+Utils.nf.format(bestFlip.get("pricePaid").getAsInt()));
                    lines.add(" Sold: "+ChatFormatting.YELLOW+Utils.nf.format(bestFlip.get("soldFor").getAsInt()));
                    lines.add(" Profit: "+ChatFormatting.GREEN+Utils.nf.format(bestFlip.get("profit").getAsInt()));

                    Utils.drawGraySquareWithBorder(180, 5, 150, (int) (lines.size()*1.4*Utils.GetMC().fontRendererObj.FONT_HEIGHT),3);
                    
                    int lineCount = 0;
                    for(String line:lines) {
                        Utils.GetMC().fontRendererObj.drawStringWithShadow(line, 190, lineCount*(Utils.GetMC().fontRendererObj.FONT_HEIGHT+1)+10, -1);
                        lineCount++;
                    }
                }

                // Utils.SendMessage(ChatFormatting.GREEN+"In the past 7 days you have made "+ChatFormatting.GOLD+Utils.nf.format(totalProfit)+ChatFormatting.GREEN+" coins!");
            }
            // get price from when buying a auction
            if(chestName.contains("Auction View") && !chestName.contains("BIN")) {
                boolean alreadyGotIt = false;
                try {
                    if(inv.getStackInSlot(29).getDisplayName().contains("Collect Auction") && SkyblockFeatures.config.showPricePaid) {
                        boolean canCollectItem = false;
                        int pricePaid = 0;
                        for(String line:ItemUtils.getItemLore(inv.getStackInSlot(29))) {
                            if(Utils.cleanColor(line).contains("collect the item")) {
                                canCollectItem=true;
                            } else {
                                String numberOnly = Utils.cleanColor(line).replaceAll("[^0-9]", "");
                                if(numberOnly.length()>0) {
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
                
                for(Slot slot:gui.inventorySlots.inventorySlots) {
                    if (slot.getHasStack() && !alreadyGotIt  && slot.getSlotIndex() == 13) {
                        ItemStack stack = slot.getStack();
                        alreadyGotIt = true;
                        String auctionIdentifier = PricingData.getIdentifier(stack);
                        if (auctionIdentifier != null) {
                            if(!PricingData.lowestBINs.containsKey(auctionIdentifier)) {
                                continue;
                            }
                            Double lowestBin = PricingData.lowestBINs.get(auctionIdentifier)*stack.stackSize;
                            Double avgBin = PricingData.averageLowestBINs.get(auctionIdentifier)*stack.stackSize;
                            Integer cost = 0;
                            String endTime = ChatFormatting.RED+"Ended";
                            Double resellProfit = 0.0;
                            boolean hasTopBid = false;
                            for(String line : ItemUtils.getItemLore(stack)) {
                                line = Utils.cleanColor(line);
                                if(line.contains("bid:")) {
                                    cost = Integer.parseInt(Utils.cleanColor(line).replaceAll("[^0-9]", ""));
                                    if(lowestBin!=null) resellProfit = lowestBin-cost;
                                    else if(avgBin!=null) resellProfit = avgBin-cost;
                                }
                                if(line.contains("Ends in: ")) {
                                    endTime = Utils.cleanColor(line).replace("Ends in: ", "");
                                }
                                if(line.contains(Utils.GetMC().thePlayer.getName())) {
                                    hasTopBid = true;
                                }
                            }
                            if(resellProfit != 0) {
                                String resellColor = resellProfit>0? ChatFormatting.GREEN+"":ChatFormatting.RED+"";
                                Boolean Manipulated = false;
                                if(lowestBin != null && avgBin!=null) {
                                    if(lowestBin > avgBin+150000) {
                                        Manipulated = true;
                                    } else if(cost > avgBin+150000) {
                                        Manipulated = true;
                                    } else if(cost > lowestBin+150000) {
                                        Manipulated = true;
                                    }
                                }
                                if(avgBin == null && lowestBin!=null) {
                                    if(cost > lowestBin+150000) {
                                        Manipulated = true;
                                    }
                                }
                                cost = (int) Math.floor(cost);
                                String avgBinString = avgBin != null?ChatFormatting.GOLD+Utils.nf.format(avgBin):ChatFormatting.RED+"Unknown";
                                String lowestBinString = lowestBin != null?ChatFormatting.GOLD+Utils.nf.format(lowestBin):ChatFormatting.RED+"Unknown";
                                double putupTax = Math.floor(lowestBin*0.01);
                                double collectAuctionTax = cost>=1000000?Math.floor(lowestBin*0.01):0;
                                double totalTax = Math.floor(putupTax+collectAuctionTax);
                                double profitAfterBid = cost>100000?resellProfit*0.95:resellProfit*0.9;
                                // Add your bid into account

                                String resellTax = collectAuctionTax>0?(Utils.nf.format(totalTax))+ChatFormatting.GRAY+" (2%)":Utils.nf.format(putupTax)+ChatFormatting.GRAY+" (1%)";
                                List<String> lines = new ArrayList<>();

                                lines.add(ChatFormatting.WHITE+"Item Price: "+ChatFormatting.GOLD+Utils.nf.format(cost));
                                lines.add(ChatFormatting.WHITE+"Lowest BIN: "+lowestBinString);
                                lines.add(ChatFormatting.WHITE+"Average BIN: "+avgBinString);
                                lines.add(ChatFormatting.WHITE+"Taxes: "+ChatFormatting.GOLD+resellTax);
                                lines.add(ChatFormatting.WHITE+"Ends: "+ChatFormatting.YELLOW+endTime);
                                lines.add("");
                                if(!hasTopBid) lines.add(ChatFormatting.WHITE+"Profit After Bid: "+resellColor+(Utils.nf.format(Math.floor(profitAfterBid))));
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
                                Utils.drawGraySquareWithBorder(180, 0, 150, (int) (lines.size()*1.29*Utils.GetMC().fontRendererObj.FONT_HEIGHT),3);
                                
                                int lineCount = 0;
                                for(String line:lines) {
                                    Utils.GetMC().fontRendererObj.drawStringWithShadow(line, 190, lineCount*(Utils.GetMC().fontRendererObj.FONT_HEIGHT+1)+10, -1);
                                    lineCount++;
                                }
                            }
                        }
                    }
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
                            Double lowestBin = PricingData.lowestBINs.get(auctionIdentifier)*stack.stackSize;
                            Double avgBin = PricingData.averageLowestBINs.get(auctionIdentifier)*stack.stackSize;
                            Utils.drawGraySquareWithBorder(180, 0, 6*("Suggested Listing Price: "+lowestBin.toString()).length(), 6*Utils.GetMC().fontRendererObj.FONT_HEIGHT,3);
                        
                            String avgBinString = avgBin != null?ChatFormatting.GOLD+Utils.nf.format(avgBin):ChatFormatting.RED+"Unknown";
                            String lowestBinString = lowestBin != null?ChatFormatting.GOLD+Utils.nf.format(lowestBin):ChatFormatting.RED+"Unknown";
                            Float priceToSellAt = (float) Math.round(((lowestBin*0.6+avgBin*0.4))*0.99);
                            JsonObject auctionData = PricingData.getItemAuctionInfo(auctionIdentifier);
                            Integer volume = auctionData.get("sales").getAsInt();
                            Integer sellingFor = 0;
                            try {
                                sellingFor = Integer.parseInt(Utils.cleanColor(inv.getStackInSlot(31).getDisplayName()).split(" ")[2].replaceAll("[^0-9]", ""));
                            } catch (Exception e) {
                                // TODO: handle exception
                            }
                            Double salesPerHour = ((24d/volume)*(sellingFor/lowestBin));
                            
                            String[] lines = {
                                ChatFormatting.WHITE+"Lowest BIN: "+lowestBinString,
                                ChatFormatting.WHITE+"Average BIN: "+avgBinString,
                                ChatFormatting.WHITE+"Suggested Listing Price: "+ChatFormatting.GOLD+Utils.nf.format(priceToSellAt),
                                ChatFormatting.WHITE+"Estimated Time To Sell: "+ChatFormatting.GREEN+getTimeToSellFormat(salesPerHour),
                            };
                            int lineCount = 0;
                            for(String line:lines) {
                                Utils.GetMC().fontRendererObj.drawStringWithShadow(line, 190, lineCount*(Utils.GetMC().fontRendererObj.FONT_HEIGHT+1)+10, -1);
                                lineCount++;
                            }
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
                List<ItemStack> endedAuctions = new ArrayList<ItemStack>();

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
                            endedAuctions.add(stack);
                        }
                        if(line.contains("Expired")) {
                            expired++;
                            endedAuctions.add(stack);
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

                Utils.drawGraySquareWithBorder(180, 0, 150, 8*Utils.GetMC().fontRendererObj.FONT_HEIGHT,3);
 
                String[] lines = {
                    ChatFormatting.GREEN+""+(unclaimed/2)+ChatFormatting.WHITE+" Unclaimed",
                    ChatFormatting.RED+""+expired+ChatFormatting.WHITE+" Expired",
                    "",
                    ChatFormatting.WHITE+"Coins to collect: "+ChatFormatting.GOLD+Utils.nf.format(toCollect),
                    ChatFormatting.WHITE+"Total Ask Value: "+ChatFormatting.GOLD+Utils.nf.format(coins)
                };
                int lineCount = 0;
                for(String line:lines) {
                    Utils.GetMC().fontRendererObj.drawStringWithShadow(line, 190, lineCount*(Utils.GetMC().fontRendererObj.FONT_HEIGHT+1)+10, -1);
                    lineCount++;
                }
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
                        if(line.contains(Utils.GetMC().thePlayer.getName()) && !winningAuctions.contains(auction.identifer)) {
                            winning++;
                            winningAuctions.add(auction.identifer);
                        }
                        else if(line.contains("Bidder")) {
                            int x = auction.slot.xDisplayPosition;
                            int y = auction.slot.yDisplayPosition;
                            if(SkyblockFeatures.config.highlightlosingAuction) Gui.drawRect(x, y, x + 16, y + 16, new Color(255, 35, 35).getRGB());
                            losing++;
                            // endedAuctions.add(stack);
                            profit -= auction.profit;
                        }
                    }
                }

                Utils.drawGraySquareWithBorder(180, 0, 150, 8*Utils.GetMC().fontRendererObj.FONT_HEIGHT,3);
                
                
                String[] lines = {
                    ChatFormatting.GREEN+""+winning+ChatFormatting.WHITE+" Winning Auctions",
                    ChatFormatting.RED+""+losing+ChatFormatting.WHITE+" Losing Auctions",
                    "",
                    ChatFormatting.WHITE+"Ended Auctions: "+ChatFormatting.GOLD+Utils.nf.format(ended),
                    ChatFormatting.WHITE+"Total Profit: "+ChatFormatting.GOLD+Utils.nf.format(profit)
                };
                int lineCount = 0;
                for(String line:lines) {
                    Utils.GetMC().fontRendererObj.drawStringWithShadow(line, 190, lineCount*(Utils.GetMC().fontRendererObj.FONT_HEIGHT+1)+10, -1);
                    lineCount++;
                }
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
        } catch (Exception e) {
            
        }
    }

    public static String getTimeToSellFormat(Double input) {
        Double decimalTime = input;
        decimalTime = decimalTime * 60 * 60;
        Integer hours = (int) Math.floor((decimalTime / (60 * 60)));
        decimalTime = decimalTime - (hours * 60 * 60);
        Integer minutes = (int) Math.floor((decimalTime / 60));
        decimalTime = decimalTime - (minutes * 60);
        Integer seconds = (int) Math.round(decimalTime);
        String output = "";
        if(hours>0) output+=hours+"h ";
        if(minutes>0) output+=minutes+"m ";
        if(minutes==0 && hours==0) {
            output+=Math.max(seconds,20)+"s";
        }
        return output;
    }

    public static void saveConfig() {
        try (FileWriter writer = new FileWriter(pricePaidFile)) {
            gson.toJson(pricePaidMap, writer);
        } catch (Exception ignored) {

        }
    }
}
