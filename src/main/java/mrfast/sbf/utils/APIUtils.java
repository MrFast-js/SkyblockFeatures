package mrfast.sbf.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
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

import mrfast.sbf.SkyblockFeatures;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;


public class APIUtils {

    public static CloseableHttpClient client = HttpClients.custom().setUserAgent("Mozilla/5.0").build();

    public static JsonObject getJSONResponse(String urlString) {
        if(urlString.contains("#")) {
            String url = urlString.split("#")[0];
            String reason = urlString.split("#")[1];
            System.out.println("Sending request to " + url+" Reason: "+reason);
        } else {
            System.out.println("Sending request to " + urlString);
        }
        // PROXYYYYY so me api key aint known ty @nea
        if(urlString.contains("api.hypixel.net")) urlString = urlString.replace("api.hypixel.net", "proxy.mrfastkrunker.workers.dev");

        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        try {
            HttpGet request = new HttpGet(new URL(urlString).toURI());
            request.setProtocolVersion(HttpVersion.HTTP_1_1);

            request.setHeader("Authentication", "Skyblock-Features-Mod");

            try (CloseableHttpResponse response = client.execute(request)) {
                HttpEntity entity = response.getEntity();
                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode == 200) {
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(entity.getContent(),StandardCharsets.UTF_8))) {
                        Gson gson = new Gson();
                        return gson.fromJson(in, JsonObject.class);
                    }
                } else {
                    System.out.println(EnumChatFormatting.RED+"Unexpected Server Response: " + statusCode);
                }
                response.close();
            }
        } catch (Exception ex) {
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "An error has occurred."));
            ex.printStackTrace();
        }
        return new JsonObject();
    }
    public static HashMap<String,String> rankCache = new HashMap<>();

    public static String getHypixelRank(String uuid) {
        if(rankCache.containsKey(uuid)) return rankCache.get(uuid);

        JsonObject json = APIUtils.getJSONResponse("https://api.hypixel.net/player?uuid="+uuid+"#getHypixelRank").get("player").getAsJsonObject();
        String rank = "§7";
        if(json.has("mostRecentMonthlyPackageRank")) rank = json.get("mostRecentMonthlyPackageRank").getAsString();
        else if(json.has("newPackageRank")) rank = json.get("newPackageRank").getAsString();

        rankCache.put(uuid, convertRank(rank)+json.get("displayname").getAsString());
        return convertRank(rank)+json.get("displayname").getAsString();
    }

    public static String convertRank(String rank) {
        switch (rank) {
            case "VIP":
                return "§a[VIP] ";
            case "VIP_PLUS":
                return "§a[VIP§6+§a] ";
            case "MVP":
                return "§b[MVP] ";
            case "MVP_PLUS":
                return "§b[MVP§c+§b] ";
            case "SUPERSTAR":
                return "§6[MVP§c++§6] ";
            default:
                return "§7";
        }
    }

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
            }
        } catch (Exception ex) {}
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

    public static HashMap<String,String> nameCache = new HashMap<>();
    public static String getName(String uuid) {
        if(nameCache.containsKey(uuid)) return nameCache.get(uuid);
        try {
            JsonObject json = getJSONResponse("https://api.mojang.com/user/profile/"+uuid);
            if(json.has("error")) return null;
            
            nameCache.put(uuid, json.get("name").getAsString());
            return json.get("name").getAsString();
        } catch (Exception e) {
            return null;
        }
    }

    public static String getLatestProfileID(String uuid) {
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
        if(latestProfile=="") {
            System.out.println("No currentt profile found, selecting first");
            JsonObject profileJSON = profilesArray.get(0).getAsJsonObject();
            latestProfile = profileJSON.get("profile_id").getAsString();
            cuteName = profileJSON.get("cute_name").getAsString();
        }
        System.out.println("LATEST PROFILE: " + latestProfile + " " + cuteName);
        return latestProfile;
    }
}
