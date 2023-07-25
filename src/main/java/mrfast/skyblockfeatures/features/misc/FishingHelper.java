package mrfast.skyblockfeatures.features.misc;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.skyblockfeatures.SkyblockFeatures;
import mrfast.skyblockfeatures.events.PacketEvent;
import mrfast.skyblockfeatures.gui.GuiManager;
import mrfast.skyblockfeatures.utils.RenderUtil;
import mrfast.skyblockfeatures.core.SkyblockInfo;
import mrfast.skyblockfeatures.utils.Utils;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FishingHelper {
    boolean reelingIn = false;
    public static List<Vec3> particles = new ArrayList<Vec3>();
    List<Vec3> fishingParticles = new ArrayList<Vec3>();
    List<Vec3> dupefishingParticles = new ArrayList<Vec3>();

    boolean clearingSoon = false;
    public static Entity fishingHook = null;
    public static Vec3 lastClosest = null;
    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        if(geyser != null && SkyblockFeatures.config.geyserBoundingBox) {
            double x = geyser.getXCoordinate();
            double y = 118;
            double z = geyser.getZCoordinate();
            Color color = 
                fishingHook!=null?
                    fishingHook.getDistance(geyser.getXCoordinate(), fishingHook.posY, geyser.getZCoordinate())<=1?
                        new Color(85,255,85): // Green
                    new Color(255,85,255): // Magenta
                new Color(255,85,255); // Magenta
            RenderUtil.drawOutlinedFilledBoundingBox(new AxisAlignedBB(x-1, y-0.1, z-1, x+1, y-0.09, z+1),color,event.partialTicks);
        }
        if(fishingHook != null && !clearingSoon && SkyblockFeatures.config.fishthing) {
            Vec3 closestParticle = null;
            try {
                for(Vec3 particle : fishingParticles) {
                    if(particle==null) continue;
                    if(closestParticle==null) {
                        boolean chainFish = false;
                        for(Vec3 particle2 : fishingParticles) {
                            if(particle==null || particle==null || particle2==particle) continue;
                            if(particle2.distanceTo(particle)<0.05) {
                                chainFish = true;
                            }
                        }
                        if(chainFish) closestParticle = particle;
                    } else {
                        if(particle.distanceTo(fishingHook.getPositionVector())<closestParticle.distanceTo(fishingHook.getPositionVector())) {
                            boolean chainFish = false;
                            for(Vec3 particle2 : fishingParticles) {
                                if(particle==null || particle==null) continue;
                                if(particle2 == particle) continue;
                                if(particle2.distanceTo(particle)<0.05) {
                                    chainFish = true;
                                }
                            }
                            if(chainFish) closestParticle = particle;
                        }
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
            if(closestParticle!=null) {
                if(lastClosest!=null) {
                    double x = lastClosest.xCoord;
                    double z = lastClosest.zCoord;
                    
                    double newX = closestParticle.xCoord;
                    double newZ = closestParticle.zCoord;
    
                    double deltaX = newX-x;
                    double deltaZ = newZ-z;
    
                    x+=deltaX/20;
                    z+=deltaZ/20;
                    closestParticle = new Vec3(x,closestParticle.yCoord,z);
                }
                lastClosest = closestParticle;
                GlStateManager.disableCull();
                if(closestParticle==null || fishingHook == null) return;
                RenderUtil.draw3DLine(closestParticle, fishingHook.getPositionVector(), 1, new Color(255, 85, 85), event.partialTicks);
                GlStateManager.enableCull();

                RenderUtil.drawOutlinedFilledBoundingBox(new AxisAlignedBB(closestParticle.xCoord-0.05, closestParticle.yCoord, closestParticle.zCoord-0.05, closestParticle.xCoord+0.05, closestParticle.yCoord+0.05, closestParticle.zCoord+0.05),new Color(0xFF00AA),event.partialTicks);
                try {
                    double widthOfSquare = closestParticle.distanceTo(fishingHook.getPositionVector());
                    Vec3 hook = fishingHook.getPositionVector();
                    Color color = widthOfSquare<0.15?Color.GREEN:Color.RED;
                    RenderUtil.drawOutlinedFilledBoundingBox(new AxisAlignedBB(hook.xCoord-widthOfSquare, hook.yCoord+0.1, hook.zCoord-widthOfSquare, hook.xCoord+widthOfSquare, hook.yCoord+0.1, hook.zCoord+widthOfSquare),color,event.partialTicks);
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
        }
        Vec3 prev = null;
        // Diana helper
        if(SkyblockFeatures.config.MythologicalHelper) {
            prev = null;
            try {
                double xDif = 0;
                double zDif = 0;
                double yDif = 0;
                double index = 0;
                for(Vec3 particle : particles) {
                    index++;
                    if(prev == null) {
                        prev = particle;
                        continue;
                    }
                    GlStateManager.disableCull();
                    RenderUtil.draw3DArrowLine(prev, particle, new Color(255, 85, 85));
                    GlStateManager.enableCull();
                    xDif = prev.xCoord-particle.xCoord;
                    zDif = prev.zCoord-particle.zCoord;
                    yDif = prev.yCoord-particle.yCoord;
                    if(index == particles.size()) {
                        for(int i=0;i<300;i++) {
                            RenderUtil.draw3DArrowLine(prev, new Vec3(particle.xCoord+xDif*-(i),particle.yCoord-yDif*i,particle.zCoord+zDif*-(i)), Color.white);

                            prev = new Vec3(particle.xCoord+xDif*-(i),particle.yCoord-yDif*i,particle.zCoord+zDif*-(i));
                        }
                    }
                    prev = particle;
                }
            } catch (Exception e) {
                //TODO: handle exception
            }
        };
    }
    Vec3 oldParticle = null;
    S2APacketParticles geyser = null;
    
    @SubscribeEvent
    public void onRecievePacket(PacketEvent.ReceiveEvent event) {
        if (!Utils.inSkyblock || Utils.GetMC().theWorld == null || !SkyblockFeatures.config.fishthing || reelingIn || !Utils.GetMC().inGameHasFocus) return;
        if(event.packet instanceof S2APacketParticles) {
            S2APacketParticles packet = (S2APacketParticles) event.packet;
            EnumParticleTypes type = packet.getParticleType();
            if(SkyblockInfo.getInstance().location.contains("olcano")) {
                Entity hook = null;
                for(Entity entity:Utils.GetMC().theWorld.loadedEntityList) {
                    if(entity instanceof EntityFishHook) {
                        if(!(((EntityFishHook) entity).angler instanceof EntityOtherPlayerMP)) hook = entity;
                    }
                }
                if(type == EnumParticleTypes.CLOUD && SkyblockFeatures.config.geyserBoundingBox) {
                    geyser = packet;
                    fishingHook = hook;
                }
                if(hook != null && SkyblockFeatures.config.hideGeyserParticles) {
                    if(type == EnumParticleTypes.SMOKE_NORMAL) {
                        if(!(packet.getYCoordinate() > hook.posY-0.2 && packet.getYCoordinate() < hook.posY+0.2)) {
                            event.setCanceled(true);
                        }
                    } else {
                        event.setCanceled(true);
                    }
                }
            }
            
            if(Utils.GetMC().thePlayer.getHeldItem() != null && Utils.GetMC().thePlayer.getHeldItem().getDisplayName().contains("Ancestral") && SkyblockFeatures.config.MythologicalHelper) {
                if(type == EnumParticleTypes.DRIP_LAVA) {
                    double dist = Utils.GetMC().thePlayer.getDistance(packet.getXCoordinate(), packet.getYCoordinate(), packet.getZCoordinate());
                    if(dist>3 && dist<5) {
                        particles.clear();
                    }
                    
                    if(dist>2) {
                        if(!particles.contains(new Vec3(packet.getXCoordinate(), packet.getYCoordinate(), packet.getZCoordinate()))) {
                            particles.add(new Vec3(packet.getXCoordinate(), packet.getYCoordinate(), packet.getZCoordinate()));
                        }
                    }
                }
            }

            if (type == EnumParticleTypes.WATER_WAKE) {
                Entity hook = null;
                for(Entity entity:Utils.GetMC().theWorld.loadedEntityList) {
                    if(entity instanceof EntityFishHook) {
                        if(!(((EntityFishHook) entity).angler instanceof EntityOtherPlayerMP)) hook = entity;
                    }
                }
                if(hook != null) {
                    fishingHook = hook;
                    Vec3 pos = new Vec3(packet.getXCoordinate(), packet.getYCoordinate(), packet.getZCoordinate());
                    if(fishingHook.getDistance(packet.getXCoordinate(), packet.getYCoordinate(), packet.getZCoordinate())<6 && !fishingParticles.contains(pos)) {
                        fishingParticles.add(pos);
                    }
                    if(fishingHook.getDistance(packet.getXCoordinate(), packet.getYCoordinate(), packet.getZCoordinate())<0.15 && Utils.GetMC().thePlayer.canEntityBeSeen(fishingHook)) {
                        if(Utils.GetMC().thePlayer.getHeldItem().getItem() instanceof ItemFishingRod) {
                            reelingIn = true;
                            GuiManager.createTitle(ChatFormatting.GREEN+"Reel it in!", 10);
                            Utils.GetMC().thePlayer.playSound("note.pling", 1, 2);
                            fishingParticles.clear();
                            fishingHook = null;
                        } else {
                            fishingParticles.clear();
                            fishingHook = null;
                        }
                        Utils.setTimeout(()->{
                            reelingIn = false;
                            fishingParticles.clear();
                            fishingHook = null;
                        }, 500);
                    }
                } else {
                    clearingSoon = true;
                    Utils.setTimeout(()->{
                        fishingParticles.clear();
                    }, 200);
                    Utils.setTimeout(()->{
                        clearingSoon = false;
                    }, 400);
                }
            }
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        particles.clear();
        fishingParticles.clear();
        dupefishingParticles.clear();;
        clearingSoon = false;
        fishingHook = null;
        lastClosest = null;
    }
}
