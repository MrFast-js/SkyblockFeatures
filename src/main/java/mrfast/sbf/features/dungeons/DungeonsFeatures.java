package mrfast.sbf.features.dungeons;

import java.awt.Color;
import java.util.*;

import mrfast.sbf.core.SkyblockInfo;
import mrfast.sbf.core.SkyblockMobDetector;
import mrfast.sbf.events.CheckRenderEntityEvent;
import mrfast.sbf.events.RenderEntityOutlineEvent;
import mrfast.sbf.utils.*;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.events.PacketEvent;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class DungeonsFeatures {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static Entity livid = null;
    public static boolean dungeonStarted = false;
    int ticks = 0;
    boolean inSpecialRoom = false;
    static HashMap<SkyblockMobDetector.SkyblockMob, Boolean> starredMobs = new HashMap<>();

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!Utils.inDungeons || !dungeonStarted) return;
        if (!SkyblockFeatures.config.glowingStarredMobs && !SkyblockFeatures.config.hideNonStarredMobs) return;

        ticks++;
        if (ticks % 20 == 0) {
            ticks = 0;
            int id = getDungeonRoomId();
            inSpecialRoom = id == 138 || id == 210 || id == -96 || id == -60;
            starredMobs.entrySet().removeIf((sbMob) -> !sbMob.getKey().getSkyblockMob().isEntityAlive());
            for (SkyblockMobDetector.SkyblockMob sbMob : SkyblockMobDetector.getLoadedSkyblockMobs()) {
                if (sbMob.skyblockMob.isInvisible()) continue;
                starredMobs.put(sbMob, sbMob.mobNameEntity.getDisplayName().getUnformattedText().contains("✯"));
            }
        }
    }

    @SubscribeEvent
    public void onRenderEntityOutlines(RenderEntityOutlineEvent event) {
        if (Utils.GetMC().theWorld == null || !Utils.inDungeons || SkyblockInfo.getLocation() == null) return;
        if (event.type == RenderEntityOutlineEvent.Type.XRAY) return;
        if (!SkyblockFeatures.config.glowingStarredMobs) return;

        if (dungeonStarted && !inSpecialRoom) {
            starredMobs.forEach((sbMob, starred) -> {
                if (starred) {
                    event.queueEntityToOutline(sbMob.skyblockMob, SkyblockFeatures.config.boxStarredMobsColor);
                }
            });
        }
    }

    @SubscribeEvent
    public void checkRender(CheckRenderEntityEvent event) {
        if (!SkyblockFeatures.config.hideNonStarredMobs) return;

        if (Utils.inDungeons && dungeonStarted && !inSpecialRoom) {
            SkyblockMobDetector.SkyblockMob sbMob = SkyblockMobDetector.getSkyblockMob(event.entity);
            if (sbMob == null || !starredMobs.containsKey(sbMob)) return;
            if (!starredMobs.get(sbMob)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onRender3D(RenderWorldLastEvent event) {
        if (!Utils.inDungeons) return;

        if (SkyblockFeatures.config.highlightBats) {
            for (Entity entity : mc.theWorld.loadedEntityList) {
                if (entity instanceof EntityBat && !entity.isInvisible()) {
                    RenderUtil.drawOutlinedFilledBoundingBox(entity.getEntityBoundingBox(), SkyblockFeatures.config.highlightBatColor, event.partialTicks);
                }
            }
        }

        if (mc.theWorld != null && SkyblockFeatures.config.highlightDoors) {
            for (TileEntity entity : mc.theWorld.loadedTileEntityList) {
                if (entity instanceof TileEntitySkull) {
                    TileEntitySkull skull = (TileEntitySkull) entity;
                    BlockPos pos = entity.getPos();
                    if (Utils.GetMC().thePlayer.getDistanceSq(pos) > 30 * 30) continue;
                    NBTTagCompound entityData = new NBTTagCompound();
                    skull.writeToNBT(entityData);
                    boolean witherSkull = isWitherSkull(entityData);
                    boolean bloodSkull = isBloodSkull(entityData);
                    if (!witherSkull && !bloodSkull) continue;

                    for (TileEntity secondEntity : mc.theWorld.loadedTileEntityList) {
                        if (secondEntity instanceof TileEntitySkull && secondEntity.getPos().equals(pos.add(4, 0, 4))) {
                            Color color = null;

                            if (witherSkull && isWitherSkull(secondEntity.serializeNBT())) {
                                color = Color.black;
                            }
                            if (bloodSkull && isBloodSkull(secondEntity.serializeNBT())) {
                                color = new Color(255, 65, 65);
                            }
                            if (color != null) {
                                GlStateManager.disableDepth();
                                AxisAlignedBB aabb = new AxisAlignedBB(pos.getX() + 1, pos.getY() - 1, pos.getZ() + 1, pos.getX() + 1 + 3, pos.getY() - 1 + 4, pos.getZ() + 1 + 3);
                                RenderUtil.drawOutlinedFilledBoundingBox(aabb, color, event.partialTicks);
                                GlStateManager.enableDepth();
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isBloodSkull(NBTTagCompound tag) {
        return tag.toString().contains("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWQ5ZDgwYjc5NDQyY2YxYTNhZmVhYTIzN2JkNmFkYWFhY2FiMGMyODgzMGZiMzZiNTcwNGNmNGQ5ZjU5MzdjNCJ9fX0");
    }

    private boolean isWitherSkull(NBTTagCompound tag) {
        return tag.toString().contains("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2JjYmJmOTRkNjAzNzQzYTFlNzE0NzAyNmUxYzEyNDBiZDk4ZmU4N2NjNGVmMDRkY2FiNTFhMzFjMzA5MTRmZCJ9fX0");
    }

    @SubscribeEvent
    public void onPacket(PacketEvent.ReceiveEvent event) {
        if (event.packet instanceof S29PacketSoundEffect && bloodguy != null && Utils.inDungeons && SkyblockFeatures.config.stopBloodMusic) {
            S29PacketSoundEffect packet = (S29PacketSoundEffect) event.packet;
            if (packet.getSoundName().contains("note")) event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onWorldChanges(WorldEvent.Load event) {
        count = 0;
        bloodguy = null;
        blessings.clear();
        dungeonStarted = false;
        livid = null;
    }

    String delimiter = EnumChatFormatting.AQUA + EnumChatFormatting.STRIKETHROUGH.toString() + EnumChatFormatting.BOLD + "--------------------------------------";
    int count = 0;
    public static String bloodguy;
    static Map<String, Integer> blessings = new HashMap<String, Integer>();

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onChatMesaage(ClientChatReceivedEvent event) {
        if (!Utils.inDungeons || event.type == 2) return;
        String text = event.message.getUnformattedText();
        for (String line : ScoreboardUtil.getSidebarLines()) {
            if (line.startsWith("Keys: ")) {
                dungeonStarted = true;
                break;
            }
        }
        if (text.startsWith("[BOSS] ") && !text.contains("The Watcher")) {
            dungeonStarted = false;
        }

        if (text.endsWith("has obtained Blood Key!")) {
            for (String entry : Nametags.playersAndClass.keySet()) {
                if (text.contains(entry)) {
                    bloodguy = entry;
                }
            }
            if (bloodguy == null) {
                bloodguy = Utils.GetMC().thePlayer.getName();
            }
        }

        if (!SkyblockFeatures.config.quickStart) return;

        if (text.contains("§6> §e§lEXTRA STATS §6<")) {
            count = 1;
        }
        if (text.equals("▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")) {
            if (count == 1) {
                ChatComponentText message = new ChatComponentText(EnumChatFormatting.AQUA + "[SBF] " + EnumChatFormatting.GOLD + "Dungeon finished! ");
                ChatComponentText warpout = new ChatComponentText(EnumChatFormatting.GREEN + "" + EnumChatFormatting.BOLD + " [WARP-OUT]  ");
                ChatComponentText frag = new ChatComponentText(EnumChatFormatting.GREEN + "" + EnumChatFormatting.BOLD + "[REPARTY]");

                frag.setChatStyle(frag.getChatStyle()
                        .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/rp"))
                        .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.GREEN + "Reparty Group"))));

                warpout.setChatStyle(warpout.getChatStyle()
                        .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp dungeon_hub"))
                        .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.GREEN + "Warp out of the dungeon"))));

                Utils.GetMC().thePlayer.addChatMessage(new ChatComponentText(ChatFormatting.GREEN + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"));

                Utils.GetMC().thePlayer.addChatMessage(
                        new ChatComponentText(delimiter)
                                .appendText("\n")
                                .appendSibling(message)
                                .appendSibling(warpout)
                                .appendSibling(frag)
                                .appendText("\n")
                                .appendSibling(new ChatComponentText(delimiter))
                );

                count = 0;
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onKeyInput(GuiScreenEvent.KeyboardInputEvent keyboardInputEvent) {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (!SkyblockFeatures.config.quickCloseChest || !Utils.inDungeons) return;

        if (screen instanceof GuiChest) {
            ContainerChest ch = (ContainerChest) ((GuiChest) screen).inventorySlots;
            if (!("Large Chest".equals(ch.getLowerChestInventory().getName()) || "Chest".equals(ch.getLowerChestInventory().getName())))
                return;

            Minecraft.getMinecraft().thePlayer.closeScreen();
        }
    }

    @SubscribeEvent
    public void onMouseInput(GuiScreenEvent.MouseInputEvent.Pre mouseInputEvent) {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (!SkyblockFeatures.config.quickCloseChest || !Utils.inDungeons) return;
        if (Mouse.getEventButton() == -1) return;

        if (screen instanceof GuiChest) {
            ContainerChest ch = (ContainerChest) ((GuiChest) screen).inventorySlots;
            if (!("Large Chest".equals(ch.getLowerChestInventory().getName()) || "Chest".equals(ch.getLowerChestInventory().getName())))
                return;

            Minecraft.getMinecraft().thePlayer.closeScreen();
        }
    }

    public static int getDungeonRoomId() {
        int output = 0;
        try {
            if (!Utils.inDungeons) return 0;
            String line = ScoreboardUtil.getSidebarLines(true).get(1);
            String roomInfo = line.split(" ")[2];
            String roomIdString = roomInfo.split(",")[0];
            output = Integer.parseInt(roomIdString);
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return output;
    }
}
