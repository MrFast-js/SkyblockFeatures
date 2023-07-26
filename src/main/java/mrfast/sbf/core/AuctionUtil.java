package mrfast.sbf.core;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.apache.commons.io.IOUtils;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;

public class AuctionUtil {
    public HashMap<String, HashSet<String>> internalnameToAucIdMap = new HashMap<>();
    public TreeMap<String, HashMap<Integer, HashSet<String>>> extrasToAucIdMap = new TreeMap<>();

    private static Gson gson = new Gson();

    public static JsonObject getApiGZIPSync(String urlS) throws IOException {
        URL url = new URL(urlS);
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

        String response = IOUtils.toString(new GZIPInputStream(connection.getInputStream()), StandardCharsets.UTF_8);

        JsonObject json = gson.fromJson(response, JsonObject.class);
        return json;
    }
    private final static ExecutorService es = Executors.newFixedThreadPool(3);
    public static void getMyApiGZIPAsync(String urlS, Consumer<JsonObject> consumer, Runnable error) {
		es.submit(() -> {
			try {
				consumer.accept(getApiGZIPSync(urlS));
			} catch (Exception e) {
				error.run();
			}
		});
	}

    public static ItemStack jsonToStack(JsonObject json, boolean useCache) {
        return jsonToStack(json, useCache, true);
    }

    public static ItemStack jsonToStack(JsonObject json, boolean useCache, boolean useReplacements) {
        return jsonToStack(json, useCache, useReplacements, true);
    }

    public static ItemStack jsonToStack(JsonObject json, boolean useCache, boolean useReplacements, boolean copyStack) {
        if(json == null) return new ItemStack(Items.painting, 1, 10);

        ItemStack stack = new ItemStack(Item.itemRegistry.getObject(
                new ResourceLocation(json.get("itemid").getAsString())));

        if(json.has("count")) {
            stack.stackSize = json.get("count").getAsInt();
        }
        
        if(stack.getItem() == null) {
            stack = new ItemStack(Item.getItemFromBlock(Blocks.stone), 0, 255); //Purple broken texture item
        } else {
            if(json.has("damage")) {
                stack.setItemDamage(json.get("damage").getAsInt());
            }

            if(json.has("nbttag")) {
                try {
                    NBTTagCompound tag = JsonToNBT.getTagFromJson(json.get("nbttag").getAsString());
                    stack.setTagCompound(tag);
                } catch(NBTException e) {
                }
            }

            HashMap<String, String> replacements = new HashMap<>();

            if(json.has("lore")) {
                NBTTagCompound display = new NBTTagCompound();
                if(stack.getTagCompound() != null && stack.getTagCompound().hasKey("display")) {
                    display = stack.getTagCompound().getCompoundTag("display");
                }
                display.setTag("Lore", processLore(json.get("lore").getAsJsonArray(), replacements));
                NBTTagCompound tag = stack.getTagCompound() != null ? stack.getTagCompound() : new NBTTagCompound();
                tag.setTag("display", display);
                stack.setTagCompound(tag);
            }
        }

        return stack;
    }

    public static NBTTagList processLore(JsonArray lore, HashMap<String, String> replacements) {
        NBTTagList nbtLore = new NBTTagList();
        for(JsonElement line : lore) {
            String lineStr = line.getAsString();
            if(!lineStr.contains("Click to view recipes!") &&
                    !lineStr.contains("Click to view recipe!")) {
                for(Map.Entry<String, String> entry : replacements.entrySet()) {
                    lineStr = lineStr.replace("{"+entry.getKey()+"}", entry.getValue());
                }
                nbtLore.appendTag(new NBTTagString(lineStr));
            }
        }
        return nbtLore;
    }


    public static JsonObject getJsonFromNBT(NBTTagCompound tag) {
        return getJsonFromNBTEntry(tag.getTagList("i", 10).getCompoundTagAt(0));
    }

    public static JsonObject getJsonFromNBTEntry(NBTTagCompound tag) {
        if(tag.getKeySet().size() == 0) return null;

        int id = tag.getShort("id");
        int damage = tag.getShort("Damage");
        int count = tag.getShort("Count");
        tag = tag.getCompoundTag("tag");

        if(id == 141) id = 391; //for some reason hypixel thinks carrots have id 141

        String internalname = getInternalnameFromNBT(tag);
        if(internalname == null) return null;

        NBTTagCompound display = tag.getCompoundTag("display");
        String[] lore = getLoreFromNBT(tag);

        Item itemMc = Item.getItemById(id);
        String itemid = "null";
        if(itemMc != null) {
            itemid = itemMc.getRegistryName();
        }
        String displayname = display.getString("Name");

        JsonObject item = new JsonObject();
        item.addProperty("internalname", internalname);
        item.addProperty("itemid", itemid);
        item.addProperty("displayname", displayname);

        if(tag != null && tag.hasKey("ExtraAttributes", 10)) {
            NBTTagCompound ea = tag.getCompoundTag("ExtraAttributes");

            byte[] bytes = null;
            for(String key : ea.getKeySet()) {
                if(key.endsWith("backpack_data") || key.equals("new_year_cake_bag_data")) {
                    bytes = ea.getByteArray(key);
                    break;
                }
            }
            if(bytes != null) {
                JsonArray bytesArr = new JsonArray();
                for(byte b : bytes) {
                    bytesArr.add(new JsonPrimitive(b));
                }
                item.add("item_contents", bytesArr);
            }
            if(ea.hasKey("dungeon_item_level")) {
                item.addProperty("dungeon_item_level", ea.getInteger("dungeon_item_level"));
            }
        }

        if(lore != null && lore.length > 0) {
            JsonArray jsonLore = new JsonArray();
            for (String line : lore) {
                jsonLore.add(new JsonPrimitive(line));
            }
            item.add("lore", jsonLore);
        }

        item.addProperty("damage", damage);
        if(count > 1) item.addProperty("count", count);
        item.addProperty("nbttag", tag.toString());

        return item;
    }

