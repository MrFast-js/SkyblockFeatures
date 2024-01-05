package mrfast.sbf.core;

import com.google.gson.*;
import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.events.ProfileSwapEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
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
    static JsonObject dataJson = new JsonObject();
    static String currentProfileId;

    static {
        loadDataFromFile();
        if (dataJson.has("currentProfileId")) {
            currentProfileId = dataJson.get("currentProfileId").getAsString();
        }
    }

    public static void loadDataFromFile() {
        try {
            String jsonContent = new String(Files.readAllBytes(Paths.get(dataFile.getPath())));
            dataJson = new JsonParser().parse(jsonContent).getAsJsonObject();
            System.out.println("Loaded Profile From Data " + dataJson.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static boolean initialPfId = false;
    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        String regexPattern = "Profile ID: (\\S+)";
        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(event.message.getUnformattedText());

        if (matcher.find()) {
            if(currentProfileId!=null && currentProfileId.equals(matcher.group(1)) && initialPfId) return;

            currentProfileId = matcher.group(1);
            dataJson.addProperty("currentProfileId", currentProfileId);
            saveDataToFile();
            MinecraftForge.EVENT_BUS.post(new ProfileSwapEvent());
        }
    }


    public static void saveProfileData(String dataName, Object dataValue) {
        if (currentProfileId == null) return;
        // Get the JSON object for the current profile ID
        JsonObject profileJson = dataJson.getAsJsonObject(currentProfileId);

        // If the profileJson is null, create a new JsonObject for the profile
        if (profileJson == null) {
            profileJson = new JsonObject();
            dataJson.add(currentProfileId, profileJson);
        }

        // Split the dataName into parts based on the dot
        String[] parts = dataName.split("\\.");

        // Traverse through the parts and create nested JSON objects as needed
        for (int i = 0; i < parts.length - 1; i++) {
            if (!profileJson.has(parts[i]) || !profileJson.get(parts[i]).isJsonObject()) {
                profileJson.add(parts[i], new JsonObject());
            }
            profileJson = profileJson.getAsJsonObject(parts[i]);
        }

        // Add the final dataValue to the last nested JSON object
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
        // Get the JSON object for the current profile ID
        JsonObject profileJson = dataJson.getAsJsonObject(currentProfileId);

        // If the profileJson is null, return null
        if (profileJson == null) {
            return null;
        }

        // Split the dataName into parts based on the dot
        String[] parts = dataName.split("\\.");

        // Traverse through the parts to find the nested JSON object
        for (int i = 0; i < parts.length - 1; i++) {
            JsonElement element = profileJson.get(parts[i]);
            if (element != null && element.isJsonObject()) {
                profileJson = element.getAsJsonObject();
            } else {
                return null; // Key doesn't exist or is not a JsonObject
            }
        }

        // Get the final dataValue from the last nested JSON object
        JsonElement lastElement = profileJson.get(parts[parts.length - 1]);
        if (lastElement != null) {
            return convertFromJsonElement(lastElement);
        } else {
            return null; // Key doesn't exist
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
