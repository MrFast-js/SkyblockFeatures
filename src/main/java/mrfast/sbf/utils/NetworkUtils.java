package mrfast.sbf.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.*;
import io.socket.engineio.client.Socket;
import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.events.SocketMessageEvent;
import net.minecraft.util.*;
import net.minecraftforge.common.MinecraftForge;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.mojang.realmsclient.gui.ChatFormatting;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import javax.net.ssl.*;


public class NetworkUtils {

    public static CloseableHttpClient client;

    static {
        SkyblockFeatures.config.temporaryAuthKey = "";
        SSLContextBuilder builder = new SSLContextBuilder();
        try {
            builder.loadTrustMaterial(null, (chain, authType) -> true);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        } catch (KeyStoreException ex) {
            throw new RuntimeException(ex);
        }
        SSLConnectionSocketFactory sslsf;
        try {
            sslsf = new SSLConnectionSocketFactory(builder.build());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (KeyManagementException e) {
            throw new RuntimeException(e);
        }

        client = HttpClients.custom().setSSLSocketFactory(sslsf).setUserAgent("Mozilla/5.0").build();
        System.out.println("CREATED CUSTOM CLIENT");
        setupSocket();
    }


    public static JsonObject getNetworth(String playerId, String selectedProfileUUID) {
        JsonObject response = getJSONResponse("https://soopy.dev/api/v2/player_skyblock/" + playerId + "?networth=true");
        JsonObject data = response.get("data").getAsJsonObject();
        JsonObject profiles = data.get("profiles").getAsJsonObject();
        JsonObject specificProfile = profiles.get(selectedProfileUUID.replace("-", "")).getAsJsonObject();
        JsonObject player = specificProfile.get("members").getAsJsonObject().get(playerId).getAsJsonObject();
        return player.get("nwDetailed").getAsJsonObject();
    }

    public static class CacheObject {
        Long createdAt;
        String url;
        JsonObject response;

        public CacheObject(String url, JsonObject res) {
            this.response = res;
            this.url = url;
            this.createdAt = System.currentTimeMillis();
        }
    }

    public static JsonObject getJSONResponse(String urlString) {
        return getJSONResponse(urlString, new String[]{});
    }

    public static JsonObject getJSONResponse(String urlString, boolean caching) {
        return getJSONResponse(urlString, new String[]{}, caching);
    }

    static HashMap<String, CacheObject> jsonCache = new HashMap<>();

    public static JsonObject getJSONResponse(String urlString, String[] headers) {
        return getJSONResponse(urlString, headers, true);
    }

    public static JsonObject getJSONResponse(String urlString, String[] headers, boolean caching) {
        return getJSONResponse(urlString, headers, true, true);
    }

    public static JsonObject getJSONResponse(String urlString, String[] headers, boolean caching, boolean useProxy) {

        if (urlString.contains("api.hypixel.net") && useProxy) {
            urlString = urlString.replace("https://api.hypixel.net", SkyblockFeatures.config.modAPIURL + "аpi");
        }
        boolean isMyApi = urlString.contains(SkyblockFeatures.config.modAPIURL);

        if (Utils.isDeveloper()) {
            if (urlString.contains("#")) {
                String url = urlString.split("#")[0];
                String reason = urlString.split("#")[1];
                System.out.println("Sending request to " + url + " Reason: " + reason);
            } else {
                System.out.println("Sending request to " + urlString);
            }
        }

        // 5 Minute Local Cache
        if (jsonCache.containsKey(urlString) && caching) {
            CacheObject obj = jsonCache.get(urlString);
            if (System.currentTimeMillis() - obj.createdAt < 1000 * 60 * 5) {
                if (Utils.isDeveloper()) System.out.println("Using Cache For: " + urlString);
                return obj.response;
            }
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
            if (isMyApi) {
                if (!SkyblockFeatures.config.temporaryAuthKey.isEmpty()) {
                    request.setHeader("temp-auth-key", SkyblockFeatures.config.temporaryAuthKey);
                }

                List<String> nearby = Utils.GetMC().theWorld.playerEntities.stream().filter((e) -> !Utils.isNPC(e)).map(EntityPlayer::getUniqueID).map(UUID::toString).limit(20).collect(Collectors.toList());

                // Server checks 2 random non-duplicate nearby player's uuids and checks if they are online to verify ingame auth
                request.setHeader("x-players", nearby.toString());
                // Send player author for logging past requests
                request.setHeader("x-request-author", Utils.GetMC().thePlayer.toString());
                // Send current mod version to server
                request.setHeader("x-version", SkyblockFeatures.VERSION);
            }
            try (CloseableHttpResponse response = client.execute(request)) {
                HttpEntity entity = response.getEntity();
                int statusCode = response.getStatusLine().getStatusCode();

                try (BufferedReader in = new BufferedReader(new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8))) {
                    Gson gson = new Gson();
                    JsonObject out = gson.fromJson(in, JsonObject.class);
                    if (isMyApi) {
                        if (out.has("auth-key")) {
                            SkyblockFeatures.config.temporaryAuthKey = out.get("auth-key").getAsString();
                            System.out.println("GOT AUTH KEY " + SkyblockFeatures.config.temporaryAuthKey);
                            return getJSONResponse(urlString, headers);
                        }
                        if (statusCode != 200) {
                            Utils.sendMessage(ChatFormatting.RED + "Server Error: " + out.get("cause").getAsString() + " " + ChatFormatting.YELLOW + ChatFormatting.ITALIC + out.get("err_code") + " " + urlString);
                            return null;
                        }
                    }
                    CacheObject cache = new CacheObject(urlString, out);
                    jsonCache.put(urlString, cache);

                    return out;
                }
            }
        } catch (SSLHandshakeException ex) {
            // sometimes enrolled, work or school computers will have DNS blocking on all websites except allowed ones
            System.out.println(urlString);
            ex.printStackTrace();
            player.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Your API request has been blocked by your administrator!"));
        } catch (JsonSyntaxException ex) {
            // Will typically happen if the server is offline so will return '502 Bad Gateway'
            System.out.println(urlString);
            ex.printStackTrace();
            if (isMyApi) {
                Utils.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "The Skyblock Features API service seems to be down. Try again later."));
            } else {
                String baseUrl = urlString.split("/")[2];
                Utils.sendMessage(new ChatComponentText(EnumChatFormatting.RED + "The " + baseUrl + " service seems to be down. Try again later."));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new JsonObject();
    }

