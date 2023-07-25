package mrfast.skyblockfeatures.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.skyblockfeatures.SkyblockFeatures;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;


public class APIUtil {

    public static CloseableHttpClient client = HttpClients.custom().setUserAgent("Mozilla/5.0").build();

    public static JsonObject getJSONResponse(String urlString) {
        System.out.println("Sending request to " + urlString);
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        try {
            HttpGet request = new HttpGet(new URL(urlString).toURI());
            request.setProtocolVersion(HttpVersion.HTTP_1_1);
            
            if (urlString.contains("https://api.hypixel.net")) {
                // Skyblock Features Production API Key
                request.setHeader("API-Key", SkyblockFeatures.config.apiKey);
            }

            try (CloseableHttpResponse response = client.execute(request)) {
                HttpEntity entity = response.getEntity();
                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode == 200) {
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(entity.getContent()))) {
                        Gson gson = new Gson();
                        System.out.println("sending full");
                        return gson.fromJson(in, JsonObject.class);
                    }
                } else {
                    Utils.SendMessage("non 200 is " + statusCode);
                }
                response.close();
            }
        } catch (Exception ex) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "An error has occurred."));
            ex.printStackTrace();
        }
        return new JsonObject();
    }

    // public static void getPetObject(JsonObject pet) {
    //     String petName = pet.get("type").getAsString();
    //     String petRarity = getPetRarity(pet.get("tier").getAsString()).toString();
    //     String url = "https://raw.githubusercontent.com/NotEnoughUpdates/NotEnoughUpdates-REPO/782dc74181469b2171d573fad74d64d0cd9f62ef/items/"+petName+";"+petRarity+".json";
    //     JsonObject petObject = getJSONResponse(url);
    //     System.out.println(url+" "+petObject);
    // }

    public static Integer getPetRarity(String tier) {
        Integer rarity = 0;
        if(tier.equals("COMMON")) rarity = 1;
        if(tier.equals("UNCOMMON")) rarity = 2;
        if(tier.equals("RARE")) rarity = 3;
        if(tier.equals("EPIC")) rarity = 4;
        if(tier.equals("LEGENDARY")) rarity = 5;
        if(tier.equals("MYTHIC")) rarity = 6;
        return rarity;
    }

    public static JsonObject getNetworthResponse(JsonObject data) {
        try {
            // Specify the URL
            URL url = new URL("http://maro.skyblockextras.com/api/networth/categories");

            // Create the connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Create the JSON body
            String jsonBody = data.toString();  // Replace with your JSON body

            // Write the JSON body to the request
            try (OutputStream outputStream = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                outputStream.write(input, 0, input.length);
            }

            // Get the response code
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            // Read the response
            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            // Parse the response JSON
            Gson gson = new Gson();
            JsonObject jsonResponse = gson.fromJson(response.toString(), JsonObject.class);

            // Print the parsed JSON
            System.out.println("Got Networth");
            // Close the connection
            connection.disconnect();
            return jsonResponse;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new JsonObject();
    }


    // Only used for UUID => Username
    public static JsonArray getArrayResponse(String urlString) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;

        try {
            HttpGet request = new HttpGet(new URL(urlString).toURI());

            request.setProtocolVersion(HttpVersion.HTTP_1_1);

            HttpResponse response = client.execute(request);

            HttpEntity entity = response.getEntity();

            if (response.getStatusLine().getStatusCode() == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(entity.getContent()));
                String input;
                StringBuilder r = new StringBuilder();

                while ((input = in.readLine()) != null) {
                    r.append(input);
                }
                in.close();

                Gson gson = new Gson();

                return gson.fromJson(r.toString(), JsonArray.class);
            } else {
                // player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Request failed. HTTP Error Code: " + response.getStatusLine().getStatusCode()));
            }
        } catch (IOException | URISyntaxException ex) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "An error has occured."));
            // ex.printStackTrace();
        }
        return new JsonArray();
    }

    public static String getUUID(String username) {
        try {
            JsonObject uuidResponse = getJSONResponse("https://api.mojang.com/users/profiles/minecraft/" + username);
            return uuidResponse.get("id").getAsString();
        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;
    }
    private static Gson gson = new Gson();
    public static String getName(String uuid) {
        try {
            try (Scanner scanner = new Scanner(new URL("https://api.mojang.com/user/profile/" + uuid).openStream(), "UTF-8").useDelimiter("\\A")) {
                String json = scanner.next();
                JsonArray array = gson.fromJson(json, JsonArray.class);
                return array.get(array.size() - 1).getAsJsonObject().get("name").getAsString();
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static String getLatestProfileID(String uuid, String key) {
        String latestProfile = "";
        String cuteName = "";
        JsonObject profilesResponse = getJSONResponse("https://api.hypixel.net/skyblock/profiles?uuid=" + uuid);
        if(profilesResponse.toString().length()>2) {
            System.out.println("GOT https://api.hypixel.net/skyblock/profiles?uuid=" + uuid);
        } else {
            Utils.SendMessage(ChatFormatting.RED+"There was a problem with the "+ChatFormatting.YELLOW+"Hypixel API"+ChatFormatting.RED+". Is it down?");
            System.out.println("FAILED https://api.hypixel.net/skyblock/profiles?uuid=" + uuid);
        }
        if (profilesResponse.has("error")) {
            String reason = profilesResponse.get("error").getAsString();
            Utils.SendMessage(EnumChatFormatting.RED + "Failed with reason: " + reason);
            return null;
        }
        if (!profilesResponse.has("profiles")) {
            return null;
        }
        JsonArray profilesArray = profilesResponse.get("profiles").getAsJsonArray();
        for (JsonElement a : profilesArray) {
            JsonObject profileJSON = a.getAsJsonObject();
            if (profileJSON.has("selected") && profileJSON.get("selected").getAsBoolean()) {
                latestProfile = profileJSON.get("profile_id").getAsString();
                cuteName = profileJSON.get("cute_name").getAsString();
                break;
            }
        }
        System.out.println("LATEST PROFILE: " + latestProfile + " " + cuteName);
        return latestProfile;
    }
}
