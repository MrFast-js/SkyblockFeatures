package mrfast.sbf.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import mrfast.sbf.core.PricingData;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.Constants;

public class ItemUtils {
    private static final Pattern RARITY_PATTERN = Pattern.compile("(§[0-9a-f]§l§ka§r )?([§0-9a-fk-or]+)(?<rarity>[A-Z]+)");
    public static final int NBT_STRING = 8;
    public static final int NBT_LIST = 9;
    public static final int NBT_COMPOUND = 10;
    public static ItemStack menuGlass = new ItemStack(Blocks.stained_glass_pane, 1, 15).setStackDisplayName(" ");


    /**
     * Returns the display name of a given item
     *
     * @param item the Item to get the display name of
     * @return the display name of the item
     * @author Mojang
     */
    public static String getDisplayName(ItemStack item) {
        String s = item.getItem().getItemStackDisplayName(item);

        if (item.getTagCompound() != null && item.getTagCompound().hasKey("display", 10)) {
            NBTTagCompound nbttagcompound = item.getTagCompound().getCompoundTag("display");

            if (nbttagcompound.hasKey("Name", 8)) {
                s = nbttagcompound.getString("Name");
            }
        }

        return s;
    }

    /**
     * Returns the Skyblock Item ID of a given Skyblock item
     *
     * @param item the Skyblock item to check
     * @return the Skyblock Item ID of this item or {@code null} if this isn't a valid Skyblock item
     * @author BiscuitDevelopment
     */
    public static String getSkyBlockItemID(ItemStack item) {
        if (item == null) {
            return null;
        }

        NBTTagCompound extraAttributes = getExtraAttributes(item);
        if (extraAttributes == null) {
            return null;
        }

        if (!extraAttributes.hasKey("id", ItemUtils.NBT_STRING)) {
            return null;
        }

        return extraAttributes.getString("id");
    }

    public static String getItemUUID(ItemStack item) {
        if (item == null) {
            return null;
        }

        NBTTagCompound extraAttributes = getExtraAttributes(item);
        if (extraAttributes == null) {
            return null;
        }

        if (!extraAttributes.hasKey("uuid", ItemUtils.NBT_STRING)) {
            return null;
        }

        return extraAttributes.getString("uuid");
    }

    /**
     * Returns the {@code ExtraAttributes} compound tag from the item's NBT data.
     *
     * @param item the item to get the tag from
     * @return the item's {@code ExtraAttributes} compound tag or {@code null} if the item doesn't have one
     * @author BiscuitDevelopment
     */
    public static NBTTagCompound getExtraAttributes(ItemStack item) {
        if (item == null || !item.hasTagCompound()) {
            return null;
        }

        return item.getSubCompound("ExtraAttributes", false);
    }

    /**
     * Returns the Skyblock Item ID of a given Skyblock Extra Attributes NBT Compound
     *
     * @param extraAttributes the NBT to check
     * @return the Skyblock Item ID of this item or {@code null} if this isn't a valid Skyblock NBT
     * @author BiscuitDevelopment
     */
    public static String getSkyBlockItemID(NBTTagCompound extraAttributes) {
        if (extraAttributes != null) {
            String itemId = extraAttributes.getString("id");

            if (!itemId.isEmpty()) {
                return itemId;
            }
        }

        return null;
    }

    private static final HashMap<String, ItemStack> itemDataCache = new HashMap<>();

    public static ItemStack getSkyblockItem(String id) {
        if (itemDataCache.containsKey(id)) {
            return itemDataCache.get(id).copy();
        }
        JsonObject itemObj = NetworkUtils.getJSONResponse("https://raw.githubusercontent.com/NotEnoughUpdates/NotEnoughUpdates-REPO/master/items/" + id + ".json");
        ItemStack stack = parseJsonToItemStack(itemObj.toString());
        itemDataCache.put(id, stack.copy());
        return stack;
    }

    public static class CustomItemData {
        public String internalname;
        public String itemid;
        public String displayname;
        public String nbttag;
        public List<String> lore;
    }

