package mrfast.sbf.features.misc;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonObject;
import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.SkyblockInfo;
import mrfast.sbf.events.CheckRenderEntityEvent;
import mrfast.sbf.events.GuiContainerEvent;
import mrfast.sbf.events.PacketEvent;
import mrfast.sbf.events.GuiContainerEvent.TitleDrawnEvent;
import mrfast.sbf.utils.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.util.*;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MiscFeatures {
    public static HashMap<Entity, EntityVillager> tracker = new HashMap<Entity, EntityVillager>();

    @SubscribeEvent
    public void onWorldChanges(WorldEvent.Load event) {
        tracker.clear();
    }

    @SubscribeEvent
    public void onCheckRender(CheckRenderEntityEvent event) {
        if (!Utils.inSkyblock) return;
        if (SkyblockFeatures.config.hideArrows && event.entity instanceof EntityArrow) {
            event.setCanceled(true);
        }
        if (SkyblockFeatures.config.hidePlayersNearNPC) {
            if(Utils.isNPC(event.entity)) return;
            for (Entity entity : Utils.GetMC().theWorld.loadedEntityList) {
                if (Utils.isNPC(entity) && event.entity instanceof EntityPlayer && entity.getDistanceToEntity(event.entity) < 3 && entity != event.entity) {
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
        if (event.packet instanceof S2APacketParticles && SkyblockFeatures.config.highlightMushrooms) {
            S2APacketParticles packet = (S2APacketParticles) event.packet;
            EnumParticleTypes type = packet.getParticleType();
            Vec3 pos = new Vec3(Math.floor(packet.getXCoordinate()), Math.floor(packet.getYCoordinate()), Math.floor(packet.getZCoordinate()));
            boolean dupe = false;
            for (Vec3 part : particles) {
                if (part.distanceTo(pos) < 1 || part == pos) {
                    dupe = true;
                }
            }

            if (!dupe && type == EnumParticleTypes.SPELL_MOB && SkyblockInfo.localLocation.contains("Glowing")) {
                particles.add(pos);
            }
        }
    }

    @SubscribeEvent
    public void RenderBlockOverlayEvent(DrawBlockHighlightEvent event) {
        if (Utils.GetMC().thePlayer.getHeldItem() != null && SkyblockFeatures.config.teleportDestination) {
            ItemStack item = Utils.GetMC().thePlayer.getHeldItem();
            String id = ItemUtils.getSkyBlockItemID(item);
            if (id == null) return;
            if (id.contains("ASPECT_OF_THE_END") || id.contains("ASPECT_OF_THE_VOID")) {
                double distance = 8.0;
                double etherDistance = 8.0;
                boolean hasEtherwarp = false;
                for (String line : ItemUtils.getItemLore(item)) {
                    line = Utils.cleanColor(line);
                    if (line.contains("Teleport")) {
                        try {
                            distance = Double.parseDouble(line.replaceAll("[^0-9]", ""));
                        } catch (Exception e) {
                            // TODO: handle exception
                        }
                    }
                    if (line.contains("up to")) {
                        try {
                            etherDistance = Double.parseDouble(line.replaceAll("[^0-9]", ""));
                        } catch (Exception e) {
                            // TODO: handle exception
                        }
                    }
                    if (line.contains("Ether")) {
                        hasEtherwarp = true;
                    }
                }
                MovingObjectPosition lookingBlock = Utils.GetMC().thePlayer.rayTrace(distance, event.partialTicks);
                if (hasEtherwarp && Utils.GetMC().thePlayer.isSneaking()) {
                    lookingBlock = Utils.GetMC().thePlayer.rayTrace(etherDistance, event.partialTicks);
                }
                AxisAlignedBB box = new AxisAlignedBB(lookingBlock.getBlockPos(), lookingBlock.getBlockPos().add(1, 1, 1));

                if (!(Utils.GetMC().theWorld.getBlockState(lookingBlock.getBlockPos()).getBlock() instanceof BlockAir)) {
                    RenderUtil.drawOutlinedFilledBoundingBox(box, new Color(0x324ca8), event.partialTicks);
                }
            }
        }
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        if (SkyblockInfo.localLocation.contains("Glowing") && SkyblockFeatures.config.highlightMushrooms) {
            try {
                for (Vec3 packet : particles) {
                    Color color = SkyblockFeatures.config.highlightMushroomsColor;
                    highlightBlock(color, Math.floor(packet.xCoord), Math.floor(packet.yCoord), Math.floor(packet.zCoord), event.partialTicks);

                    Block block = Minecraft.getMinecraft().theWorld.getBlockState(new BlockPos(packet)).getBlock();
                    if (block != null && block == Blocks.air) {
                        particles.remove(packet);
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    public static void highlightBlock(Color c, double d, double d1, double d2, float ticks) {
        RenderUtil.drawOutlinedFilledBoundingBox(new AxisAlignedBB(d + 1.0D, d1 + 1, d2 + 1.0D, d, d1, d2), c, ticks);
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
        if (event.gui instanceof GuiChest && SkyblockFeatures.config.extraProfileInfo) {
            boolean hasProfileHead = event.gui.inventorySlots.getSlot(22).getHasStack();
            if (!hasProfileHead) return;
            ItemStack profileHead = event.gui.inventorySlots.getSlot(22).getStack();
            if (!(profileHead.getItem() instanceof ItemSkull) || !ItemUtils.getItemLore(profileHead).toString().contains("Oldest Profile:"))
                return;

            if (!gotNetworth && !tryingNetworth) {
                tryingNetworth = true;
                new Thread(() -> {
                    // Get UUID for Hypixel API requests
                    String username = Utils.cleanColor(profileHead.getDisplayName());
                    if (username.split(" ").length > 1) username = username.split(" ")[1];
                    String uuid = NetworkUtils.getUUID(username);
                    // Find stats of latest profile
                    String latestProfile = NetworkUtils.getLatestProfileID(uuid);
                    if (latestProfile == null) {
                        apiOff = true;
                        return;
                    }

                    String profileURL = "https://sky.shiiyu.moe/api/v2/profile/" + username + "#extraProfileInfo";
                    JsonObject profileResponse = NetworkUtils.getJSONResponse(profileURL);
                    try {
                        profileResponse = profileResponse.get("profiles").getAsJsonObject();
                        JsonObject a = profileResponse.get(latestProfile).getAsJsonObject().get("data").getAsJsonObject();
                        networth = a.getAsJsonObject().get("networth").getAsJsonObject().get("networth").getAsDouble();
                        senitherWeight = a.getAsJsonObject().get("weight").getAsJsonObject().get("senither").getAsJsonObject().get("overall").getAsDouble();
                        averageSkill = Math.floor(a.getAsJsonObject().get("average_level").getAsDouble());
                        JsonObject social = a.getAsJsonObject().get("social").getAsJsonObject();
                        if (social.has("DISCORD")) discord = social.get("DISCORD").getAsString();
                        else discord = "None";
                        gotNetworth = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        apiOff = true;
                    }
                }).start();
            }
            List<String> lines = new ArrayList<>();

            if (gotNetworth) {
                lines.add(ChatFormatting.WHITE + "Networth: " + ChatFormatting.GOLD + Utils.nf.format(Math.floor(networth)));
                lines.add(ChatFormatting.WHITE + "Skill Avg: " + ChatFormatting.GOLD + averageSkill);
                lines.add(ChatFormatting.WHITE + "Senither Weight: " + ChatFormatting.GOLD + Utils.nf.format(senitherWeight));
                lines.add(ChatFormatting.WHITE + "Discord: " + ChatFormatting.BLUE + discord);
            } else if (!apiOff) {
                lines.add(ChatFormatting.WHITE + "Networth: " + ChatFormatting.RED + "Loading.");
                lines.add(ChatFormatting.WHITE + "Skill Avg: " + ChatFormatting.RED + "Loading..");
                lines.add(ChatFormatting.WHITE + "Senither Weight: " + ChatFormatting.RED + "Loading..");
                lines.add(ChatFormatting.WHITE + "Discord: " + ChatFormatting.RED + "Loading...");
            } else {
                lines.add(ChatFormatting.RED + "Player has API Disabled.");
            }
            GuiUtils.drawSideMenu(lines, GuiUtils.TextStyle.DROP_SHADOW);
        }
    }

}
