package mrfast.sbf.core;

import com.google.gson.*;
import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.events.ProfileSwapEvent;
import mrfast.sbf.utils.NetworkUtils;
import mrfast.sbf.utils.Utils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataManager {
    static File dataFile = new File(SkyblockFeatures.modDir, "data.json");
    public static JsonObject dataJson = new JsonObject();
    static String currentProfileId;

    static {
        loadDataFromFile();
        if (dataJson.has("currentProfileId")) {
            currentProfileId = dataJson.get("currentProfileId").getAsString();
        }
        MinecraftForge.EVENT_BUS.post(new ProfileSwapEvent());
    }

    public static void loadDataFromFile() {
        try {
            String jsonContent = new String(Files.readAllBytes(Paths.get(dataFile.getPath())));
            dataJson = new JsonParser().parse(jsonContent).getAsJsonObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        pfidSentInChat = false;
        // Use api request to get profile id because no Profile Id message is sent if one profile
        Utils.setTimeout(()->{
            if(!pfidSentInChat) {
                pfidSentInChat = true;
                if (currentProfileId == null) {
                    currentProfileId = NetworkUtils.getLatestProfileID(Utils.GetMC().thePlayer.getUniqueID().toString());
                    dataJson.addProperty("currentProfileId", currentProfileId);
                    saveDataToFile();
                    MinecraftForge.EVENT_BUS.post(new ProfileSwapEvent());
                }
                MinecraftForge.EVENT_BUS.post(new ProfileSwapEvent());
            }
        },7000);
    }
    boolean pfidSentInChat = false;
    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        String regexPattern = "Profile ID: (\\S+)";
        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(event.message.getUnformattedText());

        if (matcher.find()) {
            pfidSentInChat = true;
            // Dont update if its the same
            if(currentProfileId!=null && currentProfileId.equals(matcher.group(1))) return;

            currentProfileId = matcher.group(1);
            dataJson.addProperty("currentProfileId", currentProfileId);
            saveDataToFile();
            MinecraftForge.EVENT_BUS.post(new ProfileSwapEvent());
        }
    }

    public static void saveData(String dataName, Object dataValue) {
        dataJson.add(dataName,convertToJsonObject(dataValue));
        saveDataToFile();
    }

    public static Object getData(String dataName) {
        return convertFromJsonElement(dataJson.get(dataName));
    }

    // Works with datanames such as "subset1.list.option2" or even just "option2"
    public static void saveProfileData(String dataName, Object dataValue) {
        if (currentProfileId == null) return;
        JsonObject profileJson = dataJson.getAsJsonObject(currentProfileId);

        if (profileJson == null) {
            profileJson = new JsonObject();
            dataJson.add(currentProfileId, profileJson);
        }

        String[] parts = dataName.split("\\.");

        for (int i = 0; i < parts.length - 1; i++) {
            if (!profileJson.has(parts[i]) || !profileJson.get(parts[i]).isJsonObject()) {
                profileJson.add(parts[i], new JsonObject());
            }
            profileJson = profileJson.getAsJsonObject(parts[i]);
        }

        profileJson.add(parts[parts.length - 1], convertToJsonObject(dataValue));
        saveDataToFile();
    }

    private static void saveDataToFile() {
        try (FileWriter writer = new FileWriter(dataFile)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(dataJson, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static JsonElement convertToJsonObject(Object dataValue) {
        if (dataValue instanceof String) {
            return new JsonPrimitive(String.valueOf(dataValue));
        } else if (dataValue instanceof Number) {
            return new JsonPrimitive((Number) dataValue);
        }  else if (dataValue instanceof Boolean) {
            return new JsonPrimitive((Boolean) dataValue);
        }else {
            // Handle other types as needed
            return new Gson().toJsonTree(dataValue);
        }
    }

    public static Object getProfileDataDefault(String dataName, Object obj) {
        if (getProfileData(dataName) == null) {
            return obj;
        }
        return getProfileData(dataName);
    }


    public static Object getProfileData(String dataName) {
        JsonObject profileJson = dataJson.getAsJsonObject(currentProfileId);

        if (profileJson == null) {
            return null;
        }

        String[] parts = dataName.split("\\.");

        for (int i = 0; i < parts.length - 1; i++) {
            JsonElement element = profileJson.get(parts[i]);
            if (element != null && element.isJsonObject()) {
                profileJson = element.getAsJsonObject();
            } else {
                return null;
            }
        }

        JsonElement lastElement = profileJson.get(parts[parts.length - 1]);
        if (lastElement != null) {
            return convertFromJsonElement(lastElement);
        } else {
            return null;
        }
    }

    private static Object convertFromJsonElement(JsonElement jsonElement) {
        if (jsonElement.isJsonPrimitive()) {
            if(jsonElement.getAsJsonPrimitive().isBoolean()) {
                return jsonElement.getAsJsonPrimitive().getAsBoolean();
            } else if(jsonElement.getAsJsonPrimitive().isNumber()) {
                String str = jsonElement.getAsJsonPrimitive().getAsString();
                if(str.contains(".")) {
                    return jsonElement.getAsJsonPrimitive().getAsNumber().doubleValue();
                }
                if(str.length()>10) {
                    return jsonElement.getAsJsonPrimitive().getAsNumber().longValue();
                }
                return jsonElement.getAsJsonPrimitive().getAsNumber().intValue();
            } else {
                return jsonElement.getAsJsonPrimitive().getAsString();
            }
        } else if (jsonElement.isJsonArray()) {
            return jsonElement.getAsJsonArray();
        } else if (jsonElement.isJsonObject()) {
            return jsonElement.getAsJsonObject();
        } else {
            return null; // Unsupported JSON element type
        }
    }
}
