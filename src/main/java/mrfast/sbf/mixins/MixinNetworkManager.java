package mrfast.sbf.mixins;

import io.netty.channel.ChannelHandlerContext;
import mrfast.sbf.events.PacketEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkManager.class)
public class MixinNetworkManager {
    @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
    private void onReceivePacket(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        try {
            if (MinecraftForge.EVENT_BUS.post(new PacketEvent.ReceiveEvent(packet))) ci.cancel();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}