    public static ItemStack parseJsonToItemStack(String json) {
        Gson gson = new Gson();
        CustomItemData itemData = gson.fromJson(json, CustomItemData.class);
        int meta = 0;
        if (itemData.internalname.contains("-")) {
            meta = Integer.parseInt(itemData.internalname.split("-")[1]);
        }

        Item item = Item.getByNameOrId(itemData.itemid);

        if (item == null) {
            return null; // Return null if the item is not found by the itemid
        }

        ItemStack itemStack = new ItemStack(item, 1, meta);
        NBTTagCompound nbtTagCompound = new NBTTagCompound();

        NBTTagCompound extraAttributes = new NBTTagCompound();
        NBTTagCompound id = new NBTTagCompound();
        id.setString("id", itemData.internalname);
        extraAttributes.setTag("ExtraAttributes", id);

        nbtTagCompound.merge(extraAttributes);

        itemStack.setTagCompound(nbtTagCompound);

        itemStack.setStackDisplayName(itemData.displayname);
        return itemStack;
    }


    public static void updateLore(ItemStack itemStack, List<String> lore2) {
        NBTTagCompound itemNbt = itemStack.getTagCompound();
        if (itemNbt == null) {
            itemNbt = new NBTTagCompound();
        }

        NBTTagCompound displayNbt = itemNbt.getCompoundTag("display");
        if (!displayNbt.hasKey("Lore")) {
            displayNbt.setTag("Lore", new NBTTagList());
        }

        NBTTagList loreList = displayNbt.getTagList("Lore", 8);
        for (String lore : lore2) {
            loreList.appendTag(new net.minecraft.nbt.NBTTagString(lore));
        }

        displayNbt.setTag("Lore", loreList);
        itemNbt.setTag("display", displayNbt);
        itemStack.setTagCompound(itemNbt);

    }


