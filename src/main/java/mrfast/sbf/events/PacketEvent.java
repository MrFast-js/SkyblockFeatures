package mrfast.sbf.events;

import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;


/**
 * Modified from Skytils 0.x under GNU Affero General Public License v3.0
 * https://github.com/Skytils/SkytilsMod/tree/0.x
 *
 * @author Sychic
 */
@Cancelable
public class PacketEvent extends Event {
    public Packet<?> packet;

    public PacketEvent(Packet<?> packet) {
        this.packet = packet;
    }

    public static class ReceiveEvent extends PacketEvent {
        public ReceiveEvent(Packet<?> packet) {
            super(packet);
        }
    }

    public static class SendEvent extends PacketEvent {
        public SendEvent(Packet<?> packet) {
            super(packet);
        }
    }
}
