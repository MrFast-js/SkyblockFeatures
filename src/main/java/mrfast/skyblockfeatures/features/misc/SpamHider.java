package mrfast.skyblockfeatures.features.misc;

import mrfast.skyblockfeatures.SkyblockFeatures;
import mrfast.skyblockfeatures.events.PacketEvent;
import mrfast.skyblockfeatures.utils.Utils;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SpamHider {

    private static void cancelChatPacket(PacketEvent.ReceiveEvent ReceivePacketEvent, boolean addToSpam) {
        if (!(ReceivePacketEvent.packet instanceof S02PacketChat)) return;
        ReceivePacketEvent.setCanceled(true);
        S02PacketChat packet = ((S02PacketChat) ReceivePacketEvent.packet);
        MinecraftForge.EVENT_BUS.post(new ClientChatReceivedEvent(packet.getType(), packet.getChatComponent()));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void onChatPacket(PacketEvent.ReceiveEvent event) {
        if (!(event.packet instanceof S02PacketChat) || !SkyblockFeatures.config.hideAdvertisments || !Utils.inSkyblock) return;
        S02PacketChat packet = (S02PacketChat) event.packet;
        if (packet.getType() == 2) return;
        String unformatted = Utils.cleanColor(packet.getChatComponent().getUnformattedText());
        
        try {
            if (unformatted.contains("[Auction]") || unformatted.contains("claimed") || unformatted.contains("Bid of") || unformatted.contains("created a") || unformatted.contains("Auction started")) return;
            String u = unformatted.toLowerCase();
            if (u.contains("cheap")||u.contains("/visit")||u.contains("lowballing")||u.contains("selling")||u.contains("buying")||u.contains("visit")||u.contains("ah")||u.contains("auction")) {
                cancelChatPacket(event, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