    /**
     * Returns a string list containing the nbt lore of an ItemStack, or
     * an empty list if this item doesn't have a lore. The returned lore
     * list is unmodifiable since it has been converted from an NBTTagList.
     *
     * @param itemStack the ItemStack to get the lore from
     * @return the lore of an ItemStack as a string list
     * @author BiscuitDevelopment
     */
    public static List<String> getItemLore(ItemStack itemStack) {
        try {
            if (itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("display", ItemUtils.NBT_COMPOUND)) {
                NBTTagCompound display = itemStack.getTagCompound().getCompoundTag("display");

                if (display.hasKey("Lore", ItemUtils.NBT_LIST)) {
                    NBTTagList lore = display.getTagList("Lore", ItemUtils.NBT_STRING);

                    List<String> loreAsList = new ArrayList<>();
                    for (int lineNumber = 0; lineNumber < lore.tagCount(); lineNumber++) {
                        loreAsList.add(lore.getStringTagAt(lineNumber));
                    }

                    return Collections.unmodifiableList(loreAsList);
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        }

        return Collections.emptyList();
    }

    /**
     * Returns the rarity of a given Skyblock item
     * Modified
     *
     * @param item the Skyblock item to check
     * @return the rarity of the item if a valid rarity is found, {@code null} if no rarity is found, {@code null} if item is {@code null}
     * @author BiscuitDevelopment
     */
    public static ItemRarity getRarity(ItemStack item) {
        if (item == null || !item.hasTagCompound()) {
            return ItemRarity.COMMON;
        }

        NBTTagCompound display = item.getSubCompound("display", false);

        if (display == null || !display.hasKey("Lore")) {
            return ItemRarity.COMMON;
        }
        NBTTagCompound extraAttributes = ItemUtils.getExtraAttributes(item);

        if (extraAttributes != null && extraAttributes.hasKey("petInfo") ) {
            String petInfo = extraAttributes.getString("petInfo");
            String rarity = petInfo.split("tier\\\":\\\"")[1].split("\\\"")[0];
            for (ItemRarity itemRarity : ItemRarity.values()) {
                if (rarity.startsWith(itemRarity.getName())) {
                    return itemRarity;
                }
            }
        }

        NBTTagList lore = display.getTagList("Lore", Constants.NBT.TAG_STRING);

        // Determine the item's rarity
        for (int i = 0; i < lore.tagCount(); i++) {
            String currentLine = lore.getStringTagAt(i);

            Matcher rarityMatcher = RARITY_PATTERN.matcher(currentLine);
            if (rarityMatcher.find()) {
                String rarity = rarityMatcher.group("rarity");

                for (ItemRarity itemRarity : ItemRarity.values()) {
                    if (rarity.startsWith(itemRarity.getName())) {
                        return itemRarity;
                    }
                }
            }
        }

        // If the item doesn't have a valid rarity, return null
        return ItemRarity.COMMON;
    }

    public static HashMap<String, JsonObject> skyhelperItemMap = new HashMap<>();

    public static long getEstimatedItemValue(ItemStack stack) {
        if (stack == null) return 0L;
        NBTTagCompound ExtraAttributes = getExtraAttributes(stack);
        return getEstimatedItemValue(ExtraAttributes);
    }

    // Used for Auction flipper
    public static Long getEstimatedItemValue(NBTTagCompound ExtraAttributes) {
        if (skyhelperItemMap.isEmpty()) {
            new Thread(() -> {
                JsonArray items = NetworkUtils.getArrayResponse("https://raw.githubusercontent.com/Altpapier/SkyHelper-Networth/abb278d6be1e13b3204ccb05f47c5e8aaf614733/constants/items.json");
                for (int i = 0; i < items.size(); i++) {
                    JsonObject a = items.get(i).getAsJsonObject();
                    skyhelperItemMap.put(a.get("id").getAsString(), a);
                }
            }).start();
        }

        String id = ExtraAttributes.getString("id");
        Long total = 0L;

        try {
            // Add lowest bin as a base price
            if (PricingData.lowestBINs.containsKey(id)) {
                total += PricingData.lowestBINs.get(id).longValue();
            } else if (PricingData.averageLowestBINs.containsKey(id)) {
                total += PricingData.averageLowestBINs.get(id).longValue();
            }
            // Add wither essence value
            total += getStarCost(ExtraAttributes);
            // Add enchants
            total += getEnchantsWorth(ExtraAttributes);
            // Hbp, recombs
            total += getUpgradeCost(ExtraAttributes);
            // gemstones
            total += getGemstoneWorth(ExtraAttributes);
            // drill parts
            total += getDrillParts(ExtraAttributes);
            // scrolls for hyperion
            total += getScrollsAndMisc(ExtraAttributes);
        } catch (Exception e) {
            // TODO: handle exception
        }

        return total;
    }

    public static Long getUpgradeCost(NBTTagCompound ExtraAttributes) {
        long total = 0;
        // Hot potato books
        if (ExtraAttributes.hasKey("hot_potato_count")) {
            int hpb = ExtraAttributes.getInteger("hot_potato_count");
            if (hpb > 10) {
                if (PricingData.bazaarPrices.get("FUMING_POTATO_BOOK") != null)
                    total += (long) (PricingData.bazaarPrices.get("FUMING_POTATO_BOOK") * 0.6);
            }
            if (PricingData.bazaarPrices.get("HOT_POTATO_BOOK") != null) {
                total += (long) (PricingData.bazaarPrices.get("HOT_POTATO_BOOK") * Math.min(hpb, 10));
            }
        }

        if (ExtraAttributes.hasKey("rarity_upgrades") && !ExtraAttributes.hasKey("item_tier")) {
            if (PricingData.bazaarPrices.get("RECOMBOBULATOR_3000") != null) {
                total += PricingData.bazaarPrices.get("RECOMBOBULATOR_3000");
            }
        }
        return total;
    }

    public static Long getScrollsAndMisc(NBTTagCompound ExtraAttributes) {
        long total = 0;
        String itemId = getSkyBlockItemID(ExtraAttributes);
        if (ExtraAttributes.hasKey("ability_scroll")) {
            NBTTagList scrolls = ExtraAttributes.getTagList("ability_scroll", Constants.NBT.TAG_STRING);
            for (int i = 0; i < scrolls.tagCount(); i++) {
                String abilityScroll = scrolls.getStringTagAt(i);
                total += PricingData.bazaarPrices.get(abilityScroll).longValue();
            }
        }
        if (ExtraAttributes.hasKey("art_of_war_count")) {
            total += PricingData.bazaarPrices.get("THE_ART_OF_WAR").longValue();
        }
        if (ExtraAttributes.hasKey("dye_item")) {
            total += PricingData.averageLowestBINs.get(ExtraAttributes.getString("dye_item")).longValue();
        }
        if (ExtraAttributes.hasKey("stats_book")) {
            total += PricingData.bazaarPrices.get("BOOK_OF_STATS").longValue();
        }
        if (ExtraAttributes.hasKey("polarvoid")) {
            total += PricingData.averageLowestBINs.get("POLARVOID_BOOK").longValue() * ExtraAttributes.getInteger("polarvoid");
        }
        if (ExtraAttributes.hasKey("runes")) {
            String runeType = Optional.ofNullable(ExtraAttributes.getCompoundTag("runes"))
                    .map(NBTTagCompound::getKeySet)
                    .flatMap(keys -> keys.stream().findFirst())
                    .orElse(null);
            int runeLvl = ExtraAttributes.getCompoundTag("runes").getInteger(runeType);
            String runeId = runeType + "_RUNE;" + runeLvl;

            if (PricingData.lowestBINs.containsKey(runeId)) {
                total += PricingData.lowestBINs.get(runeId).longValue();
            }
        }

//        if(ExtraAttributes.hasKey("attributes")) {
//            List<Attribute> valuedAttr = getAttributes(ExtraAttributes);
//            for (Attribute attribute : valuedAttr) {
//                total+=attribute.value;
//            }
//        }

        return total;
    }

    public static List<Attribute> getAttributes(NBTTagCompound ExtraAttributes) {
        NBTTagCompound attr = ExtraAttributes.getCompoundTag("attributes");
        String itemId = getSkyBlockItemID(ExtraAttributes);
        List<Attribute> valuedAttr = new ArrayList<>();
        for (String attributeName : attr.getKeySet()) {
            int attributeLvl = attr.getInteger(attributeName);
            Attribute attribute = new Attribute(attributeName.toUpperCase(), attributeLvl, itemId);
            Double lowestBin = PricingData.lowestBINs.get(itemId);
            long value = getValueOfAttr(attribute, itemId);
            if (value != -1) {
                attribute.value = (long) (value - lowestBin);
            }
            valuedAttr.add(attribute);
        }
        return valuedAttr;
    }

    public static class Attribute {
        public int lvl;
        public String id;
        public String itemId;
        public long value = 0L;
        public long pricePerTier = 0L;

        public Attribute(String id, int lvl, String itemId) {
            this.id = id;
            this.lvl = lvl;
            this.itemId = itemId;
        }
    }

    public static long getValueOfAttr(Attribute attribute, String itemId) {
        Double lowestPrice = PricingData.lowestBINs.get(itemId + "+ATTRIBUTE_" + attribute.id.toUpperCase() + ";" + attribute.lvl);
        if (lowestPrice != null) return lowestPrice.longValue();

        List<Attribute> matches = PricingData.attributeAuctions.stream().filter((attr) ->
                attr.id.equals(attribute.id) && attr.itemId.equals(attribute.itemId)
        ).collect(Collectors.toList());

        if (!matches.isEmpty()) {
            for (Attribute match : matches) {
                if (match.lvl == attribute.lvl) {
                    return match.value;
                }
            }
            return matches.get(0).value;
        }

        return -1;
    }


    public static Long getDrillParts(NBTTagCompound ExtraAttributes) {
        long total = 0;

        if (ExtraAttributes.hasKey("drill_part_upgrade_module")) {
            String upgrade = ExtraAttributes.getString("drill_part_upgrade_module").toUpperCase();
            if (PricingData.lowestBINs.containsKey(upgrade)) {
                total += PricingData.lowestBINs.get(upgrade).longValue();
            }
        }

        if (ExtraAttributes.hasKey("drill_part_engine")) {
            String upgrade = ExtraAttributes.getString("drill_part_engine").toUpperCase();
            if (PricingData.lowestBINs.containsKey(upgrade)) {
                total += PricingData.lowestBINs.get(upgrade).longValue();
            }
        }
        if (ExtraAttributes.hasKey("drill_part_fuel_tank")) {
            String upgrade = ExtraAttributes.getString("drill_part_fuel_tank").toUpperCase();
            if (PricingData.lowestBINs.containsKey(upgrade)) {
                total += PricingData.lowestBINs.get(upgrade).longValue();
            }
        }
        return total;
    }

    public static Long getStarCost(NBTTagCompound extraAttributes) {
        String id = extraAttributes.getString("id");
        int stars;
        int masterStars = 0;
        int totalEssence = 0;
        String essenceType = "WITHER";
        try {
            if (extraAttributes.hasKey("upgrade_level")) {
                stars = extraAttributes.getInteger("upgrade_level");
                if (stars > 5) {
                    masterStars = stars - 5;
                    stars = 5;
                }
            } else if (extraAttributes.hasKey("dungeon_item_level")) {
                stars = extraAttributes.getInteger("dungeon_item_level");
                if (stars > 5) {
                    masterStars = stars - 5;
                    stars = 5;
                }
            } else {
                return 0L;
            }
            JsonArray upgradeCosts = skyhelperItemMap.get(id).get("upgrade_costs").getAsJsonArray();
            for (int i = 0; i < stars; i++) {
                JsonElement b = upgradeCosts.get(i).getAsJsonArray().get(0);
                totalEssence += b.getAsJsonObject().get("amount").getAsInt();
                essenceType = b.getAsJsonObject().get("essence_type").getAsString();
            }
        } catch (Exception e) {
            // e.printStackTrace();
            // TODO: handle exception
        }
        Double pricePerEssence = PricingData.bazaarPrices.get("ESSENCE_" + essenceType);
        Double masterStarPrice = 0d;
        try {
            if (masterStars > 0) {
                masterStarPrice += PricingData.bazaarPrices.get("FIRST_MASTER_STAR");
                if (masterStars >= 2) masterStarPrice += PricingData.bazaarPrices.get("SECOND_MASTER_STAR");
                if (masterStars >= 3) masterStarPrice += PricingData.bazaarPrices.get("THIRD_MASTER_STAR");
                if (masterStars >= 4) masterStarPrice += PricingData.bazaarPrices.get("FOURTH_MASTER_STAR");
                if (masterStars == 5) masterStarPrice += PricingData.bazaarPrices.get("FIFTH_MASTER_STAR");
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return ((long) ((totalEssence * pricePerEssence) + masterStarPrice));
    }

    public static Long getGemstoneWorth(NBTTagCompound extraAttributes) {
        if (!extraAttributes.hasKey("gems")) return 0L;
        NBTTagCompound nbt = extraAttributes.getCompoundTag("gems");
        long total = 0;
        List<String> finalGemstones = new ArrayList<>();

        for (String enchant : nbt.getKeySet()) {
            if (enchant.contains("_gem")) continue;
            if (nbt.hasKey(enchant + "_gem")) {
                String gemstone = nbt.getString(enchant) + "_" + nbt.getString(enchant + "_gem") + "_GEM";
                finalGemstones.add(gemstone);
            } else {
                String type = enchant.split("_")[0];
                String tier = nbt.getString(enchant);
                finalGemstones.add(tier + "_" + type + "_GEM");
            }
        }

        for (String gemstone : finalGemstones) {
            try {
                total += PricingData.bazaarPrices.get(gemstone);
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
        return total;
    }

    public static Long getEnchantsWorth(NBTTagCompound extraAttributes) {
        if (!extraAttributes.hasKey("enchantments")) return 0L;
        NBTTagCompound nbt = extraAttributes.getCompoundTag("enchantments");
        long total = 0;
        for (String enchant : nbt.getKeySet()) {
            int enchLvl = nbt.getInteger(enchant);
            String id = "ENCHANTMENT_" + enchant.toUpperCase() + "_" + enchLvl;
            if (PricingData.bazaarPrices.get(id) != null) {
                if (id.contains("ENCHANTMENT_SCAVENGER") || id.contains("ENCHANTMENT_INFINITE_QUIVER")) continue;
                total += (long) (PricingData.bazaarPrices.get(id) * 0.85);
            }
            if (enchant.equals("efficiency") && enchLvl > 5) {
                total += PricingData.bazaarPrices.get("SIL_EX").longValue() * (enchLvl - 5);
            }
        }
        return total;
    }

    public static class Inventory {
        private final String data;

        public Inventory(String data) {
            this.data = data;
        }

        public String getData() {
            return this.data.replace("\\u003d", "=");
        }
    }
    public static List<ItemStack> decodeInventory(Inventory inventory, Boolean offset) {
        List<ItemStack> itemStack = new ArrayList<>();
        if (inventory != null) {
            byte[] decode = Base64.getDecoder().decode(inventory.getData());

            try {
                NBTTagCompound compound = CompressedStreamTools.readCompressed(new ByteArrayInputStream(decode));
                NBTTagList list = compound.getTagList("i", 10);

                for (int i = 0; i < list.tagCount(); ++i) {
                    itemStack.add(ItemStack.loadItemStackFromNBT(list.getCompoundTagAt(i)));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (offset) Collections.rotate(itemStack, -9);

        } else {
            ItemStack barrier = new ItemStack(Blocks.barrier);
            barrier.setStackDisplayName(EnumChatFormatting.RESET + "" + EnumChatFormatting.RED + "Item is not available!");

            for (int i = 0; i < 36; ++i) {
                itemStack.add(barrier);
            }
        }
        return itemStack;
    }
}
