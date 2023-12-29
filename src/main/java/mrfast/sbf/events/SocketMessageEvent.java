package mrfast.sbf.events;

import io.socket.engineio.client.Socket;
import mrfast.sbf.core.SkyblockMobDetector.SkyblockMob;
import net.minecraftforge.fml.common.eventhandler.Event;

public class SocketMessageEvent extends Event {
    public Socket socket;
    public String message;
    public String type;
    public SocketMessageEvent(Socket socket, String message,String type) {
        this.socket = socket;
        this.message = message;
        this.type=type;
    }
}
