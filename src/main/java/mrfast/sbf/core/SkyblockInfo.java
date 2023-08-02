package mrfast.sbf.core;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import mrfast.sbf.utils.ScoreboardUtil;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.registry.GameData;

public class SkyblockInfo {
    private static SkyblockInfo instance;
    public String location = "";
    public String map = "";
    public int coins = 0;

    public static SkyblockInfo getInstance() {
        if (instance == null) {
            instance = new SkyblockInfo();
        }
        return instance;
    }

    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
        if(Utils.GetMC().theWorld==null) return;
        try {
            String chatMessage = event.message.getUnformattedText();
            if(chatMessage.contains("You are sending commands too fast")) event.setCanceled(true);
            if (isJsonLikeMessage(chatMessage)) {
                event.setCanceled(true);
                System.out.println("FOUND JSON MESSAGE: "+chatMessage);
                if(chatMessage.contains("limbo")) {
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
    private boolean worldJustLoaded = false;
    @SubscribeEvent
    public void onWorldLoad(Load event) {
        this.worldJustLoaded = true;
        location = "";
        map = "";

    }

    int ticks = 0;

    @SubscribeEvent
    public void onTick(ClientTickEvent event) {
        if(Utils.GetMC().theWorld==null) return;
        if(worldJustLoaded) ticks++;
        if(ticks>=80 && worldJustLoaded) {
            ticks = 0;
            worldJustLoaded = false;
            Minecraft.getMinecraft().thePlayer.sendChatMessage("/locraw");
        }
        try {
            parseCoinsAndLocation();
        } catch (Exception e) {
            // TODO: handle exception
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