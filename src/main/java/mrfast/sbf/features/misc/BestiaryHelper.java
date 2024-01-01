package mrfast.sbf.features.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import mrfast.sbf.core.ConfigManager;
import mrfast.sbf.core.SkyblockMobDetector;
import mrfast.sbf.events.GuiContainerEvent;
import mrfast.sbf.events.RenderEntityOutlineEvent;
import mrfast.sbf.events.SlotClickedEvent;
import mrfast.sbf.utils.ItemUtils;
import mrfast.sbf.utils.RenderUtil;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static mrfast.sbf.SkyblockFeatures.config;


public class BestiaryHelper {

    @SubscribeEvent
    public void onRenderEntityOutlines(RenderEntityOutlineEvent event) {
        if (!config.highlightBestiaryMobs) return;

        if (event.type == RenderEntityOutlineEvent.Type.XRAY) return;

        for (Entity entity : Utils.GetMC().theWorld.loadedEntityList) {
            SkyblockMobDetector.SkyblockMob sbMob = SkyblockMobDetector.getSkyblockMob(entity);
            if (sbMob == null) continue;
            // Glowing doesnt render on invisible entities
            if (sbMob.skyblockMob == entity && sbMob.getSkyblockMobId() != null && !sbMob.skyblockMob.isInvisible()) {
                if (!config.trackedBestiaryMobs.isEmpty()) {
                    if (isBeingTracked(sbMob.skyblockMobId)) {
                        event.queueEntityToOutline(sbMob.skyblockMob, config.highlightBestiaryColor);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        if (!config.highlightBestiaryMobs) return;

        for (Entity entity : Utils.GetMC().theWorld.loadedEntityList) {
            SkyblockMobDetector.SkyblockMob sbMob = SkyblockMobDetector.getSkyblockMob(entity);
            if (sbMob == null) continue;
            // Render outline box instead of glowing for invisible entities
            if (sbMob.skyblockMob == entity && sbMob.getSkyblockMobId() != null && sbMob.skyblockMob.isInvisible()) {
                if (!config.trackedBestiaryMobs.isEmpty()) {
                    if (isBeingTracked(sbMob.skyblockMobId)) {
                        RenderUtil.drawOutlinedFilledBoundingBox(sbMob.skyblockMob.getEntityBoundingBox(),config.highlightBestiaryColor,event.partialTicks);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onSlotDraw(GuiContainerEvent.DrawSlotEvent event) {
        if (!config.highlightBestiaryMobs) return;

        if (event.chestName.contains("➜")) {
            if (event.slot == null || event.slot.getStack() == null) return;
            String result = getMobName(event.slot.getStack());

            if (result == null) return;
            if (isBeingTracked(result)) {
                int x = event.slot.xDisplayPosition;
                int y = event.slot.yDisplayPosition;
                Gui.drawRect(x, y, x + 16, y + 16, config.highlightBestiaryColor.getRGB());
            }
        }
    }

    @SubscribeEvent
    public void onMouseInput(MouseEvent event) {
        if(!config.highlightBestiaryMobs || !config.highlightBestiaryMobsMidClick) return;

        if (event.button == 2 && event.buttonstate) { // 2 corresponds to the middle mouse button
            // Get the entity the player is looking at
            Entity targetEntity = getTargetEntity(4.0);

            if (targetEntity != null) {
                SkyblockMobDetector.SkyblockMob sbMob = SkyblockMobDetector.getSkyblockMob(targetEntity);
                if(sbMob!=null) {
                    if(sbMob.skyblockMobId==null) {
                        Utils.sendMessage(ChatFormatting.RED+"This mob could not be identified for the bestiary tracker!");
                    }
                }
                if(sbMob==null || sbMob.skyblockMobId==null) {
                    return;
                }
                List<String> mobs = Arrays.stream(config.trackedBestiaryMobs.split(", ")).collect(Collectors.toList());

                if (isBeingTracked(sbMob.skyblockMobId)) {
                    mobs.remove(sbMob.skyblockMobId);
                    Utils.playSound("random.orb", 0.1f);
                    Utils.sendMessage("§cRemoved " + sbMob.skyblockMobId + " from bestiary tracker!");
                } else {
                    mobs.add(sbMob.skyblockMobId);
                    Utils.playSound("random.orb", 1f);
                    Utils.sendMessage("§aAdded " + sbMob.skyblockMobId + " to the bestiary tracker!");
                }
                config.trackedBestiaryMobs = mobs.toString().replace("[", "").replace("]", "");
                ConfigManager.saveConfig();
            }
        }
    }



    private Entity getTargetEntity(double range) {
        EntityPlayer player = Utils.GetMC().thePlayer;
        Vec3 start = new Vec3(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        Vec3 look = player.getLookVec();
        Vec3 end = start.addVector(look.xCoord * range, look.yCoord * range, look.zCoord * range);

        Entity hitEntity = null;
        double closestDistance = range;

        for (Entity entity : Utils.GetMC().theWorld.loadedEntityList) {
            if (entity == Utils.GetMC().thePlayer) continue;
            if (entity.canBeCollidedWith()) {
                float collisionSize = entity.getCollisionBorderSize();
                AxisAlignedBB boundingBox = entity.getEntityBoundingBox().expand(collisionSize, collisionSize, collisionSize);
                MovingObjectPosition intercept = boundingBox.calculateIntercept(start, end);

                if (intercept != null) {
                    double distance = start.distanceTo(intercept.hitVec);

                    if (distance < closestDistance) {
                        closestDistance = distance;
                        hitEntity = entity;
                    }
                }
            }
        }

        return hitEntity;
    }

    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event) {
        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChest) || !config.highlightBestiaryMobs) return;

        GuiChest chest = (GuiChest) Minecraft.getMinecraft().currentScreen;
        ContainerChest cont = (ContainerChest) chest.inventorySlots;
        String chestName = cont.getLowerChestInventory().getName();

        if (chestName.contains("➜")) {
            if (getMobName(event.itemStack) != null) {
                if (isBeingTracked(getMobName(event.itemStack))) {
                    event.toolTip.add(1, ChatFormatting.YELLOW + " " + ChatFormatting.BOLD + "This Mob is being tracked!");
                    event.toolTip.add(2, ChatFormatting.RED + " " + ChatFormatting.BOLD + "CTRL+CLICK To Stop Tracking This Mob!");
                } else {
                    event.toolTip.add(1, ChatFormatting.LIGHT_PURPLE + " " + ChatFormatting.BOLD + "CTRL+CLICK To Track This Mob!");
                }
            }
        }
    }

    @SubscribeEvent
    public void onSlotClick(SlotClickedEvent event) {
        if (!config.highlightBestiaryMobs) return;

        if (event.chestName.contains("➜") && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
            if (event.slot == null || event.slot.getStack() == null) return;
            String result = getMobName(event.slot.getStack());

            List<String> mobs = Arrays.stream(config.trackedBestiaryMobs.split(", ")).collect(Collectors.toList());

            if (result == null) return;
            if (isBeingTracked(result)) {
                mobs.remove(result);

                Utils.playSound("random.orb", 0.1f);
                Utils.sendMessage("§cRemoved " + result + " from bestiary tracker!");
            } else {
                mobs.add(result);

                Utils.playSound("random.orb", 1f);
                Utils.sendMessage("§aAdded " + result + " to the bestiary tracker!");
            }
            config.trackedBestiaryMobs = mobs.toString().replace("[", "").replace("]", "");

            event.setCanceled(true);
            ConfigManager.saveConfig();
        }
    }

    public boolean isBeingTracked(String mob) {
        List<String> mobs = Arrays.stream(config.trackedBestiaryMobs.split(", ")).collect(Collectors.toList());
        return mobs.contains(mob);
    }

    public String getMobName(ItemStack stack) {
        String regex = "^([^\\d]+?)(?:\\s+[IVXLCDM]+)?$";

        Pattern pattern = Pattern.compile(regex);
        String cleanName = Utils.cleanColor(stack.getDisplayName());
        Matcher matcher = pattern.matcher(cleanName);
        if (matcher.find()) {
            if (ItemUtils.getItemLore(stack).toString().contains("Kills: ")) {
                return matcher.group(1);
            }
        }
        return null;
    }
}
