package mrfast.sbf.gui.ProfileViewer.Pages;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;
import gg.essential.elementa.UIComponent;
import gg.essential.elementa.components.UIBlock;
import gg.essential.elementa.components.UIRoundedRectangle;
import gg.essential.elementa.components.UIText;
import gg.essential.elementa.constraints.CenterConstraint;
import gg.essential.elementa.constraints.PixelConstraint;
import gg.essential.elementa.constraints.RelativeConstraint;
import gg.essential.elementa.constraints.SiblingConstraint;
import mrfast.sbf.gui.ProfileViewer.ProfileViewerGui;
import mrfast.sbf.gui.ProfileViewer.ProfileViewerUtils;
import mrfast.sbf.gui.components.ItemStackComponent;
import mrfast.sbf.utils.ItemUtils;
import mrfast.sbf.utils.NetworkUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.item.ItemStack;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class CollectionsPage extends ProfileViewerGui.ProfileViewerPage {
    public CollectionsPage(UIComponent main) {
        super(main);
    }

    static UIComponent statsAreaContainerNew = new UIBlock(Color.red)
            .setWidth(new RelativeConstraint(0.75f))
            .setHeight(new RelativeConstraint(0.75f));

    @Override
    public void loadPage() {
        this.mainComponent.clearChildren();
        UIComponent loadingText = new UIText(ChatFormatting.RED + "Loading")
                .setTextScale(new PixelConstraint(2f))
                .setChildOf(this.mainComponent)
                .setX(new CenterConstraint())
                .setY(new CenterConstraint());

        new Thread(() -> {
            double loadingIndex = 0;
            while (statsAreaContainerNew.getChildren().size() < 7) {
                if (!ProfileViewerGui.selectedCategory.equals("Collections")) break;

                if (statsAreaContainerNew.getChildren().size() == 6) {
                    loadingText.parent.removeChild(loadingText);
                    this.mainComponent.clearChildren();
                    statsAreaContainerNew.getChildren().forEach((e) -> {
                        this.mainComponent.addChild(e);
                    });
                    break;
                } else {
                    try {
                        ((UIText) loadingText).setText(ProfileViewerUtils.loadingStages[(int) Math.floor(loadingIndex)]);
                        loadingIndex += 0.5;
                        loadingIndex %= 3;
                        Thread.sleep(100);
                    } catch (Exception ignored) {
                    }
                }
            }
        }).start();
    }

    private static final HashMap<String, JsonObject> categoryDataCache = new HashMap<>();

    public static void loadCollectionsCategories() {
        if (Utils.isDeveloper()) System.out.println("Loading collections");

        if (ProfileViewerGui.collectionsData == null) {
            // Fetch the collections data only if it's not already cached
            ProfileViewerGui.collectionsData = NetworkUtils.getJSONResponse("https://api.hypixel.net/v2/resources/skyblock/collections#CollectionsForPV", new String[]{}, true, false).getAsJsonObject();
        } else {
            return;
        }

        new Thread(() -> {
            String[] categories = {"Farming", "Mining", "Combat", "Foraging", "Fishing", "Rift"};
            int totalHeight = 0;

            for (String category : categories) {
                if (!ProfileViewerGui.collectionsData.get("success").getAsBoolean()) {
                    if (Utils.isDeveloper()) System.out.println("Error: ");
                    return;
                }

                JsonObject collections = ProfileViewerGui.collectionsData.get("collections").getAsJsonObject();

                JsonObject categoryObject = categoryDataCache.computeIfAbsent(category, key -> collections.get(key.toUpperCase()).getAsJsonObject());

                UIComponent categoryComponent = new UIBlock(new Color(0, 0, 0, 0))
                        .setChildOf(statsAreaContainerNew)
                        .setY(new SiblingConstraint(10f))
                        .setHeight(new PixelConstraint(20f))
                        .setWidth(new RelativeConstraint(1f));

                new UIText(category, true)
                        .setChildOf(categoryComponent)
                        .setTextScale(new PixelConstraint((float) (1.3f)))
                        .setX(new PixelConstraint(0f))
                        .setY(new PixelConstraint(0f));

                UIComponent container = new UIBlock(new Color(0, 0, 0, 0))
                        .setChildOf(categoryComponent)
                        .setY(new SiblingConstraint(3f))
                        .setHeight(new RelativeConstraint(1f))
                        .setWidth(new RelativeConstraint(1f));

                int categoryHeight = loadCollectionsCategory(categoryObject, container);
                categoryComponent.setHeight(new PixelConstraint(categoryHeight + 23));
                totalHeight += categoryHeight + 23; // Add 23 to account for the height of the category header
            }

            statsAreaContainerNew.setHeight(new PixelConstraint(totalHeight)); // Set the parent component's height

            if (Utils.isDeveloper()) System.out.println("LOADED COLLECTIONS");
        }).start();
    }

    public static int loadCollectionsCategory(JsonObject category, UIComponent component) {
        JsonObject items = category.get("items").getAsJsonObject();

        int maxItemsPerRow = 11;
        int numRows = (int) Math.ceil((double) items.entrySet().size() / maxItemsPerRow);
        int itemSpacing = 5;
        int totalHeight = numRows * (20 + itemSpacing) - itemSpacing; // Calculate total height for all rows

        int xStart = 0;
        int yStart = 0; // Initial y-position
        int rowWidth = 0; // Keep track of the current row's width
        int index = 0;
        int row = 0;

        for (Map.Entry<String, JsonElement> item : items.entrySet()) {
            if (index >= maxItemsPerRow) {
                // Move to the next row
                rowWidth = 0; // Reset the current row's width
                index = 0;
                row++;
            }

            String itemId = item.getKey().replaceAll(":", "-");
            if (itemId.equals("MUSHROOM_COLLECTION")) itemId = "BROWN_MUSHROOM";

            ItemStack stack = ItemUtils.getSkyblockItem(itemId);

            List<String> lore = new ArrayList<>();
            List<ProfileViewerGui.CoopCollector> collectors = new ArrayList<>();
            JsonObject members = ProfileViewerGui.ProfileResponse.get("members").getAsJsonObject();
            long total = 0L;

            for (Map.Entry<String, JsonElement> member : members.entrySet()) {
                if (!ProfileViewerGui.coopNames.containsKey(member.getKey())) {
                    String name = NetworkUtils.getName(member.getKey());
                    ChatFormatting color = Objects.equals(name, Utils.GetMC().thePlayer.getName()) ? ChatFormatting.GOLD : ChatFormatting.GREEN;
                    String formattedName = color + NetworkUtils.getName(member.getKey());

                    ProfileViewerGui.coopNames.put(member.getKey(), formattedName);
                }


                long value = 0L;
                try {
                    value = member.getValue().getAsJsonObject().get("collection").getAsJsonObject().get(item.getKey()).getAsLong();
                } catch (Exception e) {
                    // TODO: handle exception
                }
                total += value;
                collectors.add(new ProfileViewerGui.CoopCollector(ProfileViewerGui.coopNames.get(member.getKey()), value));
            }
            // Create a list of CoopCollector objects
            // Sort the list based on the 'total' field in descending order
            collectors = collectors.stream()
                    .sorted(Comparator.comparingLong(ProfileViewerGui.CoopCollector::getTotal).reversed())
                    .collect(Collectors.toList());

            CollectionTier rank = getCollectionTier(item.getValue().getAsJsonObject(), total);
            String itemName = Utils.cleanColor(stack.getDisplayName());

            lore.add("§7Total Collected: §e" + Utils.nf.format(total));

            if (!rank.maxed) {
                lore.add("");
                lore.add("§7Progress to " + itemName + " " + (rank.tier + 1) + ": §e" + (Math.floor((double) total / (rank.untilNext + total) * 1000) / 10) + "§6%");
                lore.add(stringProgressBar(total, (int) (rank.untilNext + total)));
            }

            lore.add("");
            lore.add("§7Contributions:");

            for (ProfileViewerGui.CoopCollector collector : collectors) {
                lore.add(collector.username + "§7: §e" + Utils.nf.format(collector.getTotal()) + Utils.percentOf(collector.getTotal(), total));
            }

            // Calculate the position for the current item
            int xPos = xStart + rowWidth;
            int yPos = yStart + row * (20 + itemSpacing);
            Color color = new Color(100, 100, 100, 200);
            if (rank.maxed) color = new Color(218, 165, 32, 200);

            int itemWidth = 20;
            UIComponent backgroundSlot = new UIRoundedRectangle(3f)
                    .setChildOf(component)
                    .setHeight(new PixelConstraint(20f))
                    .setWidth(new PixelConstraint(itemWidth))
                    .setX(new PixelConstraint(xPos))
                    .setY(new PixelConstraint(yPos))
                    .setColor(color);

            ItemUtils.updateLore(stack, lore);

            stack.setStackDisplayName("§e" + itemName + " " + rank.tier);

            UIComponent itemStackComponent = new ItemStackComponent(stack)
                    .setHeight(new PixelConstraint(20f))
                    .setWidth(new PixelConstraint(itemWidth))
                    .setX(new CenterConstraint())
                    .setY(new CenterConstraint());

            backgroundSlot.addChild(itemStackComponent);

            // Update the current row's width and index for the next item
            rowWidth += itemWidth + itemSpacing;
            index++;
        }

        return totalHeight;
    }

    public static CollectionTier getCollectionTier(JsonObject itemObj, long total) {
        JsonArray tiers = itemObj.get("tiers").getAsJsonArray();
        int maxTiers = itemObj.get("maxTiers").getAsInt();
        boolean maxed = false;
        int tier = -1;
        int untilNext = -1;
        double progress = 0.0;

        for (int i = 0; i < tiers.size(); i++) {
            JsonObject tierObj = tiers.get(i).getAsJsonObject();
            int amountRequired = tierObj.get("amountRequired").getAsInt();

            if (total >= amountRequired) {
                if (i == maxTiers - 1) {
                    // Maxed out the last tier
                    maxed = true;
                    tier = i + 1;
                    untilNext = -1;
                    progress = 1.0;
                } else {
                    // Reached a tier, but not maxed
                    maxed = false;
                    tier = i + 1;
                    untilNext = (int) (tiers.get(i + 1).getAsJsonObject().get("amountRequired").getAsInt() - total);
                    progress = (double) (total - amountRequired) / (double) (untilNext);
                }
            } else {
                // Not yet reached this tier
                tier = i;
                untilNext = (int) (amountRequired - total);
                progress = (double) total / (double) (amountRequired);
                break;
            }
        }

        return new CollectionTier(maxed, tier, untilNext, progress);
    }

    public static class CollectionTier {
        boolean maxed;
        int tier;
        int untilNext;
        Double progress;

        CollectionTier(boolean maxed, int tier, int untilNext, Double progress) {
            this.maxed = maxed;
            this.tier = tier;
            this.untilNext = untilNext;
            this.progress = progress;
        }
    }

    public static String stringProgressBar(long total2, long total) {
        double percent = (double) total2 / total;
        String progessed = "§2§l§m §2§l§m ";
        String unprogessed = "§f§l§m §f§l§m ";
        int times = (int) (percent * 20);
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            if (i < times) out.append(progessed);
            else {
                out.append(unprogessed);
            }
        }
        return out + "§r §e" + Utils.nf.format(total2) + "§6/§e" + Utils.shortenNumber(total);
    }
}
