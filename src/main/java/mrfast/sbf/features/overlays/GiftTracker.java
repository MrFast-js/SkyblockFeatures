package mrfast.sbf.features.overlays;

import com.mojang.realmsclient.gui.ChatFormatting;
import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.ConfigManager;
import mrfast.sbf.events.CheckRenderEntityEvent;
import mrfast.sbf.events.GuiContainerEvent;
import mrfast.sbf.events.PacketEvent;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.GuiUtils;
import mrfast.sbf.utils.ItemUtils;
import mrfast.sbf.utils.RenderUtil;
import mrfast.sbf.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockIce;
import net.minecraft.block.BlockPackedIce;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GiftTracker {
    public static ArrayList<Entity> saintJerryGifts = new ArrayList<Entity>();

    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        if (event.target instanceof EntityArmorStand && ((EntityArmorStand) event.target).getCurrentArmor(3) != null && ((EntityArmorStand) event.target).getCurrentArmor(3).serializeNBT().getCompoundTag("tag").getCompoundTag("SkullOwner").getCompoundTag("Properties").toString().contains("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTBmNTM5ODUxMGIxYTA1YWZjNWIyMDFlYWQ4YmZjNTgzZTU3ZDcyMDJmNTE5M2IwYjc2MWZjYmQwYWUyIn19fQ=") && !saintJerryGifts.contains(event.target)) {
            // If this is false its a player gift
            if (event.target.lastTickPosX == event.target.posX && event.target.lastTickPosY == event.target.posY && event.target.lastTickPosZ == event.target.posZ) {
                saintJerryGifts.add(event.target);
            }
        }
    }

    private static class Gift {
        Boolean giftToSelf = false;
        Entity entity;
        Entity toEntity;
        Entity fromEntity;
    }

    private static final HashMap<Entity, Gift> gifts = new HashMap<>();

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        gifts.clear();
        // Reset unique gifts given next time the event comes next year
        if (SkyblockFeatures.config.winterYear == 0) {
            SkyblockFeatures.config.winterYear = Year.now().getValue();
        } else {
            if (Year.now().getValue() != SkyblockFeatures.config.winterYear) {
                SkyblockFeatures.config.uniqueGiftsGiven = 0;
                SkyblockFeatures.config.winterYear = Year.now().getValue();
            }
        }
    }

    @SubscribeEvent
    public void onRenderEntity(CheckRenderEntityEvent event) {
        if (!SkyblockFeatures.config.hideOtherGifts) return;

        if (event.entity instanceof EntityArmorStand) {
            for (Gift gift : gifts.values()) {
                if (gift.fromEntity.getCustomNameTag().contains(Utils.GetMC().thePlayer.getName())) continue;
                boolean isRecipient = gift.toEntity.getUniqueID().equals(event.entity.getUniqueID());
                boolean isFrom = gift.fromEntity.getUniqueID().equals(event.entity.getUniqueID());
                boolean isGift = gift.entity.getUniqueID().equals(event.entity.getUniqueID());

                if ((isFrom || isRecipient || isGift) && !gift.giftToSelf) {
                    event.setCanceled(true);
                    return;
                }
            }
        }
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (!SkyblockFeatures.config.showGiftingInfo) return;

        String clean = Utils.cleanColor(event.message.getUnformattedText());
        if (clean.startsWith("+1 Unique Gift given!")) {
            SkyblockFeatures.config.uniqueGiftsGiven++;
            ConfigManager.saveConfig(SkyblockFeatures.config);
        }
    }

    @SubscribeEvent
    public void onDrawTitle(GuiContainerEvent.TitleDrawnEvent event) {
        if (!SkyblockFeatures.config.showGiftingInfo) return;

        if (event.displayName.equals("Generow")) {
            ItemStack giftStack = event.container.getSlot(40).getStack();
            if (giftStack != null) {
                for (String line : ItemUtils.getItemLore(giftStack)) {
                    line = Utils.cleanColor(line);
                    if (line.startsWith("Unique Players Gifted:")) {
                        int gifts = Integer.parseInt(line.replaceAll("[^0-9]", ""));
                        SkyblockFeatures.config.uniqueGiftsGiven = gifts;
                        ConfigManager.saveConfig(SkyblockFeatures.config);
                        break;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onReceivePacket(PacketEvent.ReceiveEvent event) {
        if (!Utils.inSkyblock || Utils.GetMC().theWorld == null || !SkyblockFeatures.config.hideGiftParticles) return;

        if (event.packet instanceof S2APacketParticles) {
            double x = ((S2APacketParticles) event.packet).getXCoordinate();
            double y = ((S2APacketParticles) event.packet).getYCoordinate();
            double z = ((S2APacketParticles) event.packet).getZCoordinate();
            for (Gift gift : gifts.values()) {
                if (gift.entity.getPositionVector().distanceTo(new Vec3(x, y, z)) < 3) {
                    event.setCanceled(true);
                    break;
                }
            }
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (Utils.GetMC().theWorld == null) return;
        if (!(SkyblockFeatures.config.highlightSelfGifts || SkyblockFeatures.config.hideOtherGifts || SkyblockFeatures.config.hideGiftParticles))
            return;

        for (Map.Entry<Entity, Gift> entry : new HashMap<>(gifts).entrySet()) {
            if (!entry.getKey().isEntityAlive()) {
                gifts.remove(entry.getKey());
            }
        }

        for (Entity entity : Utils.GetMC().theWorld.loadedEntityList) {
            if (!(entity instanceof EntityArmorStand)) continue;
            if (isPlayerPresent((EntityArmorStand) entity)) {
                Gift gift = new Gift();
                gift.entity = entity;
                gift.toEntity = Utils.GetMC().theWorld.getEntityByID(entity.getEntityId() + 1);
                gift.fromEntity = Utils.GetMC().theWorld.getEntityByID(entity.getEntityId() + 2);
                if (gift.toEntity == null || gift.fromEntity == null) continue;
                if (!gift.toEntity.hasCustomName() || !gift.fromEntity.hasCustomName()) continue;

                if (gift.toEntity.getCustomNameTag().contains("CLICK TO OPEN")) {
                    gift.giftToSelf = true;
                }
                if (!gift.toEntity.getCustomNameTag().contains("To:") || !gift.fromEntity.getCustomNameTag().contains("From:")) {
                    if (!gift.giftToSelf) continue;
                }

                if (gift.toEntity.getDistanceToEntity(gift.entity) > 1 || gift.fromEntity.getDistanceToEntity(gift.entity) > 1 || gift.entity.getDistanceToEntity(Utils.GetMC().thePlayer) > 30) {
                    continue;
                }

                gifts.put(entity, gift);
            }
        }
    }

    public boolean isPlayerPresent(EntityArmorStand entity) {
        if (gifts.containsKey(entity)) return true;
        if (entity.getCurrentArmor(3) == null) return false;
        if (ItemUtils.getSkyBlockItemID(entity.getCurrentArmor(3)) != null) {
            return ItemUtils.getSkyBlockItemID(entity.getCurrentArmor(3)).contains("_GIFT");
        }
        boolean isMoving = (entity.lastTickPosX != entity.posX || entity.lastTickPosY != entity.posY || entity.lastTickPosZ != entity.posZ || entity.prevRotationPitch != entity.rotationPitch || entity.prevRotationYaw != entity.rotationYaw);
        return isMoving;
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || Utils.inDungeons || !Utils.inSkyblock) return;
        AxisAlignedBB glacialCaveBounds = new AxisAlignedBB(105, 72, 113, 33, 85, 19);
        boolean inGlacialCave = glacialCaveBounds.isVecInside(Utils.GetMC().thePlayer.getPositionVector());

        if (SkyblockFeatures.config.highlightSelfGifts) {
            for (Gift gift : gifts.values()) {
                if (!gift.giftToSelf) continue;
                highlightBlock(SkyblockFeatures.config.selfGiftHighlightColor, gift.entity.posX - 0.5, gift.entity.posY + 1.5, gift.entity.posZ - 0.5, 1.0D, event.partialTicks);
            }
        }

        if (!(SkyblockFeatures.config.icecaveHighlight || SkyblockFeatures.config.presentWaypoints)) return;

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (SkyblockFeatures.config.presentWaypoints && entity instanceof EntityArmorStand && !inGlacialCave && ((EntityArmorStand) entity).getCurrentArmor(3) != null && ((EntityArmorStand) entity).getCurrentArmor(3).serializeNBT().getCompoundTag("tag").getCompoundTag("SkullOwner").getCompoundTag("Properties").toString().contains("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTBmNTM5ODUxMGIxYTA1YWZjNWIyMDFlYWQ4YmZjNTgzZTU3ZDcyMDJmNTE5M2IwYjc2MWZjYmQwYWUyIn19fQ=")) {
                boolean isPlayerGift = false;
                for (Entity otherEntity : mc.theWorld.loadedEntityList) {
                    if (otherEntity instanceof EntityArmorStand && otherEntity.getDistanceToEntity(entity) < 0.5 && otherEntity.getName().contains("From: ")) {
                        isPlayerGift = true;
                    }
                }
                if (isPlayerPresent((EntityArmorStand) entity)) {
                    isPlayerGift = true;
                }
                if (!saintJerryGifts.contains(entity) && !isPlayerGift) {
                    GlStateManager.disableDepth();
                    highlightBlock(SkyblockFeatures.config.presentWaypointsColor, entity.posX - 0.5, entity.posY + 1.5, entity.posZ - 0.5, 1.0D, event.partialTicks);
                }
            }
            if (inGlacialCave && SkyblockFeatures.config.icecaveHighlight) {
                if(SkyblockFeatures.config.icecaveHighlightWalls) {
                    GlStateManager.disableDepth();
                }
                Block blockState = mc.theWorld.getBlockState(entity.getPosition().up()).getBlock();
                if (SkyblockFeatures.config.icecaveHighlight && (blockState instanceof BlockIce || blockState instanceof BlockPackedIce) && entity instanceof EntityArmorStand && ((EntityArmorStand) entity).getCurrentArmor(3) != null) {
                    String itemName = ((EntityArmorStand) entity).getCurrentArmor(3).serializeNBT().getCompoundTag("tag").getCompoundTag("display").getString("Name");
                    Vec3 StringPos = new Vec3(entity.posX, entity.posY + 3, entity.posZ);
                    BlockPos pos = new BlockPos(entity.posX, entity.posY + 2, entity.posZ);

                    // White gift
                    if (itemName.contains("White Gift")) {
                        highlightBlock(pos, Color.white, event.partialTicks);
                        RenderUtil.draw3DString(StringPos, ChatFormatting.WHITE + "White Gift", event.partialTicks);
                    }
                    // Green Gift
                    else if (itemName.contains("Green Gift")) {
                        highlightBlock(pos, Color.green, event.partialTicks);
                        RenderUtil.draw3DString(StringPos, ChatFormatting.GREEN + "Green Gift", event.partialTicks);
                    }
                    // Red Gift
                    else if (itemName.contains("Red Gift")) {
                        highlightBlock(pos, Color.red, event.partialTicks);
                        RenderUtil.draw3DString(StringPos, ChatFormatting.RED + "Red Gift", event.partialTicks);
                    }
                    // Glacial Talisman
                    else if (itemName.contains("Talisman")) {
                        highlightBlock(pos, Color.orange, event.partialTicks);
                        RenderUtil.draw3DString(StringPos, ChatFormatting.GOLD + "Talisman", event.partialTicks);
                    }
                    // Glacial Frag
                    else if (itemName.contains("Fragment")) {
                        highlightBlock(pos, Color.magenta, event.partialTicks);
                        RenderUtil.draw3DString(StringPos, ChatFormatting.LIGHT_PURPLE + "Frag", event.partialTicks);
                    }
                    // Packed Ice
                    else if (itemName.contains("Enchanted Ice")) {
                        highlightBlock(pos, new Color(0x0a0d61), event.partialTicks);
                        RenderUtil.draw3DString(StringPos, ChatFormatting.DARK_BLUE + "E. Ice", event.partialTicks);
                    }
                    // Enchanted Packed Ice
                    else if (itemName.contains("Enchanted Packed Ice")) {
                        highlightBlock(pos, new Color(0x5317eb), event.partialTicks);
                        RenderUtil.draw3DString(StringPos, ChatFormatting.DARK_BLUE + "E. Packed Ice", event.partialTicks);
                    }
                    // Enchanted Packed Ice
                    else if (itemName.contains("Glowy Chum Bait")) {
                        highlightBlock(pos, new Color(0x44ad86), event.partialTicks);
                        RenderUtil.draw3DString(StringPos, ChatFormatting.DARK_AQUA + "Glowy Chum Bait", event.partialTicks);
                    }
                    // Einary' Red Hoodie
                    else if (itemName.contains("Einary's Red Hoodie")) {
                        highlightBlock(pos, new Color(0x9c0000), event.partialTicks);
                        RenderUtil.draw3DString(StringPos, ChatFormatting.DARK_RED + "Einary's Red Hoodie", event.partialTicks);
                    }
                    // Highlight everything else gray
                    else {
                        highlightBlock(pos, Color.lightGray, event.partialTicks);
                        RenderUtil.draw3DString(StringPos, itemName, event.partialTicks);
                    }
                }
                GlStateManager.enableDepth();
            }
        }
    }

    public static void highlightBlock(BlockPos pos, Color color, float partialTicks) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        RenderUtil.drawOutlinedFilledBoundingBox(new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1), color, partialTicks);

    }

    public static void highlightBlock(Color c, double d, double d1, double d2, double size, float ticks) {
        RenderUtil.drawOutlinedFilledBoundingBox(new AxisAlignedBB(d, d1, d2, d + size, d1 + size, d2 + size), c, ticks);
    }

    static {
        new GiftingOverlay();
    }

    public static int getGiftMilestone(int gifts) {
        if (gifts <= 100) {
            return (gifts / 10);
        } else if (gifts <= 200) {
            gifts -= 100;
            return 10 + (gifts / 20);
        } else if (gifts <= 350) {
            gifts -= 200;
            return 15 + (gifts / 30);
        } else {
            gifts -= 350;
            return 20 + (gifts / 50);
        }
    }

    public static class GiftingOverlay extends UIElement {
        public GiftingOverlay() {
            super("giftingOverlay", new Point(0f, 0f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            int milestone = getGiftMilestone(SkyblockFeatures.config.uniqueGiftsGiven);
            String[] lines = {
                    "§e§lGifting Info",
                    " §f" + SkyblockFeatures.config.uniqueGiftsGiven + "§7/600 §6Unique Gifts ",
                    " §aMilestone §b" + milestone,
            };
            GuiUtils.drawTextLines(Arrays.asList(lines), 0, 0, GuiUtils.TextStyle.DROP_SHADOW);
        }

        @Override
        public void drawElementExample() {
            int milestone = getGiftMilestone(SkyblockFeatures.config.uniqueGiftsGiven);
            String[] lines = {
                    "§e§lGifting Info",
                    " §f" + SkyblockFeatures.config.uniqueGiftsGiven + "§7/600 §6Unique Gifts",
                    " §aMilestone §b" + milestone,
            };
            GuiUtils.drawTextLines(Arrays.asList(lines), 0, 0, GuiUtils.TextStyle.DROP_SHADOW);
        }

        @Override
        public boolean getToggled() {
            return SkyblockFeatures.config.showGiftingInfo;
        }

        @Override
        public boolean getRequirement() {
            return Utils.inSkyblock;
        }

        @Override
        public int getHeight() {
            return (Utils.GetMC().fontRendererObj.FONT_HEIGHT + 1) * 3;
        }

        @Override
        public int getWidth() {
            return Utils.GetMC().fontRendererObj.getStringWidth("100/600 Unique Gifts");
        }
    }
}

