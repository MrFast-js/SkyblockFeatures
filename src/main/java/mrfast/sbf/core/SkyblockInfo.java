package mrfast.sbf.core;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;
import mrfast.sbf.events.PacketEvent;
import mrfast.sbf.utils.ScoreboardUtil;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
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

    public static void getPing() {
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

    static boolean waitingForLocrawResponse = false;
    static boolean gotLocrawResponse = false;

    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
        String chatMessage = event.message.getUnformattedText();
        if (isJsonLikeMessage(chatMessage) && waitingForLocrawResponse) {
            gotLocrawResponse = true;
            waitingForLocrawResponse = false;
            event.setCanceled(true);
            if(chatMessage.contains("limbo")) {
                gotLocrawResponse = false;
                waitingForLocrawResponse = true;
                Utils.setTimeout(SkyblockInfo::sendLocraw, 2000);
            }
            parseMap(chatMessage);
        }
    }
    

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        waitingForLocrawResponse = false;
        gotLocrawResponse = false;
        location = "";
        map = "";
        ticks = 0;
    }

    int ticks = 0;

    @SubscribeEvent
    public void onTick(ClientTickEvent event) {
        if (event.phase != Phase.END || Utils.GetMC().theWorld == null) return;
        if(!waitingForLocrawResponse) ticks++;

        if (ticks >= 20 && Utils.isOnHypixel() && !waitingForLocrawResponse) {
            ticks = 0;
            sendLocraw();
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
                location = entry.substring(2);
            } else if (entry.contains("ф")) {
                location = entry.substring(2);
            }
            localLocation=location.replaceAll("[^a-zA-Z0-9\\s]", "").replaceAll("[^\\x00-\\x7F]", "");
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
        System.out.println(chatMessage);
        if (jsonObject.has("server")) {
            if (jsonObject.has("map")) {
                map = jsonObject.get("map").getAsString();
            } else {
                Utils.setTimeout(()->{
                    sendLocraw();
                }, 1000);
            }
        }
    }

    public static void sendLocraw() {
        if(gotLocrawResponse || waitingForLocrawResponse) return;
        waitingForLocrawResponse = true;
        Minecraft.getMinecraft().thePlayer.sendChatMessage("/locraw");
        System.out.println("Sending /locraw");
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