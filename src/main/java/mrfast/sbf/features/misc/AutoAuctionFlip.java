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
import net.minecraft.util.MathHelper;
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
    static int earliestApiUpdateTime = 60;
    static int latestApiUpdateTime = 0;
    static int seconds = 50;
    static int auctionsFilteredThrough = 0;
    static int messageSent = 0;
    static int auctionsPassedFilteredThrough = 0;
    static long startMs;
    static int stage = 0;
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
            resetFlipper();
        }
    }
    boolean lastToggle = false;
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if(Utils.GetMC().theWorld==null || Utils.inDungeons) return;
        if(lastToggle!=SkyblockFeatures.config.autoAuctionFlip) {
            lastToggle = SkyblockFeatures.config.autoAuctionFlip;
            resetFlipper();
        }
        if(!SkyblockFeatures.config.autoAuctionFlip) return;

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
        if(!SkyblockFeatures.config.autoAuctionFlip || Utils.inDungeons || Utils.GetMC().theWorld==null) return;
        Date date = new Date();
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);
        seconds = calendar.get(Calendar.SECOND)+1;
        if(lastSecond == seconds) return;
        lastSecond = seconds;
        Integer timeUntilReload = seconds<earliestApiUpdateTime?earliestApiUpdateTime-seconds:60-seconds+earliestApiUpdateTime;

        if(timeUntilReload == 40) {
            messageSent = 0;
            if((earliestApiUpdateTime!=60 && latestApiUpdateTime!=0) && stage == 3) {
                Utils.SendMessage(ChatFormatting.GRAY+"Filtered out "+Utils.nf.format((auctionsFilteredThrough-auctionsPassedFilteredThrough))+" auctions in the past 60s ");
            }
        }
        if(timeUntilReload == 60) {
            auctionFlips.clear();
        }
        if(timeUntilReload == 10) {
            if(earliestApiUpdateTime!=60 && latestApiUpdateTime!=0 && stage==3) {
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

            // Startup sequence (watching hypixel api for updates so it knows when to check the api for updates)
            new Thread(()->{
                JsonObject startingData = APIUtils.getJSONResponse("https://api.hypixel.net/skyblock/auctions?page=0");
                JsonArray startingProducts = startingData.get("auctions").getAsJsonArray();
                String startingUUID = startingProducts.get(0).getAsJsonObject().get("uuid").getAsString();

                for(int i=0;i<60;i++) {
                    Utils.setTimeout(()->{
                        if(Utils.inDungeons || !SkyblockFeatures.config.autoAuctionFlip || foundReloadTime) return;
                        String comparingUUID = startingUUID;
                        JsonObject data = APIUtils.getJSONResponse("https://api.hypixel.net/skyblock/auctions?page=0");
                        JsonArray products = data.get("auctions").getAsJsonArray();
                        String currentUUID = products.get(0).getAsJsonObject().get("uuid").getAsString();
                        
                        if(!currentUUID.equals(comparingUUID)) {
                            System.out.println("API Updated!!");
                            comparingUUID = currentUUID;
                            if(seconds<earliestApiUpdateTime) {
                                earliestApiUpdateTime = seconds;
                                System.out.println("New earliest API second: "+seconds);
                                Utils.SendMessage(ChatFormatting.GREEN+"Auction Flipper Stage 1/3");
                                stage = 1;
                            } else if(seconds>latestApiUpdateTime) {
                                latestApiUpdateTime = seconds;
                                System.out.println("New latest API second: "+seconds);
                                Utils.SendMessage(ChatFormatting.GREEN+"Auction Flipper Stage 2/3");
                                stage = 2;
                            }
                            foundReloadTime = true;
                        }
                    }, i*1000);
                }
                Utils.setTimeout(()->{
                    if(Utils.inDungeons || !SkyblockFeatures.config.autoAuctionFlip) return;

                    if(earliestApiUpdateTime!=60 && latestApiUpdateTime!=0) {
                        Utils.SendMessage(ChatFormatting.GREEN+"Auction Flipper Stage 3/3. Setup Complete!");
                        Utils.playSound("random.orb", 0.1);
                        stage = 3;
                        checkForNewReloadTime = false;
                        checkingForNewReloadTime = false;  
                    } else {
                        Utils.SendMessage(ChatFormatting.GREEN+"Auction Flipper Stage 2/3");
                        stage = 2;
                        checkForNewReloadTime = true;
                        checkingForNewReloadTime = false;  
                        foundReloadTime = false;
                    }
                }, 60*1000);
            }).start();
        }
        
        if((earliestApiUpdateTime!=60 && latestApiUpdateTime!=0) && !checkForNewReloadTime && seconds == earliestApiUpdateTime-1 && Utils.GetMC().theWorld!=null && Utils.inSkyblock && apiUpdated) {
            new Thread(()->{
                apiUpdated = false;
                int lengthOfSearch = MathHelper.clamp_int(latestApiUpdateTime - earliestApiUpdateTime, 8, 12);
                JsonObject startingData = APIUtils.getJSONResponse("https://api.hypixel.net/skyblock/auctions?page=0");
                JsonArray startingProducts = startingData.get("auctions").getAsJsonArray();
                String startingUUID = startingProducts.get(0).getAsJsonObject().get("uuid").getAsString();

                // Status message
                System.out.println("Searching for "+lengthOfSearch+" seconds low:"+earliestApiUpdateTime+" high:"+latestApiUpdateTime);
                Utils.SendMessage(ChatFormatting.GRAY+"Scanning for auctions..");

                for(int i=0;i<100;i++) {
                    Utils.setTimeout(()->{
                        if(apiUpdated) {
                            return;
                        }
                        JsonObject data = APIUtils.getJSONResponse("https://api.hypixel.net/skyblock/auctions?page=0");
                        JsonArray products = data.get("auctions").getAsJsonArray();
                        String currentUUID = products.get(0).getAsJsonObject().get("uuid").getAsString();

                        if(!currentUUID.equals(startingUUID) && !apiUpdated) {
                            System.out.println("Detected API Update");
                            apiUpdated = true;
                            int pages = data.get("totalPages").getAsInt();
                            // Bin Flips dont usually appear past the first 5 pages
                            if(SkyblockFeatures.config.autoAuctionFlip) pages = 5;
                            for(int b=0;b<pages;b++) {
                                JsonObject data2 = APIUtils.getJSONResponse("https://api.hypixel.net/skyblock/auctions?page="+b);
                                JsonArray products2 = data2.get("auctions").getAsJsonArray();
                                filterAndNotifyProfitableAuctions(products2);
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
    public void filterAndNotifyProfitableAuctions(JsonArray products) {
        for(JsonElement entry : products) {
            // Limit number of mesages added because it will crash game if it gets overloaded
            float max = (float) (SkyblockFeatures.config.autoAuctionFlipMinPercent);
            if(messageSent>max) continue;
            
            if(entry.isJsonObject()) {
                JsonObject itemData = entry.getAsJsonObject();
                // Bin Flip
                if(itemData.get("bin").getAsBoolean()) {
                    if(!SkyblockFeatures.config.autoFlipBIN) continue;
                    try {
                        // NBT related
                        String item_bytes = itemData.get("item_bytes").getAsString();
                        Base64InputStream is = new Base64InputStream(new ByteArrayInputStream(item_bytes.getBytes(StandardCharsets.UTF_8)));
                        NBTTagCompound nbt = CompressedStreamTools.readCompressed(is);
                        String id = AuctionUtil.getInternalnameFromNBT(nbt.getTagList("i", 10).getCompoundTagAt(0).getCompoundTag("tag"));
                        NBTTagCompound extraAttributes = nbt.getTagList("i", 10).getCompoundTagAt(0).getCompoundTag("tag").getCompoundTag("ExtraAttributes");
                        String name = nbt.getTagList("i", 10).getCompoundTagAt(0).getCompoundTag("tag").getCompoundTag("display").getString("Name");

                        // Get Current Item Price
                        Double binPrice = (double) itemData.get("starting_bid").getAsInt();

                        // Load lowets and average BIN prices
                        Double lowestBinPrice = PricingData.lowestBINs.get(id);
                        Double avgBinPrice = PricingData.averageLowestBINs.get(id);
                        if(lowestBinPrice==null||avgBinPrice==null) continue;
                        
                        // Item values;

                        if(lowestBinPrice==null||avgBinPrice==null) continue;

                        // Item Values
                        Integer estimatedPrice = ItemUtils.getEstimatedItemValue(extraAttributes);
                        String auctionId = itemData.get("uuid").toString().replaceAll("\"","");
                        Integer valueOfTheItem = (int) (SkyblockFeatures.config.autoFlipAddEnchAndStar?estimatedPrice:lowestBinPrice);
                        Integer percentage = (int) Math.floor(((valueOfTheItem/binPrice)-1)*100);
                        JsonObject auctionData = PricingData.getItemAuctionInfo(id);
                        Long enchantValue = ItemUtils.getEnchantsWorth(extraAttributes);
                        Long starValue = ItemUtils.getStarCost(extraAttributes);
                        Double profit = valueOfTheItem-binPrice;
                        int volume = 20;

                        String[] lore = itemData.get("item_lore").getAsString().split("Â");
                        String stringLore = String.join("", lore);

                        if(auctionData!=null) volume = auctionData.get("sales").getAsInt();

                        // if the lowest bin is over 1.5x the average then its most likely being manipulated so use the average
                        if(!SkyblockFeatures.config.autoFlipAddEnchAndStar) {
                            if(lowestBinPrice>1.5*avgBinPrice) {
                                valueOfTheItem=avgBinPrice.intValue();
                            }
                        }

                        auctionsFilteredThrough++;
                        // Filters
                        if(stringFilter(name,id,auctionId,stringLore)) {
                            continue;
                        }
                        if(priceFilter(volume, percentage, name, auctionId, valueOfTheItem, profit, binPrice, -1)) {
                            continue;
                        }
                        auctionsPassedFilteredThrough++;
                        
                        if(auctionData!=null) {
                            Auction auction = new Auction(auctionId, itemData, profit);
                            String currentProfit = Utils.formatNumber(profit.longValue());
                            String currentPrice = Utils.formatNumber(binPrice.longValue());
                            String itemValue = Utils.formatNumber(valueOfTheItem.longValue());
                            String ePrice = Utils.formatNumber(enchantValue.longValue());
                            String sPrice = Utils.formatNumber(starValue.longValue());
                            Boolean dupe = false;
                            for(Auction auc:auctionFlips) if(auc.auctionId==auctionId) dupe = true;
                            if(dupe) continue;
                            auctionFlips.add(auction);

                            IChatComponent message = new ChatComponentText("\n"+ChatFormatting.AQUA+"[SBF] "+ChatFormatting.GRAY+"BIN FLIP "+name+" "+ChatFormatting.GREEN+currentPrice+" -> "+itemValue+" (+"+currentProfit+" "+ChatFormatting.DARK_RED+percentage+"%"+ChatFormatting.GREEN+") "+
                            
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
                        // NBT related
                        String item_bytes = itemData.get("item_bytes").getAsString();
                        Base64InputStream is = new Base64InputStream(new ByteArrayInputStream(item_bytes.getBytes(StandardCharsets.UTF_8)));
                        NBTTagCompound nbt = CompressedStreamTools.readCompressed(is);
                        String id = AuctionUtil.getInternalnameFromNBT(nbt.getTagList("i", 10).getCompoundTagAt(0).getCompoundTag("tag"));
                        NBTTagCompound extraAttributes = nbt.getTagList("i", 10).getCompoundTagAt(0).getCompoundTag("tag").getCompoundTag("ExtraAttributes");
                        String name = nbt.getTagList("i", 10).getCompoundTagAt(0).getCompoundTag("tag").getCompoundTag("display").getString("Name");

                        Double a = (double) System.currentTimeMillis();
                        long msTillEnd = (long) Math.abs(itemData.get("end").getAsDouble()-a);
                        Double bidPrice = itemData.get("highest_bid_amount").getAsDouble();
                        if(bidPrice==0) bidPrice = itemData.get("starting_bid").getAsDouble();
                        
                        // Load lowets and average BIN prices
                        Double lowestBinPrice = PricingData.lowestBINs.get(id);
                        Double avgBinPrice = PricingData.averageLowestBINs.get(id);
                        if(lowestBinPrice==null||avgBinPrice==null) continue;
                        
                        // Item values
                        Integer estimatedPrice = ItemUtils.getEstimatedItemValue(extraAttributes);
                        Integer valueOfTheItem = (int) (SkyblockFeatures.config.autoFlipAddEnchAndStar?estimatedPrice:lowestBinPrice);
                        JsonObject auctionData = PricingData.getItemAuctionInfo(id);
                        String auctionId = itemData.get("uuid").toString().replaceAll("\"","");
                        Long enchantValue = ItemUtils.getEnchantsWorth(extraAttributes);;
                        Long starValue = ItemUtils.getStarCost(extraAttributes);
                        int volume = 20;

                        if(auctionData!=null) volume = auctionData.get("sales").getAsInt();

                        // Dont add the extra value from stars, and enchants
                        if(!SkyblockFeatures.config.autoFlipAddEnchAndStar) {
                            if(lowestBinPrice>1.5*avgBinPrice) valueOfTheItem=avgBinPrice.intValue();
                        }

                        Double profit = valueOfTheItem-bidPrice;
                        double percentage = Math.floor(((valueOfTheItem/bidPrice)-1)*100);

                        String[] lore = itemData.get("item_lore").getAsString().split("Â");
                        String stringLore = String.join("", lore);

                        // Account for taxes
                        if(bidPrice>100000) profit*=0.95;
                        else profit*=0.9;

                        auctionsFilteredThrough++;
                        // Filters
                        if(stringFilter(name,id,auctionId,stringLore)) {
                            continue;
                        }
                        if(priceFilter(volume, percentage, name, auctionId, valueOfTheItem, profit, bidPrice, msTillEnd)) {
                            continue;
                        }
                        auctionsPassedFilteredThrough++;
                        
                        if(auctionData!=null) {
                            Auction auction = new Auction(auctionId, itemData, profit);
                            String currentProfit = Utils.formatNumber(profit.longValue());
                            String currentPrice = Utils.formatNumber(bidPrice.longValue());
                            String itemValue = Utils.formatNumber(valueOfTheItem.longValue());
                            String ePrice = Utils.formatNumber(enchantValue.longValue());
                            String sPrice = Utils.formatNumber(starValue.longValue());
                            Boolean dupe = false;

                            // Filter out any auctiuons with duplicate ids
                            for(Auction auc:auctionFlips) if(auc.auctionId==auctionId) dupe = true;
                            if(dupe) continue;

                            auctionFlips.add(auction);
                            // [SBF] AUC Spiritual JuJu Shortbow 300k -> 1.3m (+1m 50%) 
                            String text = "\n"+ChatFormatting.AQUA+"[SBF] "+ChatFormatting.GRAY+"AUC "+name+" "+ChatFormatting.GREEN+currentPrice+" -> "+itemValue+" (+"+currentProfit+" "+ChatFormatting.DARK_RED+percentage+"%"+ChatFormatting.GREEN+") ";
                            text += ChatFormatting.GRAY+"Vol: "+ChatFormatting.AQUA+(auctionData.get("sales").getAsInt())+" sales/day";
                            
                            // Additional text if needed
                            if(enchantValue>0) text += ChatFormatting.GRAY+" Ench: "+ChatFormatting.AQUA+ePrice;
                            if(starValue>0) text += ChatFormatting.GRAY+" Stars: "+ChatFormatting.AQUA+sPrice;
                            if(msTillEnd>0) text += ChatFormatting.YELLOW+" "+msToTime((long) msTillEnd);

                            IChatComponent message = new ChatComponentText(text);
                            message.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/viewauction "+auctionId));
                            message.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(ChatFormatting.GREEN+"/viewauction "+auctionId)));
                            
                            // Notify User
                            Utils.playSound("note.pling", 0.5);
                            Utils.GetMC().thePlayer.addChatComponentMessage(message);

                            // Track how many notifys have been sent so it doesnt send too many
                            messageSent++;
                        }
                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                        // TODO: handle exception
                    }
                }
            }
        }
        // Sort the auctionFlips list in descending order of profit using a lambda expression
        auctionFlips.sort((a, b) -> Double.compare(b.profit, a.profit));
    }

    public boolean stringFilter(String itemName,String id,String aucId,String stringLore) {
        boolean returnValue = false;
        if(SkyblockFeatures.config.autoAuctionFilterOutPets && itemName.toLowerCase().contains("[lvl")) {
            if(debugLogging) System.out.println(itemName+" Auction Removed Because Pet Filter"+" "+aucId); 
            returnValue = true;
        }
        if(SkyblockFeatures.config.autoAuctionFilterOutSkins && (id.contains("SKIN") || itemName.toLowerCase().contains("skin"))) {
            if(debugLogging) System.out.println(itemName+" Auction Removed Because Skin Filter"+" "+aucId); 
            returnValue = true;
        }
        if(SkyblockFeatures.config.autoAuctionFilterOutRunes && itemName.contains("Rune")) {
            if(debugLogging) System.out.println(itemName+" Auction Removed Because Rune Filter"+" "+aucId); 
            returnValue = true;
        }
        if(SkyblockFeatures.config.autoAuctionFilterOutDyes && itemName.contains("Dye")) {
            if(debugLogging) System.out.println(itemName+" Auction Removed Because Dye Filter"+" "+aucId); 
            returnValue = true;
        }
        if(SkyblockFeatures.config.autoAuctionFilterOutFurniture && stringLore.contains("furniture")) {
            if(debugLogging) System.out.println(itemName+" Auction Removed Because Furniture Filter"+" "+aucId); 
            returnValue = true;
        }

        if(SkyblockFeatures.config.autoAuctionBlacklist.length()>1) {
            try {
                for(String blacklistedName:SkyblockFeatures.config.autoAuctionBlacklist.split(";")) {
                    if(Utils.cleanColor(itemName).toLowerCase().contains(blacklistedName)) {
                        if(debugLogging) System.out.println(itemName+" Auction Removed because blacklist"); 
                        returnValue = true;
                        break;
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
        return returnValue;
    }

    public boolean priceFilter(int volume,double percentage,String itemName,String aucId,Integer valueOfTheItem,Double profit,Double binPrice,long msTillEnd) {
        boolean returnValue = false;
        float margin = (float) SkyblockFeatures.config.autoAuctionFlipMargin;
        float minVolume = (float) (SkyblockFeatures.config.autoAuctionFlipMinVolume);
        float minPercent = (float) (SkyblockFeatures.config.autoAuctionFlipMinPercent);

        if(volume<minVolume) {
            if(debugLogging) System.out.println(itemName+" Auction Removed Because MinVol Filter "+"Vol: "+volume+" "+aucId); 
            returnValue = true;
        }
        if(percentage<minPercent) {
            if(debugLogging) System.out.println(itemName+" Auction Removed Because MinPerc Filter Perc:"+percentage+" "+Utils.nf.format(binPrice)+" "+Utils.nf.format(valueOfTheItem)+" "+aucId); 
            returnValue = true;
        }
        if(profit<margin) {
            if(debugLogging) System.out.println(itemName+" Auction Removed Because less than profit margin :"+profit +" Item Value:"+valueOfTheItem+"   Price of item:"+binPrice+" "+aucId); 
            returnValue = true;
        }
        if(SkyblockFeatures.config.autoAuctionFlipSetPurse && SkyblockInfo.getInstance().coins<binPrice) {
            if(debugLogging) System.out.println(itemName+" Auction Removed Because Purse Filter");
            returnValue = true;
        }
        if(msTillEnd>60*5*1000) {
            if(debugLogging) System.out.println(itemName+" Auction removed because ends in more than 5m "); 
            returnValue = true;
        }
        // It worked but too good you never got any flips
        // if(skyblockfeatures.config.autoAuctionFilterOutManip && binPrice>avgBinPrice) {
        //     if(debugLogging) System.out.println(name+" Auction Removed Because AVG BIN Gaurd"+" "+auctionId); 
        //     continue;
        // }
        return returnValue;
    }

    public void resetFlipper() {
        lastSecond = -1;
        apiUpdated = true;
        earliestApiUpdateTime = 60;
        latestApiUpdateTime = 0;
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
        startMs = System.currentTimeMillis();
        messageSent = 0;
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
        private ArrayList<String> lines = new ArrayList<>();

        public AutoAuctionGui() {
            super("Auto Auction Flip Counter", new Point(0.2f, 0.0f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        private void updateLines() {
            lines.clear();
            Integer timeUntilReload = seconds < earliestApiUpdateTime ? earliestApiUpdateTime - seconds : 60 - seconds + earliestApiUpdateTime;
            double seconds = Math.floor((System.currentTimeMillis()-startMs)/1000d);

            lines.add(ChatFormatting.GREEN+"Auction API update in " + (Math.max(timeUntilReload, 0)) + "s");
            lines.add(ChatFormatting.GOLD+"Time Elapsed: "+Utils.secondsToTime((int) seconds));
            lines.add(ChatFormatting.YELLOW +(stage != 3 ?  "Stage " + stage : "Flipper Active"));
        }

        @Override
        public void drawElement() {
            if(mc.thePlayer == null || !Utils.inSkyblock) return;
            if (this.getToggled() && Minecraft.getMinecraft().thePlayer != null && mc.theWorld != null) {
                // Update the lines dynamically
                updateLines();
    
                // Calculate the center Y coordinate
                int centerY = (this.getHeight() - (Utils.GetMC().fontRendererObj.FONT_HEIGHT + 2) * lines.size()) / 2;
                for (int i = 0; i < lines.size(); i++) {
                    String text = lines.get(i);
    
                    // Calculate the center X coordinate for each line
                    int centerX = (this.getWidth() - Utils.GetMC().fontRendererObj.getStringWidth(text)) / 2;
    
                    Utils.drawTextWithStyle3(text, centerX, centerY + i * (Utils.GetMC().fontRendererObj.FONT_HEIGHT + 2));
                }
            }
        }

        @Override
        public void drawElementExample() {
            if(mc.thePlayer == null || !Utils.inSkyblock) return;

            // Update the lines dynamically
            updateLines();

            int centerY = (this.getHeight() - (Utils.GetMC().fontRendererObj.FONT_HEIGHT + 2) * lines.size()) / 2;
            for (int i = 0; i < lines.size(); i++) {
                String text = lines.get(i);

                // Calculate the center X coordinate for each line
                int centerX = (this.getWidth() - Utils.GetMC().fontRendererObj.getStringWidth(text)) / 2;

                Utils.drawTextWithStyle3(text, centerX, centerY + i * (Utils.GetMC().fontRendererObj.FONT_HEIGHT + 2));
            }
        }

        @Override   
        public boolean getToggled() {
            return Utils.inSkyblock && SkyblockFeatures.config.autoAuctionFlipCounter && SkyblockFeatures.config.autoAuctionFlip;
        }

        @Override
        public int getHeight() {
            return (Utils.GetMC().fontRendererObj.FONT_HEIGHT+2)*3;
        }

        @Override
        public int getWidth() {
            return Utils.GetMC().fontRendererObj.getStringWidth(display);
        }
    }
}
