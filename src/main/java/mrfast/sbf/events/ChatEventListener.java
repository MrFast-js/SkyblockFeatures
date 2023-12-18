package mrfast.sbf.events;

import mrfast.sbf.core.ConfigManager;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Arrays;
import java.util.List;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.utils.Utils;

public class ChatEventListener {

    @SubscribeEvent(receiveCanceled = true, priority = EventPriority.HIGHEST)
    public void onChat(ClientChatReceivedEvent event) {
        if (!Utils.isOnHypixel()) return;
        String unformatted = Utils.cleanColor(event.message.getUnformattedText());

        if(event.message.getFormattedText().contains(": ")) {
            if (SkyblockFeatures.config.DisguisePlayersAs == 8 && SkyblockFeatures.config.playerDiguiser && Utils.inSkyblock) {
                String name = event.message.getFormattedText().split(": ")[0];
                String message = event.message.getFormattedText().split(": ")[1];
                StringBuilder monkiMessage = new StringBuilder();
                for(String word:message.split(" ")) {
                    List<String> words = Arrays.asList("Ooh","ooh","ah","Ee","Hoo","Grrr","uuh");
                    monkiMessage.append(words.get(Utils.randomNumber(0, 6))).append(" ");
                }
                event.setCanceled(true);
                Utils.GetMC().thePlayer.addChatMessage(new ChatComponentText(name+": "+monkiMessage));
            }
        }

        if(unformatted.startsWith("You have joined ") && unformatted.contains("party!") && SkyblockFeatures.config.autoPartyChat) {
            Utils.GetMC().thePlayer.sendChatMessage("/chat p");
            Utils.setTimeout(()->{
                Utils.sendMessage(EnumChatFormatting.YELLOW + "Auto Joined Party Chat.");
            },10);
        }
        
        // Welcome message
        if (SkyblockFeatures.config.firstLaunch && unformatted.equals("Welcome to Hypixel SkyBlock!")) {
            Utils.sendMessage("§bThank You for downloading Skyblock Features!§e Do /sbf for config!");

            SkyblockFeatures.config.firstLaunch = false;
            ConfigManager.saveConfig(SkyblockFeatures.config);

        }
    }
}
