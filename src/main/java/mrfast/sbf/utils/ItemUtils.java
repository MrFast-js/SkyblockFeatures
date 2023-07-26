package mrfast.sbf.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import mrfast.sbf.core.PricingData;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

public class ItemUtils {
    private static final Pattern RARITY_PATTERN = Pattern.compile("(§[0-9a-f]§l§ka§r )?([§0-9a-fk-or]+)(?<rarity>[A-Z]+)");
    public static final int NBT_INTEGER = 3;
    public static final int NBT_STRING = 8;
    public static final int NBT_LIST = 9;
    public static final int NBT_COMPOUND = 10;

    /**
     * Returns the display name of a given item
     * @author Mojang
     * @param item the Item to get the display name of
     * @return the display name of the item
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
     * @author BiscuitDevelopment
     * @param item the Skyblock item to check
     * @return the Skyblock Item ID of this item or {@code null} if this isn't a valid Skyblock item
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
     * @author BiscuitDevelopment
     * @param item the item to get the tag from
     * @return the item's {@code ExtraAttributes} compound tag or {@code null} if the item doesn't have one
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
     * @author BiscuitDevelopment
     * @param extraAttributes the NBT to check
     * @return the Skyblock Item ID of this item or {@code null} if this isn't a valid Skyblock NBT
     */
    public static String getSkyBlockItemID(NBTTagCompound extraAttributes) {
        if (extraAttributes != null) {
            String itemId = extraAttributes.getString("id");

            if (!itemId.equals("")) {
                return itemId;
            }
        }

        return null;
    }


    /**
     * Returns a string list containing the nbt lore of an ItemStack, or
     * an empty list if this item doesn't have a lore. The returned lore
     * list is unmodifiable since it has been converted from an NBTTagList.
     *
     * @author BiscuitDevelopment
     * @param itemStack the ItemStack to get the lore from
     * @return the lore of an ItemStack as a string list
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
     * @author BiscuitDevelopment
     * @param item the Skyblock item to check
     * @return the rarity of the item if a valid rarity is found, {@code null} if no rarity is found, {@code null} if item is {@code null}
     */
    public static ItemRarity getRarity(ItemStack item) {
        if (item == null || !item.hasTagCompound())  {
            return null;
        }

        NBTTagCompound display = item.getSubCompound("display", false);

        if (display == null || !display.hasKey("Lore")) {
            return null;
        }

        NBTTagList lore = display.getTagList("Lore", Constants.NBT.TAG_STRING);
        String name = display.getString("Name");

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
        return null;
    }

