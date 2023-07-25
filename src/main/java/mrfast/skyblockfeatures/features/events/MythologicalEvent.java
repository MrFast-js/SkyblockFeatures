package mrfast.skyblockfeatures.features.events;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.skyblockfeatures.SkyblockFeatures;
import mrfast.skyblockfeatures.events.PacketEvent;
import mrfast.skyblockfeatures.features.misc.FishingHelper;
import mrfast.skyblockfeatures.utils.RenderUtil;
import mrfast.skyblockfeatures.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class MythologicalEvent {
    BlockPos burrow = null;
    BlockPos prevBurrow = null;
    boolean sendNotif = true;
    @SubscribeEvent
    public void onRecievePacket(PacketEvent.ReceiveEvent event) {
        if (!Utils.inSkyblock || Utils.GetMC().theWorld == null || !Utils.GetMC().inGameHasFocus || !SkyblockFeatures.config.MythologicalHelper) return;
        if(event.packet instanceof S2APacketParticles) {
            S2APacketParticles packet = (S2APacketParticles) event.packet;
            EnumParticleTypes type = packet.getParticleType();
            if(type == EnumParticleTypes.FOOTSTEP) {
                if(!(burrow!=null && (burrow.getX()==Math.floor(packet.getXCoordinate()) || burrow.getZ()==Math.floor(packet.getZCoordinate())))) {
                    if(sendNotif) {
                        sendNotif = false;
                        Utils.SendMessage(ChatFormatting.GREEN+"Located new Griffin Burrow!");
                        Utils.playSound("random.orb", 0.1);
                        Utils.setTimeout(()->{
                            sendNotif = true;
                        }, 15*1000);
                    }
                    
                    burrow = new BlockPos(Math.floor(packet.getXCoordinate()),Math.floor(packet.getYCoordinate()),Math.floor(packet.getZCoordinate()));
                }
            }
        }
    }
    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (!Utils.inSkyblock || event.type == 2 || !SkyblockFeatures.config.MythologicalHelper) return;

        String unformatted = Utils.cleanColor(event.message.getUnformattedText());
        if(unformatted.contains("You dug out a Griffin Burrow") || unformatted.contains("You finished the Griffin burrow chain")) {
            endPos = null;
            startPos = null;
            FishingHelper.particles.clear();
            Utils.setTimeout(()->{
                prevBurrow = burrow;
                burrow = null;
                sendNotif = true;
            }, 300);
        }
    }
    Vec3 startPos = null;
    Vec3 endPos = null;

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        if (!Utils.inSkyblock || !SkyblockFeatures.config.MythologicalHelper) return;
        if(burrow!=null && SkyblockFeatures.config.MythologicalHelper) {
            AxisAlignedBB aabb2 = new AxisAlignedBB(burrow, burrow.add(1, 1, 1));
            RenderUtil.drawOutlinedFilledBoundingBox(aabb2, Color.green, event.partialTicks);

            AxisAlignedBB aabb = new AxisAlignedBB(burrow.getX()+0.5, burrow.getY()+100, burrow.getZ()+0.5, burrow.getX()+0.5, burrow.getY(), burrow.getZ()+0.5);
            RenderUtil.drawOutlinedFilledBoundingBox(aabb, Color.green, event.partialTicks);
        }
        if(startPos != null && endPos !=null) {
            GlStateManager.pushMatrix();
            GlStateManager.disableDepth(); 
            RenderUtil.draw3DArrowLine(startPos, endPos, Color.cyan);
            GlStateManager.enableDepth();
            GlStateManager.popMatrix();
        }
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent event) {
        if (!Utils.inSkyblock || !SkyblockFeatures.config.MythologicalHelper || prevBurrow==null || Utils.GetMC().theWorld==null) return;
        for (Entity entity : Utils.GetMC().theWorld.getLoadedEntityList()) {
            if (entity instanceof EntityArmorStand) {
                EntityArmorStand armorStand = (EntityArmorStand) entity;
                ItemStack head = armorStand.getEquipmentInSlot(0);
                if (head==null) continue;
                if (head.getUnlocalizedName().contains("arrow") && armorStand.getDistance(prevBurrow.getX(),prevBurrow.getY(),prevBurrow.getZ())<5) {
                    Vec3 startPos2 = new Vec3(armorStand.posX, armorStand.posY + 2, armorStand.posZ);
                    double angle = Math.toRadians(armorStand.rotationYaw+180);
                    double lineLength = 500.0; // Adjust the line length as needed
                    double endX = armorStand.posX + lineLength * Math.sin(angle);
                    double endY = armorStand.posY + 2;
                    double endZ = armorStand.posZ - lineLength * Math.cos(angle);
                    Vec3 endPos2 = new Vec3(endX, endY, endZ);
                    startPos = startPos2.addVector(-0.5,0,-0.5);
                    endPos = endPos2.addVector(-0.5,0,-0.5);
                }
            }
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        burrow = null;
        startPos = null;
        endPos = null;
    }
}
