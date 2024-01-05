package mrfast.sbf.features.misc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;
import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.AuctionUtil;
import mrfast.sbf.core.PricingData;
import mrfast.sbf.core.SkyblockInfo;
import mrfast.sbf.events.SlotClickedEvent;
import mrfast.sbf.events.SocketMessageEvent;
import mrfast.sbf.features.items.HideGlass;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.APIUtils;
import mrfast.sbf.utils.GuiUtils;
import mrfast.sbf.utils.ItemUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.codec.binary.Base64InputStream;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class AutoAuctionFlip {
    static Auction bestAuction = null;
    static boolean sent = false;
    static boolean clicking = false;
    static boolean clicking2 = false;
    static List<Auction> auctionFlips = new ArrayList<>();
    static int auctionsFilteredThrough = 0;
    static int messageSent = 0;
    static int auctionsPassedFilteredThrough = 0;
    static long startMs;

    public static class Auction {
        String auctionId;
        JsonObject item_Data;
        Long profit;

        public Auction(String aucId, JsonObject itemData, Long profit) {
            this.profit = profit;
            this.auctionId = aucId;
            this.item_Data = itemData;
        }
    }

    @SubscribeEvent
    public void onSocketMessage(SocketMessageEvent event) {
        if (!SkyblockFeatures.config.aucFlipperEnabled) return;

        if (event.type.equals("event") && event.message.equals("AuctionUpdate")) {
            getNewAuctions();
        }
    }

    public void getNewAuctions() {
        auctionFlips.clear();
        new Thread(() -> {
            int pages = 30; // Can be changed in the future

            // Check pages for auctions
            for (int b = 0; b < pages; b++) {
                JsonObject data = APIUtils.getJSONResponse("https://api.hypixel.net/skyblock/auctions?page=" + b, new String[]{}, false, false);
                if (data != null) {
                    JsonArray products = data.get("auctions").getAsJsonArray();
                    filterAndNotifyProfitableAuctions(products);

                    if (b == pages - 1) {
                        if (!auctionFlips.isEmpty()) {
                            bestAuction = auctionFlips.get(0);
                            if (SkyblockFeatures.config.autoAuctionFlipOpen) {
                                if (bestAuction != null) {
                                    Utils.GetMC().thePlayer.sendChatMessage("/viewauction " + bestAuction.auctionId);
                                    auctionFlips.remove(auctionFlips.get(0));
                                }
                            }
                        } else {
                            Utils.sendMessage(ChatFormatting.RED + "No flips that match your filter found!");
                        }
                    }
                }
            }
        }).start();
    }

    @SubscribeEvent
    public void onClick(SlotClickedEvent event) {
        if (Utils.GetMC().currentScreen instanceof GuiChest) {

            GuiChest gui = (GuiChest) Utils.GetMC().currentScreen;
            ContainerChest chest = (ContainerChest) gui.inventorySlots;
            IInventory inv = chest.getLowerChestInventory();
            String chestName = inv.getDisplayName().getUnformattedText().trim();
            try {
                if (!chestName.contains("Ultrasequencer")) {
                    if (HideGlass.isEmptyGlassPane(event.item)) {
                        event.setCanceled(true);
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
            if (Utils.inDungeons || !SkyblockFeatures.config.autoAuctionFlipEasyBuy) return;

            try {
                if (!HideGlass.isEmptyGlassPane(event.item)) {
                    return;
                }
            } catch (Exception e) {
                // TODO: handle exception
            }

            if (chestName.contains("BIN Auction View") && !clicking) {
                clicking = true;
                Utils.GetMC().playerController.windowClick(Utils.GetMC().thePlayer.openContainer.windowId, 31, 0, 0, Utils.GetMC().thePlayer);
                Utils.setTimeout(() -> {
                    AutoAuctionFlip.clicking = false;
                }, 500);
            } else if (chestName.contains("Confirm Purchase") && !clicking2) {
                clicking2 = true;
                Utils.GetMC().playerController.windowClick(Utils.GetMC().thePlayer.openContainer.windowId, 11, 0, 0, Utils.GetMC().thePlayer);
                Utils.setTimeout(() -> {
                    AutoAuctionFlip.clicking2 = false;
                }, 500);
            } else if (chestName.contains("Auction View") && !clicking) {
                clicking = true;
                Utils.GetMC().playerController.windowClick(Utils.GetMC().thePlayer.openContainer.windowId, 29, 0, 0, Utils.GetMC().thePlayer);
                Utils.setTimeout(() -> {
                    AutoAuctionFlip.clicking = false;
                }, 500);
            } else if (chestName.contains("Confirm Bid") && !clicking2) {
                clicking2 = true;
                Utils.GetMC().playerController.windowClick(Utils.GetMC().thePlayer.openContainer.windowId, 11, 0, 0, Utils.GetMC().thePlayer);
                Utils.setTimeout(() -> {
                    AutoAuctionFlip.clicking2 = false;
                }, 500);
            }
        }
    }

    @SubscribeEvent
    public void onLoad(WorldEvent.Load event) {
        if (Utils.inDungeons || !SkyblockFeatures.config.aucFlipperEnabled) {
            resetFlipper();
        }
    }

    boolean lastToggle = false;
    boolean lastKeyStatus = false;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (Utils.GetMC().theWorld == null || Utils.inDungeons) return;
        if (lastToggle != SkyblockFeatures.config.aucFlipperEnabled) {
            lastToggle = SkyblockFeatures.config.aucFlipperEnabled;
            resetFlipper();
        }

        if (!SkyblockFeatures.config.aucFlipperEnabled) return;

        try {
            boolean down = Mouse.isButtonDown(SkyblockFeatures.config.autoAuctionFlipOpenKeybind) || Keyboard.isKeyDown(SkyblockFeatures.config.autoAuctionFlipOpenKeybind);
            if (Utils.GetMC().currentScreen == null) {
                if (down) {
                    if (!auctionFlips.isEmpty() && !sent) {

                        bestAuction = auctionFlips.get(0);
                        Utils.GetMC().thePlayer.sendChatMessage("/viewauction " + bestAuction.auctionId);
                        sent = true;
                        auctionFlips.remove(auctionFlips.get(0));
                        Utils.setTimeout(() -> {
                            AutoAuctionFlip.sent = false;
                        }, 1000);
                    }
                }
                if (lastKeyStatus != down && down) {
                    Utils.sendMessage(ChatFormatting.RED + "Best flip not found! Keep holding to open next.");
                }
                lastKeyStatus = down;
            }
        } catch (Exception ignored) {

        }
    }

    boolean canToggle = true;

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent keyboardInputEvent) {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        try {
            if (screen == null && Keyboard.isKeyDown(SkyblockFeatures.config.aucFlipperKeybind) && canToggle) {
                SkyblockFeatures.config.aucFlipperEnabled = !SkyblockFeatures.config.aucFlipperEnabled;
                canToggle = false;
                Utils.setTimeout(() -> {
                    canToggle = true;
                }, 1000);
            }
        } catch (Exception ignored) {

        }
    }


    public void filterAndNotifyProfitableAuctions(JsonArray products) {
        if (SkyblockFeatures.config.debugAuctionFlipper) {
            System.out.println("Checking " + products.size() + " Auctionss " + products.get(0).getAsJsonObject().get("uuid").getAsString());
        }
        for (JsonElement entry : products) {
            // Limit number of messages added because it will crash game if it gets overloaded
            float max = (float) (SkyblockFeatures.config.autoAuctionFlipMaxAuc);
            if (messageSent > max) continue;

            if (entry.isJsonObject()) {
                JsonObject itemData = entry.getAsJsonObject();
                // NBT related
                String item_bytes = itemData.get("item_bytes").getAsString();

                // NBT related
                Base64InputStream is = new Base64InputStream(new ByteArrayInputStream(item_bytes.getBytes(StandardCharsets.UTF_8)));
                NBTTagCompound nbt = null;
                try {
                    nbt = CompressedStreamTools.readCompressed(is);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                String id = AuctionUtil.getInternalNameFromNBT(nbt.getTagList("i", 10).getCompoundTagAt(0).getCompoundTag("tag"));
                NBTTagCompound extraAttributes = nbt.getTagList("i", 10).getCompoundTagAt(0).getCompoundTag("tag").getCompoundTag("ExtraAttributes");
                String name = nbt.getTagList("i", 10).getCompoundTagAt(0).getCompoundTag("tag").getCompoundTag("display").getString("Name");

                // Load lowest and average BIN prices
                Double lowestBinPrice = PricingData.lowestBINs.get(id);
                Double avgBinPrice = PricingData.averageLowestBINs.get(id);
                if (lowestBinPrice == null || avgBinPrice == null) continue;

                String auctionId = itemData.get("uuid").toString().replaceAll("\"", "");
                long basePrice = lowestBinPrice.longValue();

                // Bin Flip
                if (itemData.get("bin").getAsBoolean()) {
                    if (!SkyblockFeatures.config.aucFlipperBins) continue;
                    // Get Current Item Price
                    long goingForPrice = itemData.get("starting_bid").getAsLong();

                    // Item Values
                    if (lowestBinPrice > 1.10 * avgBinPrice) {
                        basePrice = avgBinPrice.longValue();
                    }

                    long valueOfTheItem = basePrice;
                    if (SkyblockFeatures.config.aucFlipperItemUpgrades) {
                        valueOfTheItem = ItemUtils.getEstimatedItemValue(extraAttributes);
                    }
                    int flipPercentage = (int) Math.floor(((valueOfTheItem / goingForPrice) - 1) * 100);
                    JsonObject auctionData = PricingData.getItemAuctionInfo(id);
                    Long enchantValue = ItemUtils.getEnchantsWorth(extraAttributes);
                    Long starValue = ItemUtils.getStarCost(extraAttributes);
                    Long profit = valueOfTheItem - goingForPrice;
                    int volume = 20;

                    String[] lore = itemData.get("item_lore").getAsString().split("Â");
                    String stringLore = String.join("", lore);

                    if (auctionData != null) volume = auctionData.get("sales").getAsInt();

                    // if the lowest bin is over 1.10x the average then its most likely being manipulated so use the average instead
                    if (!SkyblockFeatures.config.aucFlipperItemUpgrades) {
                        if (lowestBinPrice > 1.10 * avgBinPrice) {
                            valueOfTheItem = avgBinPrice.intValue();
                        }
                    }

                    auctionsFilteredThrough++;
                    // Filters
                    if (stringFilter(name, id, auctionId, stringLore)) {
                        continue;
                    }
                    if (priceFilter(volume, flipPercentage, name, auctionId, valueOfTheItem, profit, goingForPrice, -1)) {
                        continue;
                    }
                    auctionsPassedFilteredThrough++;

                    if (auctionData != null) {
                        Auction auction = new Auction(auctionId, itemData, profit);
                        String currentProfit = Utils.shortenNumber(profit.longValue());
                        String currentPrice = Utils.shortenNumber(goingForPrice);
                        String itemValue = Utils.shortenNumber(valueOfTheItem);
                        String ePrice = Utils.shortenNumber(enchantValue);
                        String sPrice = Utils.shortenNumber(starValue);
                        boolean dupe = auctionFlips.stream().anyMatch(auc -> Objects.equals(auc.auctionId, auctionId));
                        if (dupe || valueOfTheItem < goingForPrice) continue;

                        auctionFlips.add(auction);

                        sendAuctionNotification(name, currentPrice, itemValue, currentProfit, flipPercentage, auctionData, ePrice, sPrice, -1L, auctionId, "BIN");
                    }
                } else {
                    if (!SkyblockFeatures.config.aucFlipperAucs) continue;
                    // Auction Flip
                    double currentTime = System.currentTimeMillis();
                    long msTillEnd = (long) Math.abs(itemData.get("end").getAsDouble() - currentTime);
                    long goingForPrice = itemData.get("highest_bid_amount").getAsLong();
                    if (goingForPrice == 0) goingForPrice = itemData.get("starting_bid").getAsLong();

                    // if the lowest bin is over 1.10x the average then its most likely being manipulated so use the average instead
                    if (lowestBinPrice > 1.10 * avgBinPrice) {
                        basePrice = avgBinPrice.longValue();
                    }

                    long valueOfTheItem = basePrice;
                    if (SkyblockFeatures.config.aucFlipperItemUpgrades) {
                        valueOfTheItem = ItemUtils.getEstimatedItemValue(extraAttributes);
                    }

                    JsonObject auctionData = PricingData.getItemAuctionInfo(id);
                    Long enchantValue = ItemUtils.getEnchantsWorth(extraAttributes);
                    Long starValue = ItemUtils.getStarCost(extraAttributes);
                    int volume = 20;

                    if (auctionData != null) {
                        volume = auctionData.get("sales").getAsInt();
                    }

                    long profit = valueOfTheItem - goingForPrice;
                    int flipPercentage = (int) Math.floor((((double) valueOfTheItem / goingForPrice) - 1) * 100);

                    String[] lore = itemData.get("item_lore").getAsString().split("Â");
                    String stringLore = String.join("", lore);

                    // Account for taxes
                    if (goingForPrice > 100000) profit = ((Double) ((double) profit * 0.95)).longValue();
                    else profit = ((Double) ((double) profit * 0.9)).longValue();

                    auctionsFilteredThrough++;
                    // Filters
                    if (stringFilter(name, id, auctionId, stringLore)) {
                        continue;
                    }
                    if (priceFilter(volume, flipPercentage, name, auctionId, valueOfTheItem, profit, goingForPrice, msTillEnd)) {
                        continue;
                    }
                    auctionsPassedFilteredThrough++;

                    if (auctionData != null) {
                        Auction auction = new Auction(auctionId, itemData, profit);
                        String currentProfit = Utils.shortenNumber(profit);
                        String currentPrice = Utils.shortenNumber(goingForPrice);
                        String itemValue = Utils.shortenNumber(valueOfTheItem);
                        String ePrice = Utils.shortenNumber(enchantValue);
                        String sPrice = Utils.shortenNumber(starValue);

                        // Filter out any auctions with duplicate ids
                        boolean dupe = auctionFlips.stream().anyMatch(auc -> Objects.equals(auc.auctionId, auctionId));

                        if (dupe || goingForPrice > valueOfTheItem) continue;

                        auctionFlips.add(auction);

                        sendAuctionNotification(name, currentPrice, itemValue, currentProfit, flipPercentage, auctionData, ePrice, sPrice, msTillEnd, auctionId, "AUC");
                    }
                }
            }
        }
        // Sort the auctionFlips list in descending order of profit
        auctionFlips.sort((a, b) -> Double.compare(b.profit, a.profit));
    }

    public void sendAuctionNotification(String itemName, String currentPrice, String itemValue, String currentProfit, int flipPercentage, JsonObject auctionData, String ePrice, String sPrice, Long msTillEnd, String auctionId, String flipType) {
        // [SBF] AUC Spiritual JuJu Shortbow 300k -> 1.3m (+1m 50%)
        String text = "\n" + ChatFormatting.AQUA + "[SBF] " +
                ChatFormatting.GRAY + "AUC " + itemName + " " +
                ChatFormatting.GREEN + currentPrice + " -> " + itemValue +
                " (+" + currentProfit + " " + ChatFormatting.DARK_RED + flipPercentage + "%" + ChatFormatting.GREEN + ") ";

        // Add volume and sales per day
        text += ChatFormatting.GRAY + "Vol: " + ChatFormatting.AQUA + (auctionData.get("sales").getAsInt()) + " sales/day";

        // Additional text if needed
        if (SkyblockFeatures.config.aucFlipperItemUpgrades) {
            if (!ePrice.equals("0"))
                text += ChatFormatting.GRAY + " Ench: " + ChatFormatting.AQUA + ePrice;
            if (!sPrice.equals("0"))
                text += ChatFormatting.GRAY + " Stars: " + ChatFormatting.AQUA + sPrice;
        }

        // Add time until ending
        if (msTillEnd > 0) {
            text += ChatFormatting.YELLOW + " " + Utils.secondsToTime((int) (msTillEnd / 1000));
        }

        IChatComponent message = new ChatComponentText(text);
        message.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/viewauction " + auctionId));
        message.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(ChatFormatting.GREEN + "/viewauction " + auctionId)));

        // Notify User
        if (SkyblockFeatures.config.aucFlipperSound) Utils.playSound("note.pling", 0.5);
        Utils.GetMC().thePlayer.addChatComponentMessage(message);

        // Track how many notifications have been sent so it doesnt send too many
        messageSent++;
    }

    public boolean stringFilter(String itemName, String id, String aucId, String stringLore) {
        boolean returnValue = false;
        if (SkyblockFeatures.config.autoAuctionFilterOutPets && itemName.toLowerCase().contains("[lvl")) {
            if (SkyblockFeatures.config.debugAuctionFlipper)
                System.out.println(itemName + " Auction Removed Because Pet Filter" + " " + aucId);
            returnValue = true;
        }
        if (SkyblockFeatures.config.autoAuctionFilterOutSkins && (id.contains("SKIN") || itemName.toLowerCase().contains("skin"))) {
            if (SkyblockFeatures.config.debugAuctionFlipper)
                System.out.println(itemName + " Auction Removed Because Skin Filter" + " " + aucId);
            returnValue = true;
        }
        if (SkyblockFeatures.config.autoAuctionFilterOutRunes && itemName.contains("Rune")) {
            if (SkyblockFeatures.config.debugAuctionFlipper)
                System.out.println(itemName + " Auction Removed Because Rune Filter" + " " + aucId);
            returnValue = true;
        }
        if (SkyblockFeatures.config.autoAuctionFilterOutDyes && itemName.contains("Dye")) {
            if (SkyblockFeatures.config.debugAuctionFlipper)
                System.out.println(itemName + " Auction Removed Because Dye Filter" + " " + aucId);
            returnValue = true;
        }
        if (SkyblockFeatures.config.autoAuctionFilterOutFurniture && stringLore.contains("furniture")) {
            if (SkyblockFeatures.config.debugAuctionFlipper)
                System.out.println(itemName + " Auction Removed Because Furniture Filter" + " " + aucId);
            returnValue = true;
        }

        if (SkyblockFeatures.config.autoAuctionBlacklist.length() > 1) {
            try {
                for (String blacklistedName : SkyblockFeatures.config.autoAuctionBlacklist.split(";")) {
                    if (Utils.cleanColor(itemName).toLowerCase().contains(blacklistedName)) {
                        if (SkyblockFeatures.config.debugAuctionFlipper)
                            System.out.println(itemName + " Auction Removed because blacklist");
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

    public boolean priceFilter(int volume, double percentage, String itemName, String aucId, Long valueOfTheItem, Long profit, Long binPrice, long msTillEnd) {
        boolean returnValue = false;
        float margin = (float) SkyblockFeatures.config.autoAuctionFlipMargin;
        float minVolume = (float) (SkyblockFeatures.config.autoAuctionFlipMinVolume);
        float minPercent = (float) (SkyblockFeatures.config.autoAuctionFlipMinPercent);

        if (volume < minVolume) {
            if (SkyblockFeatures.config.debugAuctionFlipper)
                System.out.println(itemName + " Auction Removed Because MinVol Filter " + "Vol: " + volume + " " + aucId);
            returnValue = true;
        } else if (percentage < minPercent) {
            if (SkyblockFeatures.config.debugAuctionFlipper)
                System.out.println(itemName + " Auction Removed Because MinPerc Filter Perc:" + percentage + " " + Utils.nf.format(binPrice) + " " + Utils.nf.format(valueOfTheItem) + " " + aucId);
            returnValue = true;
        } else if (profit < margin) {
            if (SkyblockFeatures.config.debugAuctionFlipper)
                System.out.println(itemName + " Auction Removed Because less than profit margin :" + profit + " Item Value:" + valueOfTheItem + "   Price of item:" + binPrice + " " + aucId);
            returnValue = true;
        } else if (SkyblockFeatures.config.autoAuctionFlipSetPurse && SkyblockInfo.getCoins() < binPrice) {
            if (SkyblockFeatures.config.debugAuctionFlipper)
                System.out.println(itemName + " Auction Removed Because Purse Filter");
            returnValue = true;
        }
        // Filter out auctions ending in more than 5 minutes
        else if (msTillEnd > 60 * 5 * 1000) {
            if (SkyblockFeatures.config.debugAuctionFlipper)
                System.out.println(itemName + " Auction removed because ends in more than 5m ");
            returnValue = true;
        }
        return returnValue;
    }

    public void resetFlipper() {
        auctionsFilteredThrough = 0;
        auctionsPassedFilteredThrough = 0;
        bestAuction = null;
        sent = false;
        clicking = false;
        clicking2 = false;
        startMs = System.currentTimeMillis();
        messageSent = 0;
    }

    static {
        new AutoAuctionGui();
    }

    static String display = "Auction API update in 60s";

    public static class AutoAuctionGui extends UIElement {
        private final ArrayList<String> lines = new ArrayList<>();

        public AutoAuctionGui() {
            super("Auto Auction Flip Counter", new Point(0.2f, 0.0f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        private void updateLines() {
            lines.clear();
            double seconds = Math.floor((System.currentTimeMillis() - startMs) / 1000d);

            lines.add(ChatFormatting.GOLD + "Time Elapsed: " + Utils.secondsToTime((int) seconds));
            lines.add(ChatFormatting.YELLOW + "Flipper Active");
        }

        @Override
        public void drawElement() {
            // Update the lines dynamically
            updateLines();

            // Calculate the center Y coordinate
            GuiUtils.drawCenteredText(this,lines,GuiUtils.TextStyle.BLACK_OUTLINE);
        }

        @Override
        public void drawElementExample() {
            // Update the lines dynamically
            updateLines();

            GuiUtils.drawCenteredText(this,lines, GuiUtils.TextStyle.BLACK_OUTLINE);
        }

        @Override
        public boolean getToggled() {
            return SkyblockFeatures.config.aucFlipperEnabled;
        }

        @Override
        public boolean getRequirement() {
            return Utils.inSkyblock;
        }

        @Override
        public int getHeight() {
            return (Utils.GetMC().fontRendererObj.FONT_HEIGHT + 2) * 3;
        }

        @Override
        public int getWidth() {
            return Utils.GetMC().fontRendererObj.getStringWidth(display);
        }
    }
}