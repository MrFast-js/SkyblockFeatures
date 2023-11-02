package mrfast.sbf.events;

import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.utils.APIUtils;
import mrfast.sbf.utils.ItemUtils;
import mrfast.sbf.utils.Utils;

public class ChatEventListener {
    public static Minecraft mc = Minecraft.getMinecraft();

    public static boolean alreadySent = false;
    int barCount = 0;

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        barCount = 0;
    }
    
    @SubscribeEvent(receiveCanceled = true, priority = EventPriority.HIGHEST)
    public void onChat(ClientChatReceivedEvent event) {
        if (!Utils.isOnHypixel()) return;
        String unformatted = Utils.cleanColor(event.message.getUnformattedText());

        if(event.message.getFormattedText().contains(": ")) {
            if (SkyblockFeatures.config.DisguisePlayersAs == 8 && SkyblockFeatures.config.playerDiguiser && Utils.inSkyblock) {
                String name = event.message.getFormattedText().split(": ")[0];
                String message = event.message.getFormattedText().split(": ")[1];
                String monkiMessage = "";
                for(String word:message.split(" ")) {
                    List<String> words = Arrays.asList("Ooh","ooh","ah","Ee","Hoo","Grrr","uuh");
                    monkiMessage+=words.get((int) Utils.randomNumber(0, 6))+" ";
                }
                event.setCanceled(true);
                Utils.GetMC().thePlayer.addChatMessage(new ChatComponentText(name+": "+monkiMessage));
            }
        }

        if(unformatted.startsWith("You have joined ") && unformatted.contains("party!") && SkyblockFeatures.config.autoPartyChat) {
            Utils.GetMC().thePlayer.sendChatMessage("/chat p");
            Utils.setTimeout(()->{
                Utils.SendMessage(EnumChatFormatting.YELLOW + "Auto Joined Party Chat.");
            },10);
        }
        
        // Welcome message
        if (SkyblockFeatures.config.firstLaunch && unformatted.equals("Welcome to Hypixel SkyBlock!")) {
            Utils.SendMessage("§bThank You for downloading Skyblock Features!§e Do /sbf for config!");

            SkyblockFeatures.config.firstLaunch = false;
            SkyblockFeatures.config.forceSave();
        }
    }
}
