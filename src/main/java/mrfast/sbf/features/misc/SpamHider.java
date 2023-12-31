package mrfast.sbf.features.misc;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.events.PacketEvent;
import mrfast.sbf.utils.Utils;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SpamHider {

    private static void cancelChatPacket(PacketEvent.ReceiveEvent ReceivePacketEvent) {
        if (!(ReceivePacketEvent.packet instanceof S02PacketChat)) return;
        ReceivePacketEvent.setCanceled(true);
        S02PacketChat packet = ((S02PacketChat) ReceivePacketEvent.packet);
        MinecraftForge.EVENT_BUS.post(new ClientChatReceivedEvent(packet.getType(), packet.getChatComponent()));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public void onChatPacket(PacketEvent.ReceiveEvent event) {
        if (!(event.packet instanceof S02PacketChat) || !Utils.inSkyblock) return;
        S02PacketChat packet = (S02PacketChat) event.packet;
        if (packet.getType() == 2) return;
        String unformatted = Utils.cleanColor(packet.getChatComponent().getUnformattedText());
        
        try {
            String u = unformatted.toLowerCase();
            if(SkyblockFeatures.config.hideRingOfLoveMsg && u.endsWith("ring of love requires higher quest completion! its stats and effects don't apply!")) {
                cancelChatPacket(event);
            }
            if(SkyblockFeatures.config.hideAdvertisments) {
                if (u.contains("/visit") || u.contains("lowballing") || u.contains("selling") || u.contains("buying") || u.contains("visit") || u.contains("ah") || u.contains("auction") || u.contains("guild")) {
                    // Dont remove messages from the auction house
                    if (unformatted.contains("[Auction]") || unformatted.contains("claimed") || unformatted.contains("Bid of") || unformatted.contains("created a") || unformatted.contains("Auction started"))
                        return;

                    cancelChatPacket(event);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
