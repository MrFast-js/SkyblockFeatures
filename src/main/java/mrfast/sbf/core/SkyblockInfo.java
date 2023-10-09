package mrfast.sbf.core;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.events.PacketEvent;
import mrfast.sbf.utils.ScoreboardUtil;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.network.play.client.C16PacketClientStatus.EnumState;
import net.minecraft.network.play.server.S37PacketStatistics;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class SkyblockInfo {
    private static SkyblockInfo instance;
    public String location = "";
    public String localLocation = "";
    public String map = "";
    public int coins = 0;

    public static SkyblockInfo getInstance() {
        if (instance == null) {
            instance = new SkyblockInfo();
        }
        return instance;
    }

    private static long startTime;
    private static boolean waitingForResponse = false;

    public static void getPing(String serverIp) {
        ServerData test = new ServerData("🫵🤓", serverIp, false);
       
        sendServerQuery(test);
    }

    private static void sendServerQuery(ServerData serverData) {
        NetworkManager networkManager = Minecraft.getMinecraft().getNetHandler().getNetworkManager();
        C16PacketClientStatus queryPacket = new C16PacketClientStatus(EnumState.REQUEST_STATS);
 
        // Record the start time
        startTime = System.currentTimeMillis();

        // Send the query packet to the server
        networkManager.sendPacket(queryPacket);

        // Set waitingForResponse to true to avoid sending multiple queries in one tick
        waitingForResponse = true;
    }

    @SubscribeEvent
    public void onPacket(PacketEvent.ReceiveEvent event) {
        if(waitingForResponse && event.packet instanceof S37PacketStatistics) {
            waitingForResponse = false;
            Utils.playSound("random.orb", 0.1);
            Utils.SendMessage(ChatFormatting.GREEN+"Your current ping is "+ChatFormatting.YELLOW+(System.currentTimeMillis()-startTime)+"ms");
        }
    }

    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
        if(Utils.GetMC().theWorld==null) return;
        try {
            String chatMessage = event.message.getUnformattedText();
            if (isJsonLikeMessage(chatMessage)) {
                event.setCanceled(true);
                if(chatMessage.contains("limbo") && Utils.inSkyblock) {
                    Utils.setTimeout(()->{
                       Minecraft.getMinecraft().thePlayer.sendChatMessage("/locraw");
                    }, 2000);
                }
                parseMap(chatMessage);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        
    }
    
    private static boolean worldJustLoaded = false;

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        worldJustLoaded = true;
        location = "";
        map = "";
    }

    int ticks = 0;

    @SubscribeEvent
    public void onTick(ClientTickEvent event) {
        if (event.phase != Phase.END) {
            return; // Only run on the client side at the end of a tick
        }

        if (Utils.GetMC().theWorld == null) {
            return;
        }

        if (worldJustLoaded) {
            ticks++;
        }

        if (ticks >= 80 && worldJustLoaded && Utils.isOnHypixel()) {
            ticks = 0;
            worldJustLoaded = false;
            Minecraft.getMinecraft().thePlayer.sendChatMessage("/locraw");
        }

        try {
            parseCoinsAndLocation();
        } catch (Exception e) {
            // Handle exceptions
        }
    }
    private void parseCoinsAndLocation() {
        for (String entry : ScoreboardUtil.getSidebarLines()) {
            entry = Utils.cleanColor(entry);
            if (entry.contains("Purse:") || entry.contains("Piggy:")) {
                coins = parseCoins(entry.replaceAll("[^0-9]", ""));
            } else if (entry.contains("⏣")) {
                location = entry.substring(2, entry.length());
            } else if (entry.contains("ф")) {
                location = entry.substring(2, entry.length());
            }
            localLocation=location.replaceAll("[^a-zA-Z0-9\\s]", "");
        }
    }

    private int parseCoins(String coinsString) {
        try {
            return Integer.parseInt(coinsString);
        } catch (NumberFormatException e) {
            return 0;   
        }
    }

    private boolean isJsonLikeMessage(String chatMessage) {
        // Check if the message contains curly braces ({}) as a simple heuristic
        return chatMessage.contains("{\"server\"");
    }

    private void parseMap(String chatMessage) {
        JsonObject jsonObject = new JsonObject();
        try {
            jsonObject = new Gson().fromJson(chatMessage, JsonObject.class);
        } catch (Exception e) {
            // Invalid JSON format, do error handling if needed
            e.printStackTrace();
        }
        if (jsonObject.has("server")) {
            if (jsonObject.has("map")) {
                map = jsonObject.get("map").getAsString();
            } else {
                Utils.setTimeout(()->{
                    if(!worldJustLoaded) return;
                    Minecraft.getMinecraft().thePlayer.sendChatMessage("/locraw");
                    worldJustLoaded = false;
                }, 1000);
            }
        }
    }


    public String getLocation() {
        return location;
    }

    public int getCoins() {
        return coins;
    }

    public String getMap() {
        return map;
    }
}