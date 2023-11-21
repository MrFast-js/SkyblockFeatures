package mrfast.sbf.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import mrfast.sbf.commands.FakePlayerCommand;
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

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import javax.net.ssl.SSLHandshakeException;


public class APIUtils {

    public static CloseableHttpClient client = HttpClients.custom().setUserAgent("Mozilla/5.0").build();
    public static JsonObject getJSONResponse(String urlString) {
        return getJSONResponse(urlString, new String[]{});
    }

    public static JsonObject getJSONResponse(String urlString,String[] headers) {
        if(Utils.isDeveloper()) {
            if (urlString.contains("#")) {
                String url = urlString.split("#")[0];
                String reason = urlString.split("#")[1];
                System.out.println("Sending request to " + url + " Reason: " + reason);
            } else {
                System.out.println("Sending request to " + urlString);
            }
        }
        // Split between 4 proxies to reduce lag
        int proxyNumber = (int) Utils.randomNumber(1,4);
        String proxy = "proxy"+proxyNumber;
        if(urlString.contains("api.hypixel.net")) {
            urlString = urlString.replace("api.hypixel.net", proxy+".mrfastkrunker.workers.dev");
        }

        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        try {
            HttpGet request = new HttpGet(new URL(urlString).toURI());
            request.setProtocolVersion(HttpVersion.HTTP_1_1);
            // Custom headers
            for (String header : headers) {
                String name = header.split("=")[0];
                String value = header.split("=")[1];
                request.setHeader(name, value);
            }

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
                    System.out.println(EnumChatFormatting.RED+"Unexpected Server Response: " + statusCode+" "+response+"  "+response.getStatusLine().getReasonPhrase());
                }
            }
        } catch (SSLHandshakeException ex) {
            // sometimes enrolled, work or school computers will have DNS blocking on all websites except allowed ones
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Your API request has been blocked by your administrator! "));
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

    public static int getPetRarity(String tier) {
        int rarity = 0;
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
        } catch (Exception ignored) {}
        return new JsonArray();
    }

    public static String getUUID(String username) {
        return getUUID(username,false);
    }
    public static String getUUID(String username,boolean formatted) {
        try {
            JsonObject uuidResponse = getJSONResponse("https://api.mojang.com/users/profiles/minecraft/" + username);
            String out = uuidResponse.get("id").getAsString();
            return formatted?formatUUID(out):out;
        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;
    }

    private static String formatUUID(String input) {
        return input.replaceAll("(.{8})(.{4})(.{4})(.{4})(.{12})", "$1-$2-$3-$4-$5");
    }

    private static final HashMap<String,String> nameCache = new HashMap<>();
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
            if(Utils.isDeveloper()) System.out.println("GOT https://api.hypixel.net/skyblock/profiles?uuid=" + uuid);
        } else {
            Utils.SendMessage(ChatFormatting.RED+"There was a problem with the "+ChatFormatting.YELLOW+"Hypixel API"+ChatFormatting.RED+". Is it down?");
            if(Utils.isDeveloper()) System.out.println("FAILED https://api.hypixel.net/skyblock/profiles?uuid=" + uuid);
            GuiUtils.openGui(null);
        }

        if (profilesResponse.has("error")) {
            String reason = profilesResponse.get("error").getAsString();
            Utils.SendMessage(EnumChatFormatting.RED + "Failed with reason: " + reason);
            GuiUtils.openGui(null);
            return null;
        }

        if (!profilesResponse.has("profiles") || profilesResponse.get("profiles").isJsonNull()) {
            Utils.SendMessage(EnumChatFormatting.RED + "This player has no Skyblock profiles!");
            GuiUtils.openGui(null);
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
        // This happens if the person hasn't logged on in a while
        if(latestProfile.isEmpty()) {
            if(Utils.isDeveloper()) System.out.println("No current profile found, selecting first");
            JsonObject profileJSON = profilesArray.get(0).getAsJsonObject();
            latestProfile = profileJSON.get("profile_id").getAsString();
            cuteName = profileJSON.get("cute_name").getAsString();
        }
        if(Utils.isDeveloper()) System.out.println("Found Latest Profile: " + latestProfile + " " + cuteName);
        return latestProfile;
    }
}
