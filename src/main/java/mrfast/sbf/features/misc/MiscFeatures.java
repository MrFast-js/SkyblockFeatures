package mrfast.sbf.features.misc;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.SkyblockInfo;
import mrfast.sbf.events.CheckRenderEntityEvent;
import mrfast.sbf.events.GuiContainerEvent;
import mrfast.sbf.events.PacketEvent;
import mrfast.sbf.events.GuiContainerEvent.TitleDrawnEvent;
import mrfast.sbf.utils.APIUtils;
import mrfast.sbf.utils.ItemUtils;
import mrfast.sbf.utils.RenderUtil;
import mrfast.sbf.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class MiscFeatures {
    public static HashMap<Entity,EntityVillager> tracker = new HashMap<Entity,EntityVillager>();

    @SubscribeEvent
    public void onWorldChanges(WorldEvent.Load event) {
        try {
            tracker.clear();
        } catch(Exception e) {

        }
    }

    @SubscribeEvent
    public void onCheckRender(CheckRenderEntityEvent event) {
        if (!Utils.inSkyblock) return;
        if(SkyblockFeatures.config.hideArrows && event.entity instanceof EntityArrow) {
            event.setCanceled(true);
        }
        if(SkyblockFeatures.config.hidePlayersNearNPC) {
            for(Entity entity: Utils.GetMC().theWorld.loadedEntityList) {
                if(Utils.isNPC(entity) && event.entity instanceof EntityPlayer && entity.getDistanceToEntity(event.entity)<3 && entity!=event.entity) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderOverlayPre(RenderGameOverlayEvent.Pre event) {
        if (!Utils.inSkyblock) return;
        if (event.type == RenderGameOverlayEvent.ElementType.AIR && SkyblockFeatures.config.hideAirDisplay && !Utils.inDungeons) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderBlockOverlay(RenderBlockOverlayEvent event) {
        if (Utils.inSkyblock && SkyblockFeatures.config.noFire && event.overlayType == RenderBlockOverlayEvent.OverlayType.FIRE) {
            event.setCanceled(true);
        }
    }
    
    List<Vec3> particles = new ArrayList<Vec3>();
    @SubscribeEvent
    public void onRecievePacket(PacketEvent.ReceiveEvent event) {
        if(event.packet instanceof S2APacketParticles  && SkyblockFeatures.config.highlightMushrooms) {
            S2APacketParticles packet = (S2APacketParticles) event.packet;
            EnumParticleTypes type = packet.getParticleType();
            Vec3 pos = new Vec3(Math.floor(packet.getXCoordinate()),Math.floor(packet.getYCoordinate()),Math.floor(packet.getZCoordinate()));
            boolean dupe = false;
            for(Vec3 part:particles) {
                if(part.distanceTo(pos) < 1 || part==pos) {
                    dupe = true;
                }
            }
            
            if(!dupe && type == EnumParticleTypes.SPELL_MOB && SkyblockInfo.getInstance().localLocation.contains("Glowing")) {
                particles.add(pos);
            }
        }
    }
    HashMap<EntityTNTPrimed,Double> tntExistTimes = new HashMap<>();
    int tick = 0;
    @SubscribeEvent
    public void onTick(ClientTickEvent event) {
        if(Utils.GetMC().theWorld==null || !SkyblockFeatures.config.tntTimer) return;
        
        tick++;
        for(Entity entity:Utils.GetMC().theWorld.loadedEntityList) {
            if(entity instanceof EntityTNTPrimed && !tntExistTimes.containsKey(entity)) {
                tntExistTimes.put((EntityTNTPrimed) entity, 4.7d);
            }
        }
        if(tick==4) {
            tick = 0;
            for(EntityTNTPrimed tnt:tntExistTimes.keySet()) {
                if(tntExistTimes.get(tnt)==0.1) continue;
                tntExistTimes.put(tnt,Math.floor((tntExistTimes.get(tnt)-0.1)*10)/10);
            }
        }
    }
    @SubscribeEvent
    public void RenderBlockOverlayEvent(DrawBlockHighlightEvent event) {
        try {
            if(Utils.GetMC().thePlayer.getHeldItem()!=null && SkyblockFeatures.config.teleportDestination) {
                ItemStack item = Utils.GetMC().thePlayer.getHeldItem();
                String id = ItemUtils.getSkyBlockItemID(item);
                if(id!=null)
                if(id.contains("ASPECT_OF_THE_END") || id.contains("ASPECT_OF_THE_VOID")) {
                    Double distance = 8.0;
                    Double etherDistance = 8.0;
                    Boolean hasEtherwarp = false;
                    for(String line:ItemUtils.getItemLore(item)) {
                        line = Utils.cleanColor(line);
                        if(line.contains("Teleport")) {
                            try {
                                distance = Double.parseDouble(line.replaceAll("[^0-9]", ""));
                            } catch (Exception e) {
                                // TODO: handle exception
                            }
                        }
                        if(line.contains("up to")) {
                            try {
                                etherDistance = Double.parseDouble(line.replaceAll("[^0-9]", ""));
                            } catch (Exception e) {
                                // TODO: handle exception
                            }
                        }
                        if(line.contains("Ether")) {
                            hasEtherwarp = true;
                        }
                    }
                    MovingObjectPosition lookingBlock = Utils.GetMC().thePlayer.rayTrace(distance, event.partialTicks);
                    if(hasEtherwarp && Utils.GetMC().thePlayer.isSneaking())  {
                        lookingBlock = Utils.GetMC().thePlayer.rayTrace(etherDistance, event.partialTicks);
                    }
                    AxisAlignedBB box = new AxisAlignedBB(lookingBlock.getBlockPos(), lookingBlock.getBlockPos().add(1, 1, 1));
    
                    if(!(Utils.GetMC().theWorld.getBlockState(lookingBlock.getBlockPos()).getBlock() instanceof BlockAir)) {
                        RenderUtil.drawOutlinedFilledBoundingBox(box, new Color(0x324ca8), event.partialTicks);
                    }
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        if(SkyblockFeatures.config.tntTimer) {
            for(EntityTNTPrimed tnt:tntExistTimes.keySet()) {
                if(tnt.isDead) {
                    tntExistTimes.remove(tnt);
                    return;
                }
                if(tntExistTimes.get(tnt)==0) {
                    return;
                }
                RenderUtil.draw3DStringWithShadow(tnt.getPositionVector().addVector(0, 1.5, 0), ChatFormatting.GREEN+""+tntExistTimes.get(tnt).toString(), 0xFFFFFF, event.partialTicks);
            }
        }
        if(SkyblockInfo.getInstance().localLocation.contains("Glowing") && SkyblockFeatures.config.highlightMushrooms) {
            try {
                for(Vec3 packet:particles) {
                    Color color = new Color(0x55FF55);
                    highlightBlock(color, Math.floor(packet.xCoord),Math.floor(packet.yCoord), Math.floor(packet.zCoord), event.partialTicks);

                    Block block = Minecraft.getMinecraft().theWorld.getBlockState(new BlockPos(packet)).getBlock();
                    if(block != null && block == Blocks.air) {
                        particles.remove(packet);
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    public static void highlightBlock(Color c, double d, double d1, double d2, float ticks) {
        RenderUtil.drawOutlinedFilledBoundingBox(new AxisAlignedBB(d + 1.0D, d1 + 1, d2 + 1.0D, d, d1, d2),c,ticks);
    }
    boolean gotNetworth = false;
    boolean tryingNetworth = false;
    Double networth = 0d;
    Double senitherWeight = 0d;
    Double averageSkill = 0d;
    String discord = "";
    boolean apiOff = false;
    @SubscribeEvent
    public void onGuiClose(GuiContainerEvent.CloseWindowEvent event) {
        gotNetworth = false;
        tryingNetworth = false;
        networth = 0d;
        senitherWeight = 0d;
        averageSkill = 0d;
        discord = "";
        apiOff = false;
    }

    @SubscribeEvent
    public void onDrawContainerTitle(TitleDrawnEvent event) {
        try {
            

        if (event.gui !=null && event.gui instanceof GuiChest && SkyblockFeatures.config.extraProfileInfo) {
            GuiChest gui = (GuiChest) event.gui;
            ContainerChest chest = (ContainerChest) gui.inventorySlots;
            IInventory inv = chest.getLowerChestInventory();
            String chestName = inv.getDisplayName().getUnformattedText().trim();
            List<String> lines = new ArrayList<>();
            if(!chestName.contains("Profile") || chestName.contains("Management")) return;

            if(!gotNetworth && !tryingNetworth) {
                tryingNetworth = true;
                new Thread(() -> {
                    try {
                        
                    
                    String key = SkyblockFeatures.config.apiKey;
                    if (key.equals("")) return;
                    
                    // Get UUID for Hypixel API requests
                    String username = chestName.substring(0, chestName.indexOf("'"));
                    String uuid = APIUtils.getUUID(username);
                    // Find stats of latest profile
                    String latestProfile = APIUtils.getLatestProfileID(uuid, key);
                    if (latestProfile == null) {
                        apiOff = true;
                        return;
                    };
                    
                    String profileURL = "https://sky.shiiyu.moe/api/v2/profile/"+username+"#extraProfileInfo";
                    JsonObject profileResponse = APIUtils.getJSONResponse(profileURL);
                    try {
                        profileResponse = profileResponse.get("profiles").getAsJsonObject();
                        JsonObject a = profileResponse.get(latestProfile).getAsJsonObject().get("data").getAsJsonObject();
                        networth = a.getAsJsonObject().get("networth").getAsJsonObject().get("networth").getAsDouble();
                        senitherWeight = a.getAsJsonObject().get("weight").getAsJsonObject().get("senither").getAsJsonObject().get("overall").getAsDouble();
                        averageSkill = Math.floor(a.getAsJsonObject().get("average_level").getAsDouble());
                        JsonObject social = a.getAsJsonObject().get("social").getAsJsonObject();
                        if(social.has("DISCORD")) discord=social.get("DISCORD").getAsString();
                        else discord="None";
                        gotNetworth = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        apiOff = true;
                    }} catch (Exception e) {
                        // TODO: handle exception
                    }
                }).start();
            }

            if(gotNetworth) {
                lines.add(ChatFormatting.WHITE+"Networth: "+ChatFormatting.GOLD+Utils.nf.format(Math.floor(networth)));
                lines.add(ChatFormatting.WHITE+"Skill Avg: "+ChatFormatting.GOLD+averageSkill);
                lines.add(ChatFormatting.WHITE+"Senither Weight: "+ChatFormatting.GOLD+Utils.nf.format(senitherWeight));
                lines.add(ChatFormatting.WHITE+"Discord: "+ChatFormatting.BLUE+discord);
            } else if(!apiOff) {
                lines.add(ChatFormatting.WHITE+"Networth: "+ChatFormatting.RED+"Loading.");
                lines.add(ChatFormatting.WHITE+"Skill Avg: "+ChatFormatting.RED+"Loading..");
                lines.add(ChatFormatting.WHITE+"Senither Weight: "+ChatFormatting.RED+"Loading..");
                lines.add(ChatFormatting.WHITE+"Discord: "+ChatFormatting.RED+"Loading...");
            } else if(SkyblockFeatures.config.apiKey.equals("")){
                lines.add(ChatFormatting.RED+"API key not set.");
            } else {
                lines.add(ChatFormatting.RED+"Player has API Disabled.");
            }
            Utils.drawGraySquareWithBorder(180, 0, 150, (int) ((3+lines.size())*Utils.GetMC().fontRendererObj.FONT_HEIGHT)-8,3);

            for(int i=0;i<lines.size();i++) {
                Utils.GetMC().fontRendererObj.drawStringWithShadow(lines.get(i), 190, i*(Utils.GetMC().fontRendererObj.FONT_HEIGHT+1)+10, -1);
            }
        }
    } catch (Exception e) {
        // TODO: handle exception
    }
    }

}
