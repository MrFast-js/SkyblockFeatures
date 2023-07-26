package mrfast.sbf.features.misc;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.codec.binary.Base64InputStream;
import org.lwjgl.input.Keyboard;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.AuctionUtil;
import mrfast.sbf.core.PricingData;
import mrfast.sbf.core.SkyblockInfo;
import mrfast.sbf.events.GuiContainerEvent;
import mrfast.sbf.events.SecondPassedEvent;
import mrfast.sbf.events.SlotClickedEvent;
import mrfast.sbf.features.items.HideGlass;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.APIUtils;
import mrfast.sbf.utils.ItemUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class AutoAuctionFlip {
    static Auction bestAuction = null;
    static boolean sent = false;
    static boolean clicking = false;
    static boolean clicking2 = false;
    static boolean checkForNewReloadTime = true;
    static boolean checkingForNewReloadTime = false;
    static boolean foundReloadTime = false;
    static boolean apiUpdated = true;
    static List<Auction> auctionFlips = new ArrayList<>();
    static int lastSecond = -1;
    static int lowestSecondFound = 60;
    static int highestSecondFound = 0;
    static int seconds = 50;
    static int auctionsFilteredThrough = 0;
    static int messageSent = 0;
    static int auctionsPassedFilteredThrough = 0;

    public class Auction {
        String auctionId = "";
        JsonObject item_Data = null;
        Double profit = 0d;

        public Auction(String aucId,JsonObject itemData,Double profit) {
            this.profit=profit;
            this.auctionId=aucId;
            this.item_Data=itemData;
        }
    }

    public class TotalAuction {
        String name = "";
        Double price = 0d;

        public TotalAuction(String name,Double price) {
            this.price=price;
            this.name=name;
        }
    }
    
    @SubscribeEvent
	public void onClick(SlotClickedEvent event) {
        if (Utils.GetMC().currentScreen instanceof GuiChest) {

            GuiChest gui = (GuiChest) Utils.GetMC().currentScreen;
            ContainerChest chest = (ContainerChest) gui.inventorySlots;
            IInventory inv = chest.getLowerChestInventory();
            String chestName = inv.getDisplayName().getUnformattedText().trim();
            try {
                if(!chestName.contains("Ultrasequencer")) {
                    if(HideGlass.isEmptyGlassPane(event.item)) {
                        event.setCanceled(true);
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
            if(Utils.inDungeons || !SkyblockFeatures.config.autoAuctionFlipEasyBuy) return;

            try {
                if(!HideGlass.isEmptyGlassPane(event.item)) {
                    return;
                }
            } catch (Exception e) {
                // TODO: handle exception
            }

            if(chestName.contains("BIN Auction View") && !clicking) {
                clicking = true;
                Utils.GetMC().playerController.windowClick(Utils.GetMC().thePlayer.openContainer.windowId, 31, 0, 0, Utils.GetMC().thePlayer);
                Utils.setTimeout(()->{
                    AutoAuctionFlip.clicking = false;
                },500);
            }
            else if(chestName.contains("Confirm Purchase") && !clicking2) {
                clicking2 = true;
                Utils.GetMC().playerController.windowClick(Utils.GetMC().thePlayer.openContainer.windowId, 11, 0, 0, Utils.GetMC().thePlayer);
                Utils.setTimeout(()->{
                    AutoAuctionFlip.clicking2 = false;
                },500);
            }
            else if(chestName.contains("Auction View") && !clicking) {
                clicking = true;
                Utils.GetMC().playerController.windowClick(Utils.GetMC().thePlayer.openContainer.windowId, 29, 0, 0, Utils.GetMC().thePlayer);
                Utils.setTimeout(()->{
                    AutoAuctionFlip.clicking = false;
                },500);
            }
            else if(chestName.contains("Confirm Bid") && !clicking2) {
                clicking2 = true;
                Utils.GetMC().playerController.windowClick(Utils.GetMC().thePlayer.openContainer.windowId, 11, 0, 0, Utils.GetMC().thePlayer);
                Utils.setTimeout(()->{
                    AutoAuctionFlip.clicking2 = false;
                },500);
            }
        }
    }

    @SubscribeEvent
    public void onLoad(WorldEvent.Load event) {
        if(Utils.inDungeons || !SkyblockFeatures.config.autoAuctionFlip) {
            lastSecond = -1;
            apiUpdated = true;
            lowestSecondFound = 60;
            highestSecondFound = 0;
            checkForNewReloadTime = true;
            checkingForNewReloadTime = false;
            foundReloadTime = false;
            seconds = 50;
            auctionsFilteredThrough = 0;
            auctionsPassedFilteredThrough = 0;
            bestAuction = null;
            sent = false;
            clicking = false;
            clicking2 = false;
            messageSent = 0;
            return;
        }
    }
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if(!SkyblockFeatures.config.autoAuctionFlip || Utils.inDungeons) return;

        if(Keyboard.isKeyDown(SkyblockFeatures.openBestFlipKeybind.getKeyCode()) && Utils.GetMC().currentScreen==null) {
            if(auctionFlips.size()>0 && !sent) {
                bestAuction = auctionFlips.get(0);
                Utils.GetMC().thePlayer.sendChatMessage("/viewauction "+bestAuction.auctionId);
                sent = true;
                auctionFlips.remove(auctionFlips.get(0));
                Utils.setTimeout(()-> {
                    AutoAuctionFlip.sent = false;
                }, 1000);
            } else {
                Utils.SendMessage(ChatFormatting.RED+"Best flip not found! Keep holding to open next.");
            }
        }
    }

    @SubscribeEvent
    public void onSecond(SecondPassedEvent event) {
        if(!SkyblockFeatures.config.autoAuctionFlip || Utils.inDungeons) return;
        Date date = new Date();
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);
        seconds = calendar.get(Calendar.SECOND)+1;
        if(lastSecond == seconds) return;
        lastSecond = seconds;
        Integer timeToReload = seconds<lowestSecondFound?lowestSecondFound-seconds:60-seconds+lowestSecondFound;

        if(timeToReload == 40) {
            messageSent = 0;
            if((lowestSecondFound!=60 && highestSecondFound!=0)) {
                Utils.SendMessage(ChatFormatting.GRAY+"Filtered out "+Utils.nf.format((auctionsFilteredThrough-auctionsPassedFilteredThrough))+" auctions in the past 60s ");
            }
        }
        if(timeToReload == 60) {
            auctionFlips.clear();
        }
        if(timeToReload == 10) {
            if((lowestSecondFound!=60 && highestSecondFound!=0)) {
                Utils.SendMessage(ChatFormatting.GRAY+"Scanning for auctions in 10s ");
                if(!apiUpdated) {
                    Utils.SendMessage(ChatFormatting.RED+"The API Didnt update when expected! Restarting flipper..");
                    SkyblockFeatures.config.autoAuctionFlip = false;
                    Utils.setTimeout(()->{
                        SkyblockFeatures.config.autoAuctionFlip = true;
                    }, 100);
                }
            }
            auctionsFilteredThrough = 0;
            apiUpdated = true;
            auctionsPassedFilteredThrough = 0;
        }
        if(checkForNewReloadTime && !checkingForNewReloadTime) {
            checkingForNewReloadTime=true;
            Utils.SendMessage(ChatFormatting.GREEN+"Wait 3 minutes for the flipper to setup.");
            new Thread(()->{
                JsonObject startingData = APIUtils.getJSONResponse("https://api.hypixel.net/skyblock/auctions?page=0");
                JsonArray startingProducts = startingData.get("auctions").getAsJsonArray();
                String startingUUID = startingProducts.get(0).getAsJsonObject().get("uuid").getAsString();
                for(int i=0;i<60;i++) {
                    Utils.setTimeout(()->{
                        if(Utils.inDungeons || !SkyblockFeatures.config.autoAuctionFlip) return;
                        if(foundReloadTime) return;
                        JsonObject data = APIUtils.getJSONResponse("https://api.hypixel.net/skyblock/auctions?page=0");
                        JsonArray products = data.get("auctions").getAsJsonArray();
                        String currentUUID = products.get(0).getAsJsonObject().get("uuid").getAsString();
                        if(!currentUUID.equals(startingUUID)) {
                            if(seconds<lowestSecondFound) {
                                lowestSecondFound = seconds;
                                Utils.SendMessage(ChatFormatting.GREEN+"Auction Flipper Stage 1/3");
                            }
                            else if(seconds>highestSecondFound) {
                                highestSecondFound = seconds;
                                Utils.SendMessage(ChatFormatting.GREEN+"Auction Flipper Stage 2/3");
                            }
                            foundReloadTime = true;
                        }
                    }, i*1000);
                }
                Utils.setTimeout(()->{
                    if(Utils.inDungeons || !SkyblockFeatures.config.autoAuctionFlip) return;
                    if(lowestSecondFound!=60 && highestSecondFound!=0) {
                        Utils.SendMessage(ChatFormatting.GREEN+"Auction Flipper Stage 3/3. Setup Complete!");
                        Utils.playSound("random.orb", 0.1);
                        checkForNewReloadTime = false;
                        checkingForNewReloadTime = false;  
                    } else {
                        Utils.SendMessage(ChatFormatting.GREEN+"Auction Flipper Stage 2/3");
                        checkForNewReloadTime = true;
                        checkingForNewReloadTime = false;  
                        foundReloadTime = false;
                    }
                }, 60*1000);
            }).start();
        }
        
        if((lowestSecondFound!=60 && highestSecondFound!=0) && !checkForNewReloadTime && seconds == lowestSecondFound-1 && Utils.GetMC().theWorld!=null && Utils.inSkyblock && apiUpdated) {
            new Thread(()->{
                apiUpdated = false;
                int lengthOfSearch = highestSecondFound-lowestSecondFound;
                lengthOfSearch = Math.max(8,lengthOfSearch);
                lengthOfSearch = Math.min(12, lengthOfSearch);

                System.out.println("Searching for "+lengthOfSearch+" seconds low:"+lowestSecondFound+" high:"+highestSecondFound);
                Utils.SendMessage(ChatFormatting.GRAY+"Scanning for auctions..");
                JsonObject startingData = APIUtils.getJSONResponse("https://api.hypixel.net/skyblock/auctions?page=0");
                JsonArray startingProducts = startingData.get("auctions").getAsJsonArray();
                String startingUUID = startingProducts.get(0).getAsJsonObject().get("uuid").getAsString();
                for(int i=0;i<100;i++) {
                    Utils.setTimeout(()->{
                        if(apiUpdated) {
                            return;
                        }
                        JsonObject data = APIUtils.getJSONResponse("https://api.hypixel.net/skyblock/auctions?page=0");
                        JsonArray products = data.get("auctions").getAsJsonArray();
                        String currentUUID = products.get(0).getAsJsonObject().get("uuid").getAsString();
                        if(!currentUUID.equals(startingUUID) && !apiUpdated) {
                            apiUpdated = true;
                            int pages = data.get("totalPages").getAsInt();
                            for(int b=0;b<pages;b++) {
                                JsonObject data2 = APIUtils.getJSONResponse("https://api.hypixel.net/skyblock/auctions?page="+b);
                                JsonArray products2 = data2.get("auctions").getAsJsonArray();
                                doAuctionFlipStuff(products2);
                            }
                            try {
                                bestAuction = auctionFlips.get(0);
                                if(SkyblockFeatures.config.autoAuctionFlipOpen) {
                                    if(bestAuction != null) {
                                        Utils.GetMC().thePlayer.sendChatMessage("/viewauction "+bestAuction.auctionId);
                                        auctionFlips.remove(auctionFlips.get(0));
                                    }
                                }
                            } catch (Exception e) {
                                Utils.SendMessage(ChatFormatting.RED+"No flips that match your filter found!");
                            }
                        }
                    }, i*((lengthOfSearch*1000)/100));
                }
            }).start();
        }
    }
    
    HashMap<String,Double> totalAuctions = new HashMap<>();
    boolean debugLogging = false;
    public void doAuctionFlipStuff(JsonArray products) {
        for(JsonElement entry : products) {
            // Limit number of mesages added because it will crash game if it gets overloaded
            if(messageSent>50) continue;
            if(entry.isJsonObject()) {
                JsonObject itemData = entry.getAsJsonObject();
                // Bin Flip
                if(itemData.get("bin").getAsBoolean()) {
                    if(!SkyblockFeatures.config.autoFlipBIN) continue;
                    try {
                        String item_bytes = itemData.get("item_bytes").getAsString();
                        Base64InputStream is = new Base64InputStream(new ByteArrayInputStream(item_bytes.getBytes(StandardCharsets.UTF_8)));
                        NBTTagCompound nbt = CompressedStreamTools.readCompressed(is);
                        Double binPrice = (double) itemData.get("starting_bid").getAsInt();
                        String id = AuctionUtil.getInternalnameFromNBT(nbt.getTagList("i", 10).getCompoundTagAt(0).getCompoundTag("tag"));
                        NBTTagCompound extraAttributes = nbt.getTagList("i", 10).getCompoundTagAt(0).getCompoundTag("tag").getCompoundTag("ExtraAttributes");
                        Double lowestBinPrice = PricingData.lowestBINs.get(id);
                        Double avgBinPrice = PricingData.averageLowestBINs.get(id);
                        Float margin = Float.parseFloat(SkyblockFeatures.config.autoAuctionFlipMargin);
                        Float minVolume = Float.parseFloat(SkyblockFeatures.config.autoAuctionFlipMinVolume);
                        Float minPercent = Float.parseFloat(SkyblockFeatures.config.autoAuctionFlipMinPercent);
                        String name = nbt.getTagList("i", 10).getCompoundTagAt(0).getCompoundTag("tag").getCompoundTag("display").getString("Name");

                        if(lowestBinPrice==null||avgBinPrice==null) continue;

                        Integer estimatedPrice = ItemUtils.getEstimatedItemValue(extraAttributes);
                        String auctionId = itemData.get("uuid").toString().replaceAll("\"","");
                        Integer valueOfTheItem = (int) (SkyblockFeatures.config.autoFlipAddEnchAndStar?estimatedPrice:lowestBinPrice);
                        Integer percentage = (int) Math.floor(((valueOfTheItem/binPrice)-1)*100);
                        String[] lore = itemData.get("item_lore").getAsString().split("Â");
                        JsonObject auctionData = PricingData.getItemAuctionInfo(id);
                        Double enchantValue = ItemUtils.getEnchantsWorth(extraAttributes);
                        Double starValue = ItemUtils.getStarCost(extraAttributes);
                        Double profit = valueOfTheItem-binPrice;
                        boolean inBlacklist = false;
                        int volume = 20;

                        if(auctionData!=null) volume = auctionData.get("sales").getAsInt();

                        if(!SkyblockFeatures.config.autoFlipAddEnchAndStar) {
                            if(lowestBinPrice>1.5*avgBinPrice) {
                                valueOfTheItem=avgBinPrice.intValue();
                            }
                        }

                        auctionsFilteredThrough++;
                        // Filters

                        // It worked but too good you never got any flips
                        // if(skyblockfeatures.config.autoAuctionFilterOutManip && binPrice>avgBinPrice) {
                        //     System.out.println(name+" Auction Removed Because AVG BIN Gaurd"+" "+auctionId); 
                        //     continue;
                        // }
                        if(SkyblockFeatures.config.autoAuctionFilterOutPets && id.contains("PET")) {
                            if(debugLogging) System.out.println(name+" Removed Because Pet Filter"+" "+auctionId); 
                            continue;
                        }
                        if(SkyblockFeatures.config.autoAuctionFilterOutSkins && (id.contains("SKIN") || name.toLowerCase().contains("skin"))) {
                            if(debugLogging) System.out.println(name+" Auction Removed Because Skin Filter"+" "+auctionId); 
                            continue;
                        }
                        if(SkyblockFeatures.config.autoAuctionFilterOutRunes && itemData.get("item_name").getAsString().contains("Rune")) {
                            if(debugLogging) System.out.println(name+" Removed Because Rune Filter"+" "+auctionId); 
                            continue;
                        }
                        if(SkyblockFeatures.config.autoAuctionFilterOutDyes && itemData.get("item_name").getAsString().contains("Dye")) {
                            if(debugLogging) System.out.println(name+" Removed Because Dye Filter"+" "+auctionId); 
                            continue;
                        }
                        if(SkyblockFeatures.config.autoAuctionFilterOutFurniture && lore.toString().toLowerCase().contains("furniture")) {
                            if(debugLogging) System.out.println(name+" Removed Because Furniture Filter"+" "+auctionId); 
                            continue;
                        }
                        if(volume<minVolume) {
                            if(debugLogging) System.out.println(name+" Removed Because MinVol Filter "+"Vol: "+volume+" "+auctionId); 
                            continue;
                        }
                        if(percentage<minPercent) {
                            if(debugLogging) System.out.println(name+" Removed Because MinPerc Filter Perc:"+percentage+" "+Utils.nf.format(binPrice)+" "+Utils.nf.format(valueOfTheItem)+" "+auctionId); 
                            continue;
                        }
                        if(profit<margin) {
                            if(debugLogging) System.out.println(name+" Removed Because less than profit margin :"+profit +" Item Value:"+valueOfTheItem+"   Price of item:"+binPrice+" "+auctionId); 
                            continue;
                        }
                        if(SkyblockFeatures.config.autoAuctionBlacklist.length()>1) {
                            try {
                                for(String blacklistedName:SkyblockFeatures.config.autoAuctionBlacklist.split(";")) {
                                    if(Utils.cleanColor(name).toLowerCase().contains(blacklistedName)) inBlacklist = true;
                                }
                            } catch (Exception e) {
                                // TODO: handle exception
                            }
                        }
                        if(inBlacklist) {
                            if(debugLogging) System.out.println(name+" Removed because blacklist"); 
                            continue;
                        }
                        if(SkyblockFeatures.config.autoAuctionFlipSetPurse && SkyblockInfo.getInstance().coins<binPrice) {
                            if(debugLogging) System.out.println(name+" Removed Because Purse Filter");
                            continue;
                        }
                        auctionsPassedFilteredThrough++;
                        
                        if(auctionData!=null) {
                            Auction auction = new Auction(auctionId, itemData, profit);
                            String iprofit = Utils.formatNumber(profit.longValue());
                            String oPrice = Utils.formatNumber(binPrice.longValue());
                            String itemValue = Utils.formatNumber(valueOfTheItem.longValue());
                            String ePrice = Utils.formatNumber(enchantValue.longValue());
                            String sPrice = Utils.formatNumber(starValue.longValue());
                            Boolean dupe = false;
                            for(Auction auc:auctionFlips) if(auc.auctionId==auctionId) dupe = true;
                            if(dupe) continue;
                            auctionFlips.add(auction);

                            IChatComponent message = new ChatComponentText("\n"+ChatFormatting.AQUA+"[SBF] "+ChatFormatting.GRAY+"BIN FLIP "+name+" "+ChatFormatting.GREEN+oPrice+" -> "+itemValue+" (+"+iprofit+" "+ChatFormatting.DARK_RED+percentage+"%"+ChatFormatting.GREEN+") "+
                            ChatFormatting.GRAY+"Vol: "+ChatFormatting.AQUA+(auctionData.get("sales").getAsInt())+" sales/day"+
                            (enchantValue>0?(ChatFormatting.GRAY+" Ench: "+ChatFormatting.AQUA+ePrice):"")+
                            (starValue>0?(ChatFormatting.GRAY+" Stars: "+ChatFormatting.AQUA+sPrice):""));
                            message.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/viewauction "+auctionId));
                            message.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(ChatFormatting.GREEN+"/viewauction "+auctionId)));
                            Utils.playSound("note.pling", 0.5);
                            Utils.GetMC().thePlayer.addChatComponentMessage(message);
                            messageSent++;
                        }
                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                        // TODO: handle exception
                    }
                } else {
                    if(!SkyblockFeatures.config.autoFlipAuction) continue;
                    // Auction Flip
                    try {
                        String item_bytes = itemData.get("item_bytes").getAsString();
                        Base64InputStream is = new Base64InputStream(new ByteArrayInputStream(item_bytes.getBytes(StandardCharsets.UTF_8)));
                        NBTTagCompound nbt = CompressedStreamTools.readCompressed(is);
                        Double a = (double) System.currentTimeMillis();
                        long msTillEnd = (long) Math.abs(itemData.get("end").getAsDouble()-a);
                        Double binPrice = itemData.get("highest_bid_amount").getAsDouble();
                        if(binPrice==0) binPrice = itemData.get("starting_bid").getAsDouble();
                        String id = AuctionUtil.getInternalnameFromNBT(nbt.getTagList("i", 10).getCompoundTagAt(0).getCompoundTag("tag"));
                        NBTTagCompound extraAttributes = nbt.getTagList("i", 10).getCompoundTagAt(0).getCompoundTag("tag").getCompoundTag("ExtraAttributes");
                        String name = nbt.getTagList("i", 10).getCompoundTagAt(0).getCompoundTag("tag").getCompoundTag("display").getString("Name");
                        Double lowestBinPrice = PricingData.lowestBINs.get(id);
                        Double avgBinPrice = PricingData.averageLowestBINs.get(id);
                        if(lowestBinPrice==null||avgBinPrice==null) continue;

                        Float margin = Float.parseFloat(SkyblockFeatures.config.autoAuctionFlipMargin);
                        Float minVolume = Float.parseFloat(SkyblockFeatures.config.autoAuctionFlipMinVolume);
                        Float minPercent = Float.parseFloat(SkyblockFeatures.config.autoAuctionFlipMinPercent);
                        Integer estimatedPrice = ItemUtils.getEstimatedItemValue(extraAttributes);
                        Integer valueOfTheItem = (int) (SkyblockFeatures.config.autoFlipAddEnchAndStar?estimatedPrice:lowestBinPrice);
                        JsonObject auctionData = PricingData.getItemAuctionInfo(id);
                        String auctionId = itemData.get("uuid").toString().replaceAll("\"","");
                        Double enchantValue = ItemUtils.getEnchantsWorth(extraAttributes);;
                        Double starValue = ItemUtils.getStarCost(extraAttributes);
                        boolean inBlacklist = false;
                        int volume = 20;

                        if(auctionData!=null) volume = auctionData.get("sales").getAsInt();
                        if(!SkyblockFeatures.config.autoFlipAddEnchAndStar) if(lowestBinPrice>1.5*avgBinPrice) valueOfTheItem=avgBinPrice.intValue();

                        Double profit = valueOfTheItem-binPrice;
                        double percentage = Math.floor(((valueOfTheItem/binPrice)-1)*100);
                        String[] lore = itemData.get("item_lore").getAsString().split("Â");
                        String stringLore = "";
                        for(int i=0;i<lore.length;i++) {
                            stringLore+=lore[i];
                        }
                        // Add your bid into account
                        if(binPrice>100000) profit*=0.95;
                        else profit*=0.9;

                        auctionsFilteredThrough++;
                        // Filters
                        // It worked but too good you never got any flips
                        // if(skyblockfeatures.config.autoAuctionFilterOutManip && binPrice>avgBinPrice) {
                        //     if(debugLogging) System.out.println(name+" Auction Removed Because AVG BIN Gaurd"+" "+auctionId); 
                        //     continue;
                        // }
                        if(SkyblockFeatures.config.autoAuctionFilterOutPets && name.toLowerCase().contains("[lvl")) {
                            if(debugLogging) System.out.println(name+" Auction Removed Because Pet Filter"+" "+auctionId); 
                            continue;
                        }
                        if(SkyblockFeatures.config.autoAuctionFilterOutSkins && (id.contains("SKIN") || name.toLowerCase().contains("skin"))) {
                            if(debugLogging) System.out.println(name+" Auction Removed Because Skin Filter"+" "+auctionId); 
                            continue;
                        }
                        if(SkyblockFeatures.config.autoAuctionFilterOutRunes && itemData.get("item_name").getAsString().contains("Rune")) {
                            if(debugLogging) System.out.println(name+" Auction Removed Because Rune Filter"+" "+auctionId); 
                            continue;
                        }
                        if(SkyblockFeatures.config.autoAuctionFilterOutDyes && itemData.get("item_name").getAsString().contains("Dye")) {
                            if(debugLogging) System.out.println(name+" Auction Removed Because Dye Filter"+" "+auctionId); 
                            continue;
                        }
                        if(SkyblockFeatures.config.autoAuctionFilterOutFurniture && stringLore.contains("furniture")) {
                            if(debugLogging) System.out.println(name+" Auction Removed Because Furniture Filter"+" "+auctionId); 
                            continue;
                        }
                        if(volume<minVolume) {
                            if(debugLogging) System.out.println(name+" Auction Removed Because MinVol Filter "+"Vol: "+volume+" "+auctionId); 
                            continue;
                        }
                        if(percentage<minPercent) {
                            if(debugLogging) System.out.println(name+" Auction Removed Because MinPerc Filter Perc:"+percentage+" "+Utils.nf.format(binPrice)+" "+Utils.nf.format(valueOfTheItem)+" "+auctionId); 
                            continue;
                        }
                        if(profit<margin) {
                            if(debugLogging) System.out.println(name+" Auction Removed Because less than profit margin :"+profit +" Item Value:"+valueOfTheItem+"   Price of item:"+binPrice+" "+auctionId); 
                            continue;
                        }
                        if(SkyblockFeatures.config.autoAuctionBlacklist.length()>1) {
                            try {
                                for(String blacklistedName:SkyblockFeatures.config.autoAuctionBlacklist.split(";")) {
                                    if(Utils.cleanColor(name).toLowerCase().contains(blacklistedName)) inBlacklist = true;
                                }
                            } catch (Exception e) {
                                // TODO: handle exception
                            }
                        }
                        if(inBlacklist) {
                            if(debugLogging) System.out.println(name+" Auction Removed because blacklist"); 
                            continue;
                        }
                        if(SkyblockFeatures.config.autoAuctionFlipSetPurse && SkyblockInfo.getInstance().coins<binPrice) {
                            if(debugLogging) System.out.println(name+" Auction Removed Because Purse Filter");
                            continue;
                        }
                        if(msTillEnd>60*5*1000) {
                            if(debugLogging) System.out.println(name+" Auction removed because ends in more than 5m "+Utils.msToDuration(itemData.get("end").getAsLong())); 
                            continue;
                        }
                        auctionsPassedFilteredThrough++;
                        
                        if(auctionData!=null) {
                            Auction auction = new Auction(auctionId, itemData, profit);
                            String iprofit = Utils.formatNumber(profit.longValue());
                            String oPrice = Utils.formatNumber(binPrice.longValue());
                            String itemValue = Utils.formatNumber(valueOfTheItem.longValue());
                            String ePrice = Utils.formatNumber(enchantValue.longValue());
                            String sPrice = Utils.formatNumber(starValue.longValue());
                            Boolean dupe = false;
                            for(Auction auc:auctionFlips) if(auc.auctionId==auctionId) dupe = true;
                            if(dupe) continue;

                            auctionFlips.add(auction);
                            String text = "\n"+ChatFormatting.AQUA+"[SBF] "+ChatFormatting.GRAY+"AUC "+name+" "+ChatFormatting.GREEN+oPrice+" -> "+itemValue+" (+"+iprofit+" "+ChatFormatting.DARK_RED+percentage+"%"+ChatFormatting.GREEN+") ";
                            text += ChatFormatting.GRAY+"Vol: "+ChatFormatting.AQUA+(auctionData.get("sales").getAsInt())+" sales/day";
                            if(enchantValue>0) text += ChatFormatting.GRAY+" Ench: "+ChatFormatting.AQUA+ePrice;
                            if(starValue>0) text += ChatFormatting.GRAY+" Stars: "+ChatFormatting.AQUA+sPrice;
                            if(msTillEnd>0) text += ChatFormatting.YELLOW+" "+msToTime((long) msTillEnd);

                            IChatComponent message = new ChatComponentText(text);
                            message.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/viewauction "+auctionId));
                            message.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(ChatFormatting.GREEN+"/viewauction "+auctionId)));
                            Utils.playSound("note.pling", 0.5);
                            Utils.GetMC().thePlayer.addChatComponentMessage(message);
                            messageSent++;
                        }
                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                        // TODO: handle exception
                    }
                }
            }
        }
        auctionFlips.sort((a,b)->{
            return (int) (a.profit-b.profit);
        });
        List<Auction> reversed = new ArrayList<>();
        for(int i=0;i<auctionFlips.size();i++) {
            reversed.add(auctionFlips.get(auctionFlips.size()-i-1));
        }
        auctionFlips = reversed;
    }

    private static final Minecraft mc = Minecraft.getMinecraft();

    static {
        new AutoAuctionGui();
    }

    public String msToTime(long ms) {
        String output = "";
        int seconds = (int) Math.floor((ms / 1000) % 60);
        int minutes = (int) Math.floor((ms / 1000 / 60) % 60);

        if(minutes>0) output+=minutes+"m ";
        if(seconds>0 )output+=seconds+"s";
        
        return output;
    }

    static String display = "Auction API update in 60s";
    
    public static class AutoAuctionGui extends UIElement {
        public AutoAuctionGui() {
            super("Auto Auction Flip Counter", new Point(0.25677082f, 0.4435921f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            if(mc.thePlayer == null || !Utils.inSkyblock) return;
            if (this.getToggled() && Minecraft.getMinecraft().thePlayer != null && mc.theWorld != null) {
                Integer timeToReload = seconds<lowestSecondFound?lowestSecondFound-seconds:60-seconds+lowestSecondFound;
                Utils.drawTextWithStyle("Auction API update in "+(Math.max(timeToReload,0))+"s", 0, 0, 0x00FF00);
            }
        }
        @Override
        public void drawElementExample() {
            if(mc.thePlayer == null || !Utils.inSkyblock) return;
            Utils.drawTextWithStyle("Auction API update in 49s", 0, 0, 0x00FF00);
        }

        @Override   
        public boolean getToggled() {
            return Utils.inSkyblock && SkyblockFeatures.config.autoAuctionFlipCounter && SkyblockFeatures.config.autoAuctionFlip;
        }

        @Override
        public int getHeight() {
            return Utils.GetMC().fontRendererObj.FONT_HEIGHT;
        }

        @Override
        public int getWidth() {
            return Utils.GetMC().fontRendererObj.getStringWidth(display);
        }
    }
}