    public static String getJsonFromNBTEntry2(NBTTagCompound tag) {
        if(tag.getKeySet().size() == 0) return null;

        int id = tag.getShort("id");
        int damage = tag.getShort("Damage");
        int count = tag.getShort("Count");
        tag = tag.getCompoundTag("tag");

        if(id == 141) id = 391; //for some reason hypixel thinks carrots have id 141

        String internalname = getInternalnameFromNBT(tag);
        if(internalname == null) return null;

        NBTTagCompound display = tag.getCompoundTag("display");
        String[] lore = getLoreFromNBT(tag);

        Item itemMc = Item.getItemById(id);
        String itemid = "null";
        if(itemMc != null) {
            itemid = itemMc.getRegistryName();
        }
        String displayname = display.getString("Name");

        JsonObject item = new JsonObject();
        item.addProperty("internalname", internalname);
        item.addProperty("itemid", itemid);
        item.addProperty("displayname", displayname);

        if(tag != null && tag.hasKey("ExtraAttributes", 10)) {
            NBTTagCompound ea = tag.getCompoundTag("ExtraAttributes");

            byte[] bytes = null;
            for(String key : ea.getKeySet()) {
                if(key.endsWith("backpack_data") || key.equals("new_year_cake_bag_data")) {
                    bytes = ea.getByteArray(key);
                    break;
                }
            }
            if(bytes != null) {
                JsonArray bytesArr = new JsonArray();
                for(byte b : bytes) {
                    bytesArr.add(new JsonPrimitive(b));
                }
                item.add("item_contents", bytesArr);
            }
            if(ea.hasKey("dungeon_item_level")) {
                item.addProperty("dungeon_item_level", ea.getInteger("dungeon_item_level"));
            }
        }

        if(lore != null && lore.length > 0) {
            JsonArray jsonLore = new JsonArray();
            for (String line : lore) {
                jsonLore.add(new JsonPrimitive(line));
            }
            item.add("lore", jsonLore);
        }

        item.addProperty("damage", damage);
        if(count > 1) item.addProperty("count", count);
        item.addProperty("nbttag", tag.toString());

        return item+"";
    }


    public static String[] getLoreFromNBT(NBTTagCompound tag) {
        String[] lore = new String[0];
        NBTTagCompound display = tag.getCompoundTag("display");

        if(display.hasKey("Lore", 9)) {
            NBTTagList list = display.getTagList("Lore", 8);
            lore = new String[list.tagCount()];
            for(int k=0; k<list.tagCount(); k++) {
                lore[k] = list.getStringTagAt(k);
            }
        }
        return lore;
    }

    public static String getInternalnameFromNBT(NBTTagCompound tag) {
        String internalname = null;
        if(tag != null && tag.hasKey("ExtraAttributes", 10)) {
            NBTTagCompound ea = tag.getCompoundTag("ExtraAttributes");

            if(ea.hasKey("id", 8)) {
                internalname = ea.getString("id").replaceAll(":", "-");
            } else {
                return null;
            }

            if("PET".equals(internalname)) {
                String petInfo = ea.getString("petInfo");
                if(petInfo.length() > 0) {
                    JsonObject petInfoObject = gson.fromJson(petInfo, JsonObject.class);
                    internalname = petInfoObject.get("type").getAsString();
                    String tier = petInfoObject.get("tier").getAsString();
                    switch(tier) {
                        case "COMMON":
                            internalname += ";0"; break;
                        case "UNCOMMON":
                            internalname += ";1"; break;
                        case "RARE":
                            internalname += ";2"; break;
                        case "EPIC":
                            internalname += ";3"; break;
                        case "LEGENDARY":
                            internalname += ";4"; break;
                        case "MYTHIC":
                            internalname += ";5"; break;
                    }
                }
            }
            if("ENCHANTED_BOOK".equals(internalname)) {
                NBTTagCompound enchants = ea.getCompoundTag("enchantments");

                for(String enchname : enchants.getKeySet()) {
                    internalname = enchname.toUpperCase() + ";" + enchants.getInteger(enchname);
                    break;
                }
            }
        }

        return internalname;
    }

}