    // Only used for UUID => Username
    public static JsonArray getArrayResponse(String urlString) {
        try {
            HttpGet request = new HttpGet(new URL(urlString).toURI());

            request.setProtocolVersion(HttpVersion.HTTP_1_1);

            HttpResponse response = client.execute(request);

            HttpEntity entity = response.getEntity();

            if (response.getStatusLine().getStatusCode() == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8));
                String input;
                StringBuilder r = new StringBuilder();

                while ((input = in.readLine()) != null) {
                    r.append(input);
                }
                in.close();

                Gson gson = new Gson();

                return gson.fromJson(r.toString(), JsonArray.class);
            }
        } catch (Exception ignored) {
        }
        return new JsonArray();
    }

    private static final HashMap<String, String> nameCache = new HashMap<>();
    public static String getUUID(String username) {
        return getUUID(username, false);
    }

    public static String getUUID(String username, boolean formatted) {
        if(nameCache.containsValue(username.toLowerCase())) {
            System.out.println("Found cached UUID for " + username);
            for(Map.Entry<String,String> entry : nameCache.entrySet()) {
                if(entry.getValue().equals(username)) {
                    return formatted ? formatUUID(entry.getKey()) : entry.getKey();
                }
            }
        }
        System.out.println("Getting UUID for " + username);
        try {
            JsonObject uuidResponse = getJSONResponse("https://api.mojang.com/users/profiles/minecraft/" + username);
            String out = uuidResponse.get("id").getAsString();
            nameCache.put(out, uuidResponse.get("name").getAsString().toLowerCase());
            return formatted ? formatUUID(out) : out;
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return null;
    }

    private static String formatUUID(String input) {
        return input.replaceAll("(.{8})(.{4})(.{4})(.{4})(.{12})", "$1-$2-$3-$4-$5");
    }

    public static String getName(String uuid) {
        if (nameCache.containsKey(uuid)) return nameCache.get(uuid);
        try {
            JsonObject json = getJSONResponse("https://api.mojang.com/user/profile/" + uuid);
            if (json.has("error")) return null;
            nameCache.put(uuid, json.get("name").getAsString().toLowerCase());
            return json.get("name").getAsString();
        } catch (Exception e) {
            return null;
        }
    }

    static HashMap<String, String> latestProfileCache = new HashMap<>();

    public static String getLatestProfileID(String uuid) {
        if (latestProfileCache.containsKey(uuid)) {
            return latestProfileCache.get(uuid);
        }
        String latestProfile = "";
        String cuteName = "";
        JsonObject profilesResponse = getJSONResponse("https://api.hypixel.net/skyblock/profiles?uuid=" + uuid);
        if (profilesResponse.toString().length() > 2) {
            if (Utils.isDeveloper()) System.out.println("GOT https://api.hypixel.net/skyblock/profiles?uuid=" + uuid);
        } else {
            Utils.sendMessage(ChatFormatting.RED + "There was a problem with the " + ChatFormatting.YELLOW + "Hypixel API" + ChatFormatting.RED + ". Is it down?");
            if (Utils.isDeveloper())
                System.out.println("FAILED https://api.hypixel.net/skyblock/profiles?uuid=" + uuid);
            GuiUtils.openGui(null);
        }

        if (profilesResponse.has("error")) {
            String reason = profilesResponse.get("error").getAsString();
            Utils.sendMessage(EnumChatFormatting.RED + "Failed with reason: " + reason);
            GuiUtils.openGui(null);
            return null;
        }

        if (!profilesResponse.has("profiles") || profilesResponse.get("profiles").isJsonNull()) {
            Utils.sendMessage(EnumChatFormatting.RED + "This player has no Skyblock profiles!");
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
        if (latestProfile.isEmpty()) {
            if (Utils.isDeveloper()) System.out.println("No current profile found, selecting first");
            JsonObject profileJSON = profilesArray.get(0).getAsJsonObject();
            latestProfile = profileJSON.get("profile_id").getAsString();
            cuteName = profileJSON.get("cute_name").getAsString();
        }
        if (Utils.isDeveloper()) System.out.println("Found Latest Profile: " + latestProfile + " " + cuteName);
        latestProfileCache.put(uuid, latestProfile);
        return latestProfile;
    }

    public static Socket socket;
    static boolean internalClose = false;
    public static boolean socketConnected = true;
    public static void setupSocket() {
        System.out.println("Attempting connection to SBF websocket!");
        try {
            if(socket!=null) {
                internalClose=true;
                socket.close();
            }
            // Connect to the Socket.IO server
            socket = new Socket("ws://app.mrfast-developer.com:1512");
            socket.on(Socket.EVENT_OPEN, args -> {
                System.out.println("Opened connection to SBF websocket!");
                socketConnected = true;
                internalClose = false;
            });

            socket.on(Socket.EVENT_MESSAGE, args -> {
                if (args.length > 0) {
                    String eventType = args[0].toString().split("~")[0];
                    String data = args[0].toString().split("~")[1];
                    MinecraftForge.EVENT_BUS.post(new SocketMessageEvent(socket, data,eventType));
                }
            });

            socket.on(Socket.EVENT_CLOSE, args -> {
                if(internalClose) return;
                System.out.println("Lost connection to SBF websocket! Retrying in 5 seconds..");
                Utils.setTimeout(NetworkUtils::setupSocket, 5000);
                socketConnected = false;
            });

            socket.open();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
