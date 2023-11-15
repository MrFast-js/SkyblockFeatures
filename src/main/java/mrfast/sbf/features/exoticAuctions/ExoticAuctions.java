package mrfast.sbf.features.exoticAuctions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import mrfast.sbf.utils.GuiUtils;
import org.apache.commons.codec.binary.Base64InputStream;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.AuctionUtil;
import mrfast.sbf.events.SecondPassedEvent;
import mrfast.sbf.features.exoticAuctions.colors.CrystalColors;
import mrfast.sbf.features.exoticAuctions.colors.FairyColors;
import mrfast.sbf.features.exoticAuctions.colors.PureColors;
import mrfast.sbf.features.exoticAuctions.colors.SpookyColors;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.APIUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ExoticAuctions {
    static boolean checkForNewReloadTime = true;
    static boolean checkingForNewReloadTime = false;
    static boolean foundReloadTime = false;
    static boolean apiUpdated = true;
    static int lastSecond = -1;
    static int lowestSecondFound = 60;
    static int highestSecondFound = 0;
    static int seconds = 50;
    HashMap<String,HashMap<String,Integer>> armorColors = new HashMap<>();
    HashMap<String,String> DefaultArmorColors = new HashMap<>();

    public static class ExoticAuction {
        String auctionId;
        String hexcode;
        String itemName;
        ExoticType type;
        int price;
        Boolean bin;
        long msTillEnd;

        public ExoticAuction(String aucId,String hex,String itemName,String seller,ExoticType type,Boolean bin,Integer price,Long msTillEnd) {
            this.auctionId=aucId;
            this.hexcode=hex;
            this.itemName=itemName;
            this.type=type;
            this.bin=bin;
            this.price=price;
            this.msTillEnd=msTillEnd;
        }
    }


    public enum ExoticType {
        FAIRY(ChatFormatting.LIGHT_PURPLE+"FAIRY"),
        EXOTIC(ChatFormatting.RED+"EXOTIC"),
        PURE(ChatFormatting.YELLOW+"PURE"),
        SPOOKY(ChatFormatting.DARK_PURPLE+"SPOOKY"),
        BLEACH(ChatFormatting.WHITE+"BLEACH"),
        CRYSTAL(ChatFormatting.AQUA+"CRYSTAL");

        public final String color;

        private ExoticType(String color) {
            this.color = color;
        }
    }
    @SubscribeEvent
    public void onSecond(SecondPassedEvent event) {
        if(Utils.inDungeons || !SkyblockFeatures.config.exoticAuctionFinder) return;


        Date date = new Date();
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);
        seconds = calendar.get(Calendar.SECOND)+1;
        if(lastSecond == seconds) return;
        lastSecond = seconds;
        int timeToReload = seconds<lowestSecondFound?lowestSecondFound-seconds:60-seconds+lowestSecondFound;

        if(timeToReload == 10) {
            if((lowestSecondFound!=60 && highestSecondFound!=0)) {
                Utils.SendMessage(ChatFormatting.GRAY+"Scanning for exotic auctions in 10s");
                if(!apiUpdated) {
                    Utils.SendMessage(ChatFormatting.RED+"The API Didnt update when expected! Restarting tingy..");
                    checkForNewReloadTime = true;
                    checkingForNewReloadTime = false;
                    foundReloadTime = false;
                    lastSecond = -1;
                    lowestSecondFound = 60;
                    highestSecondFound = 0;
                }
            }
            apiUpdated = true;
        }
        if (checkForNewReloadTime && !checkingForNewReloadTime) {
            checkingForNewReloadTime = true;
            Utils.SendMessage(ChatFormatting.GREEN + "Wait 3 minutes for the Exotic Auctions to set up.");
            new Thread(() -> {
                JsonObject startingData = APIUtils.getJSONResponse("https://api.hypixel.net/skyblock/auctions?page=0");
                JsonArray startingProducts = startingData.get("auctions").getAsJsonArray();
                String startingUUID = startingProducts.get(0).getAsJsonObject().get("uuid").getAsString();
        
                for (int i = 0; i < 60; i++) {
                    Utils.setTimeout(() -> {
                        if (Utils.inDungeons || !SkyblockFeatures.config.exoticAuctionFinder || foundReloadTime) return;
        
                        JsonObject data = APIUtils.getJSONResponse("https://api.hypixel.net/skyblock/auctions?page=0");
                        JsonArray products = data.get("auctions").getAsJsonArray();
                        String currentUUID = products.get(0).getAsJsonObject().get("uuid").getAsString();
        
                        if (!currentUUID.equals(startingUUID)) {
                            if (seconds < lowestSecondFound) {
                                lowestSecondFound = seconds;
                                Utils.SendMessage(ChatFormatting.GREEN + "Exotic Auctions Stage 1/3");
                            } else if (seconds > highestSecondFound) {
                                highestSecondFound = seconds;
                                Utils.SendMessage(ChatFormatting.GREEN + "Exotic Auctions Stage 2/3");
                            }
                            foundReloadTime = true;
                        }
                    }, i * 1000);
                }
        
                Utils.setTimeout(() -> {
                    if (Utils.inDungeons || !SkyblockFeatures.config.exoticAuctionFinder) return;
        
                    if (lowestSecondFound != 60 && highestSecondFound != 0) {
                        Utils.SendMessage(ChatFormatting.GREEN + "Exotic Auctions Stage 3/3. Setup Complete!");
                        Utils.playSound("random.orb", 0.1);
                        checkForNewReloadTime = false;
                        checkingForNewReloadTime = false;
                    } else {
                        Utils.SendMessage(ChatFormatting.GREEN + "Exotic Auctions Stage 2/3");
                        checkForNewReloadTime = true;
                        checkingForNewReloadTime = false;
                        foundReloadTime = false;
                    }
                }, 60 * 1000);
            }).start();
        }
        // System.out.println((lowestSecondFound!=60 && highestSecondFound!=0)+" "+(!checkForNewReloadTime)+" "+checkingForNewReloadTime+" "+(seconds == lowestSecondFound-1)+" "+(Utils.GetMC().theWorld!=null)+" "+apiUpdated);
        if((lowestSecondFound!=60 && highestSecondFound!=0) && !checkForNewReloadTime && seconds == lowestSecondFound-1 && Utils.GetMC().theWorld!=null && Utils.inSkyblock && apiUpdated) {
            if(DefaultArmorColors.size()<50) {
                Utils.SendMessage(ChatFormatting.GREEN + "Getting Default Armor Colors...");
                Utils.playSound("random.orb", 0.1);
                System.out.println("STARTING DEFAULT ARMOR CHECKING");
                new Thread(() -> {
                    apiUpdated = false;
                    int lengthOfSearch = Math.min(Math.max(highestSecondFound - lowestSecondFound, 8), 12);

                    System.out.println("DAC searching for " + lengthOfSearch + " seconds low:" + lowestSecondFound + " high:" + highestSecondFound);
                    Utils.SendMessage(ChatFormatting.GRAY + "Scanning for auctions..");
                    JsonObject startingData = APIUtils.getJSONResponse("https://api.hypixel.net/skyblock/auctions?page=0");
                    JsonArray startingProducts = startingData.get("auctions").getAsJsonArray();
                    String startingUUID = startingProducts.get(0).getAsJsonObject().get("uuid").getAsString();

                    for (int i = 0; i < 100; i++) {
                        Utils.setTimeout(() -> {
                            if (Utils.inDungeons || !SkyblockFeatures.config.exoticAuctionFinder || apiUpdated) return;

                            JsonObject data = APIUtils.getJSONResponse("https://api.hypixel.net/skyblock/auctions?page=0");
                            JsonArray products = data.get("auctions").getAsJsonArray();
                            String currentUUID = products.get(0).getAsJsonObject().get("uuid").getAsString();

                            if (!currentUUID.equals(startingUUID) && !apiUpdated) {
                                System.out.println("GOt good instance");
                                apiUpdated = true;
                                int pages = data.get("totalPages").getAsInt();

                                for (int b = 0; b < pages; b++) {
                                    JsonObject data2 = APIUtils.getJSONResponse("https://api.hypixel.net/skyblock/auctions?page=" + b);
                                    JsonArray products2 = data2.get("auctions").getAsJsonArray();
                                    System.out.println("Checking page " + b + " size:" + products2.size());
                                    setDefaultArmorColors(products2);
                                }

                                System.out.println("SETTING DEFAULT THINGS");
                                armorColors.forEach((itemName, colors) -> {
                                    String mostCommonHex = "";
                                    int maxCount = 0;

                                    for (Map.Entry<String, Integer> entry : colors.entrySet()) {
                                        String color = entry.getKey();
                                        int count = entry.getValue();

                                        if (mostCommonHex.isEmpty() || count > maxCount) {
                                            mostCommonHex = color;
                                            maxCount = count;
                                        }
                                    }

                                    DefaultArmorColors.put(itemName, mostCommonHex);
                                    System.out.println("Set default color for " + itemName + " to #" + mostCommonHex);
                                    // Do something with the most common hex color
                                });

                                Utils.SendMessage(ChatFormatting.GREEN + "Updated Default Armor Colors");
                                Utils.playSound("random.orb", 0.1);

                                armorColors.clear();
                            }
                        }, i * ((lengthOfSearch * 1000) / 100));
                    }
                }).start();
            }

            // Getting actual auctions
            System.out.println("STARTING AH ARMOR EXOTIC CHECKING " + DefaultArmorColors.size());
            if (DefaultArmorColors.size() > 50) {
                new Thread(() -> {
                    apiUpdated = false;
                    int lengthOfSearch = Math.min(Math.max(highestSecondFound - lowestSecondFound, 8), 12);
                    System.out.println("EAS Searching for " + lengthOfSearch + " seconds low:" + lowestSecondFound + " high:" + highestSecondFound);
                    Utils.SendMessage(ChatFormatting.GRAY + "Scanning for auctions..");
                    JsonObject startingData = APIUtils.getJSONResponse("https://api.hypixel.net/skyblock/auctions?page=0");
                    JsonArray startingProducts = startingData.get("auctions").getAsJsonArray();
                    String startingUUID = startingProducts.get(0).getAsJsonObject().get("uuid").getAsString();
                    for (int i = 0; i < 100; i++) {
                        Utils.setTimeout(() -> {
                            if (Utils.inDungeons || !SkyblockFeatures.config.exoticAuctionFinder || apiUpdated) return;
                            JsonObject data = APIUtils.getJSONResponse("https://api.hypixel.net/skyblock/auctions?page=0");
                            JsonArray products = data.get("auctions").getAsJsonArray();
                            String currentUUID = products.get(0).getAsJsonObject().get("uuid").getAsString();
                            if (!currentUUID.equals(startingUUID) && !apiUpdated) {
                                apiUpdated = true;
                                int pages = data.get("totalPages").getAsInt();
                                System.out.println("Got a good instance, "+pages+" pages");
                                for (int b = 0; b < pages; b++) {
                                    JsonObject data2 = APIUtils.getJSONResponse("https://api.hypixel.net/skyblock/auctions?page=" + b);
                                    JsonArray products2 = data2.get("auctions").getAsJsonArray();
                                    doExoticStuff(products2);
                                }
                            }
                        }, i * ((lengthOfSearch * 1000) / 100));
                    }
                }).start();
            }
        }
    }
    
    public void setDefaultArmorColors(JsonArray products) {
        for(JsonElement entry : products) {
            // Limit number of mesages added because it will crash game if it gets overloaded
            if(entry.isJsonObject()) {
                JsonObject itemData = entry.getAsJsonObject();
                String item_bytes = itemData.get("item_bytes").getAsString();
                Base64InputStream is = new Base64InputStream(new ByteArrayInputStream(item_bytes.getBytes(StandardCharsets.UTF_8)));
                NBTTagCompound nbt=null;
                try {
                    nbt = CompressedStreamTools.readCompressed(is);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    // e.printStackTrace();
                }
                if(nbt==null) continue;

                String skyblockItemID = AuctionUtil.getInternalNameFromNBT(nbt.getTagList("i", 10).getCompoundTagAt(0).getCompoundTag("tag"));

                try {
                    String hex = getHexFromDisplayColor(nbt.getTagList("i", 10).getCompoundTagAt(0).getCompoundTag("tag"));
                    if(hex.equals("UNDYED")) continue;
                    // System.out.println(skyblockItemID+" "+hex);
                    if(armorColors.get(skyblockItemID)==null) {
                        armorColors.put(skyblockItemID, new HashMap<>());
                    }
                    HashMap<String,Integer> colors = armorColors.get(skyblockItemID);
                    if(colors.get(hex)==null) {
                        colors.put(hex, 1);
                    } else {
                        colors.put(hex, colors.get(hex)+1);
                    }
                    armorColors.put(skyblockItemID, colors);
                } catch (Exception e) {
                    e.printStackTrace();
                    // TODO: handle exception
                }
            }
        }
    }
    
    public void doExoticStuff(JsonArray products) {
        int c = 0;
        for(JsonElement entry : products) {
            if(c>10) break;
            // Limit number of mesages added because it will crash game if it gets overloaded
            if(entry.isJsonObject()) {
                JsonObject itemData = entry.getAsJsonObject();
                String item_bytes = itemData.get("item_bytes").getAsString();
                Base64InputStream is = new Base64InputStream(new ByteArrayInputStream(item_bytes.getBytes(StandardCharsets.UTF_8)));
                String auctionId = itemData.get("uuid").toString().replaceAll("\"","");
                String sellerId = itemData.get("auctioneer").toString().replaceAll("\"","");
                NBTTagCompound nbt=null;
                try {
                    nbt = CompressedStreamTools.readCompressed(is);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    // e.printStackTrace();
                }
                if(nbt==null) continue;

                String skyblockItemID = AuctionUtil.getInternalNameFromNBT(nbt.getTagList("i", 10).getCompoundTagAt(0).getCompoundTag("tag"));
                double a = (double) System.currentTimeMillis();
                long msTillEnd = (long) Math.abs(itemData.get("end").getAsDouble()-a);

                if(skyblockItemID.equals("VELVET_TOP_HAT") ||
                   skyblockItemID.equals("CASHMERE_JACKET") ||
                   skyblockItemID.equals("SATIN_TROUSERS") ||
                   skyblockItemID.equals("OXFORD_SHOES") || 
                   skyblockItemID.contains("LEATHER") || skyblockItemID.contains("REAPER")) {
                    continue;
                }
                // Bin Flip
                if(itemData.get("bin").getAsBoolean()) {
                    double binPrice = itemData.get("starting_bid").getAsInt();
                    NBTTagCompound extraAttributes = nbt.getTagList("i", 10).getCompoundTagAt(0).getCompoundTag("tag").getCompoundTag("ExtraAttributes");
                    String name = nbt.getTagList("i", 10).getCompoundTagAt(0).getCompoundTag("tag").getCompoundTag("display").getString("Name");
                    if(getHex(extraAttributes)==-1 || name.contains("✿")) continue;

                    String hex = getHexFromDisplayColor(nbt.getTagList("i", 10).getCompoundTagAt(0).getCompoundTag("tag"));

                    if(isExotic(hex,skyblockItemID)!=null) {
                        ExoticAuction exotic = new ExoticAuction(auctionId, hex, name, APIUtils.getName(sellerId), isExotic(hex, skyblockItemID), true, (int) binPrice, msTillEnd);
                        c++;
                        sendAuctionToChat(exotic);
                    }
                } else {
                    double binPrice = itemData.get("highest_bid_amount").getAsDouble();
                    if(binPrice==0) binPrice = itemData.get("starting_bid").getAsDouble();
                    NBTTagCompound extraAttributes = nbt.getTagList("i", 10).getCompoundTagAt(0).getCompoundTag("tag").getCompoundTag("ExtraAttributes");
                    String name = nbt.getTagList("i", 10).getCompoundTagAt(0).getCompoundTag("tag").getCompoundTag("display").getString("Name");
                    if(getHex(extraAttributes)==-1 || name.contains("✿")) continue;
                    String hex = getHexFromDisplayColor(nbt.getTagList("i", 10).getCompoundTagAt(0).getCompoundTag("tag"));
                    if(isExotic(hex,skyblockItemID)!=null) {
                        ExoticAuction exotic = new ExoticAuction(auctionId, hex, name, APIUtils.getName(sellerId), isExotic(hex, skyblockItemID), false, (int) binPrice, msTillEnd);
                        c++;
                        sendAuctionToChat(exotic);
                    }
                }
            }
        }
    }

    public void sendAuctionToChat(ExoticAuction exotic) {
        String buyType = exotic.bin?"BIN":"AUC";
        String text = "\n§b[SBF] §7"+buyType+" §8| §7"+exotic.type.color+" §8| "+exotic.itemName+" §8| §a#"+exotic.hexcode+" §8| §e"+Utils.formatNumber(exotic.price)+" §8| §e"+Utils.secondsToTime((int) (exotic.msTillEnd/1000));
        IChatComponent message = new ChatComponentText(text);
        message.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/viewauction "+exotic.auctionId));
        message.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(ChatFormatting.GREEN+"/viewauction "+exotic.auctionId)));

        // Utils.playSound("note.pling", 0.5);
        Utils.GetMC().thePlayer.addChatComponentMessage(message);
    }

    public ExoticType isExotic(String hex,String itemID) {
        if(itemID.contains("FAIRY")) return null;
        if(itemID.contains("GREAT_SPOOK")) return null;
        if(itemID.contains("CRYSTAL")) return null;

        if(DefaultArmorColors.get(itemID)==null) return null;
        if(DefaultArmorColors.get(itemID).equals(hex)) {
            return null;
        } else {
            if(SkyblockFeatures.config.bleachedExotics) {if(hex.equals("UNDYED")) return ExoticType.BLEACH;}
            else return null;

            if(SkyblockFeatures.config.spookyExotics) {if(SpookyColors.isSpookColor(hex)) return ExoticType.SPOOKY;}
            else return null;

            if(SkyblockFeatures.config.crystalExotics) {if(CrystalColors.isCrystalColor(hex)) return ExoticType.CRYSTAL;}
            else return null;

            if(SkyblockFeatures.config.fairyExotics) {if(FairyColors.isFairyColor(hex) || FairyColors.isOGFairyColor(itemID,hex)) return ExoticType.FAIRY;}
            else return null;

            if(SkyblockFeatures.config.exoticsExotics) {if(PureColors.isPureColor(hex)) return ExoticType.PURE;}
            else return null;
            
            if(SkyblockFeatures.config.exoticsExotics) {return ExoticType.EXOTIC;}
            else return null;
        }
    }

    /**
     * Modified from iTEM under GNU General Public License v2.0
     * https://github.com/TGWaffles/iTEM/blob/master/LICENSE
     *
     * @author TGWaffles
     */
    public String getHexFromDisplayColor(NBTTagCompound ting) {
        NBTTagCompound displayData = ting.getCompoundTag("display");
        if (!displayData.hasKey("color")) {
            return "UNDYED";
        }
        int colorInt = displayData.getInteger("color");
        return String.format("%06X", colorInt);
    }
    public int getHex(NBTTagCompound extraAttributes) {
        if (extraAttributes.hasKey("color")) {
            // definitely has a color.
            String colorString = extraAttributes.getString("color");
            // expected format is, eg: "208:127:0" for an rr:gg:bb value
            String[] splitColorString = colorString.split(":");
            int colorInt = 0;
            int i = 2;
            for (String colorValueAsString : splitColorString) {
                int colorValue = Integer.parseInt(colorValueAsString);
                // << 16, << 8, << 0
                colorInt += colorValue << (8 * i);
                i--;
            }
            return colorInt;
        }
        return -1;
    }

    private static final Minecraft mc = Minecraft.getMinecraft();

    static {
        new AutoAuctionGui();
    }

    static String display = "Auction API update in 60s";
    
    public static class AutoAuctionGui extends UIElement {
        public AutoAuctionGui() {
            super("Auto Auction Exotic Counter", new Point(0.2f, 0.0f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            if(mc.thePlayer == null || !Utils.inSkyblock) return;
            if (this.getToggled() && Minecraft.getMinecraft().thePlayer != null && mc.theWorld != null) {
                int timeToReload = seconds<lowestSecondFound?lowestSecondFound-seconds:60-seconds+lowestSecondFound;
                GuiUtils.drawText(ChatFormatting.AQUA+""+(Math.max(timeToReload,0))+"s", 0, 0, GuiUtils.TextStyle.BLACK_OUTLINE);
            }
        }
        @Override
        public void drawElementExample() {
            if(mc.thePlayer == null || !Utils.inSkyblock) return;
            GuiUtils.drawText(ChatFormatting.AQUA+"Auction API update in 49s", 0, 0, GuiUtils.TextStyle.BLACK_OUTLINE);
        }

        @Override   
        public boolean getToggled() {
            return Utils.inSkyblock && SkyblockFeatures.config.exoticAuctionFinder;
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