    public static ItemRarity getRarity(ItemStack item, String a) {
        if (item == null || !item.hasTagCompound())  {
            return ItemRarity.COMMON;
        }

        NBTTagCompound display = item.getSubCompound("display", false);

        if (display == null || !display.hasKey("Lore")) {
            return ItemRarity.COMMON;
        }

        NBTTagList lore = display.getTagList("Lore", Constants.NBT.TAG_STRING);
        String name = display.getString("Name");

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
    static HashMap<String,JsonObject> itemMap = new HashMap<>();
    
    public static double getEstimatedItemValue(ItemStack stack) {
        if(itemMap.size()==0) {
            JsonArray items = APIUtils.getArrayResponse("https://raw.githubusercontent.com/Altpapier/SkyHelper-Networth/abb278d6be1e13b3204ccb05f47c5e8aaf614733/constants/items.json");
            for(int i=0;i<items.size();i++) {
                JsonObject a = items.get(i).getAsJsonObject();
                itemMap.put(a.get("id").getAsString(), a);
            }
        }
        String id = PricingData.getIdentifier(stack);
        NBTTagCompound ExtraAttributes = getExtraAttributes(stack);
        double total = 0;

        try {
            // Add lowest bin as a base price
            total+=PricingData.lowestBINs.get(id);
            // Add wither essence value
            total+=getStarCost(ExtraAttributes);
            // Add enchants
            total+=getEnchantsWorth(ExtraAttributes);
            // Hbp, recombs
            total+=getUpgradeCost(ExtraAttributes);
            // gemstones
            total+=getGemstoneWorth(ExtraAttributes);   

        } catch (Exception e) {
            // TODO: handle exception
        }
        
        return total;
    }

    public static Integer getEstimatedItemValue(NBTTagCompound ExtraAttributes) {
        if(itemMap.size()==0) {
            JsonArray items = APIUtils.getArrayResponse("https://raw.githubusercontent.com/Altpapier/SkyHelper-Networth/abb278d6be1e13b3204ccb05f47c5e8aaf614733/constants/items.json");
            for(int i=0;i<items.size();i++) {
                JsonObject a = items.get(i).getAsJsonObject();
                itemMap.put(a.get("id").getAsString(), a);
            }
        }
        String id = ExtraAttributes.getString("id");
        if(!PricingData.averageLowestBINs.containsKey(id)) return 0;
        double total = 0;

        try {
            // Add lowest bin as a base price
            total+=PricingData.lowestBINs.get(id);
            // Add wither essence value
            total+=getStarCost(ExtraAttributes);
            // Add enchants
            total+=getEnchantsWorth(ExtraAttributes);   
            // Hbp, recombs
            total+=getUpgradeCost(ExtraAttributes);   
            // gemstones
            total+=getGemstoneWorth(ExtraAttributes);   
        } catch (Exception e) {
            // TODO: handle exception
        }
        
        return (int) total;
    }

    public static double getUpgradeCost(NBTTagCompound ExtraAttributes) {
        double total = 0;
        // Hot potato books
        if (ExtraAttributes.hasKey("hot_potato_count")) {
            int hpb = ExtraAttributes.getInteger("hot_potato_count");
            if (hpb > 10) {
            if(PricingData.bazaarPrices.get("FUMING_POTATO_BOOK")!=null) total+= (PricingData.bazaarPrices.get("FUMING_POTATO_BOOK")*0.6);
            }
            if(PricingData.bazaarPrices.get("HOT_POTATO_BOOK")!=null) {
                total+= (PricingData.bazaarPrices.get("HOT_POTATO_BOOK")*Math.min(hpb, 10));
            }
        }

        if (ExtraAttributes.hasKey("rarity_upgrades") && !ExtraAttributes.hasKey("item_tier")) {
            if(PricingData.bazaarPrices.get("RECOMBOBULATOR_3000")!=null) {
                total+= PricingData.bazaarPrices.get("RECOMBOBULATOR_3000");
            }
        }
        return total;
    }

    public static double getStarCost(NBTTagCompound extraAttributes) {
        String id = extraAttributes.getString("id");
        int stars = 0;
        int masterStars = 0;
        int totalEssence = 0;
        String essenceType = "WITHER";
        try {
            if(extraAttributes.hasKey("upgrade_level")) {
                stars = extraAttributes.getInteger("upgrade_level");
                if(stars>5) {
                    masterStars = stars-5;
                    stars = 5;
                }
            }
            else if(extraAttributes.hasKey("dungeon_item_level")) {
                stars = extraAttributes.getInteger("dungeon_item_level");
                if(stars>5) {
                    masterStars = stars-5;
                    stars = 5;
                }
            } else {
                return 0;
            }
            JsonArray upgradeCosts = itemMap.get(id).get("upgrade_costs").getAsJsonArray();
            for(int i=0;i<stars;i++) {
                JsonElement b = upgradeCosts.get(i).getAsJsonArray().get(0);
                totalEssence+=b.getAsJsonObject().get("amount").getAsInt();
                essenceType=b.getAsJsonObject().get("essence_type").getAsString();
            }
        } catch (Exception e) {
            // e.printStackTrace();
            // TODO: handle exception
        }
        Double pricePerEssence = PricingData.bazaarPrices.get("ESSENCE_"+essenceType);
        Double masterStarPrice = 0d;
        try {
            if(masterStars>0) {
                if(masterStars>=1) masterStarPrice+=PricingData.bazaarPrices.get("FIRST_MASTER_STAR");
                if(masterStars>=2) masterStarPrice+=PricingData.bazaarPrices.get("SECOND_MASTER_STAR");
                if(masterStars>=3) masterStarPrice+=PricingData.bazaarPrices.get("THIRD_MASTER_STAR");
                if(masterStars>=4) masterStarPrice+=PricingData.bazaarPrices.get("FOURTH_MASTER_STAR");
                if(masterStars==5) masterStarPrice+=PricingData.bazaarPrices.get("FIFTH_MASTER_STAR");
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return (totalEssence*pricePerEssence)+masterStarPrice;
    }

    public static double getGemstoneWorth(NBTTagCompound extraAttributes) {
        if (!extraAttributes.hasKey("gems")) return 0;
        NBTTagCompound nbt = extraAttributes.getCompoundTag("gems");
        int total = 0;
        List<String> finalGemstones = new ArrayList<>();

        for (String enchant : nbt.getKeySet()) {
            if(enchant.contains("_gem")) continue;
            if(nbt.hasKey(enchant+"_gem")) {
                String gemstone = nbt.getString(enchant)+"_"+nbt.getString(enchant+"_gem")+"_GEM";
                finalGemstones.add(gemstone);
            } else {
                String type = enchant.split("_")[0];
                String tier = nbt.getString(enchant);
                finalGemstones.add(tier+"_"+type+"_GEM");
            }
        }

        for (String gemstone : finalGemstones) {
            try {
                total+=PricingData.bazaarPrices.get(gemstone);
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
        return total;
    }

    public static double getEnchantsWorth(NBTTagCompound extraAttributes) {
        if(!extraAttributes.hasKey("enchantments")) return 0;
        NBTTagCompound nbt = extraAttributes.getCompoundTag("enchantments");
        int total = 0;
        for(String enchant:nbt.getKeySet()) {
            String id = "ENCHANTMENT_"+enchant.toUpperCase()+"_"+nbt.getInteger(enchant);
            if(PricingData.bazaarPrices.get(id)!=null) {
                if(id.contains("ENCHANTMENT_SCAVENGER")||id.contains("ENCHANTMENT_INFINITE_QUIVER")) continue;
                total+=PricingData.bazaarPrices.get(id)*0.85;
            }
        }
        return total;
    }
}
