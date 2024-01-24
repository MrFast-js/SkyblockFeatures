package mrfast.sbf.features.events;

import java.awt.Color;
import java.util.*;
import java.util.stream.Collectors;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.SkyblockInfo;
import mrfast.sbf.events.PacketEvent.ReceiveEvent;
import mrfast.sbf.events.UseItemAbilityEvent;
import mrfast.sbf.utils.RenderUtil;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.util.*;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class MythologicalHelper {
    private static List<Burrow> burrows = new ArrayList<>();

    public static class Burrow {
        String type;
        BlockPos pos;

        public Burrow(BlockPos pos) {
            this.pos = pos;

            this.type = "Particle";
        }
    }

    private BlockPos prevBurrow = null;
    private boolean sendNotification = true;
    private static List<Vec3> lavaDripParticles = new ArrayList<>();
    private static Entity fishingHook = null;
    private S2APacketParticles geyser = null;
    private Vec3 startPos = null;
    private Vec3 endPos = null;

    @SubscribeEvent
    public void onReceivePacket(ReceiveEvent event) {
        if (!Utils.inSkyblock || Utils.GetMC().theWorld == null || !Utils.GetMC().inGameHasFocus) {
            return;
        }

        if (event.packet instanceof S2APacketParticles) {
            processParticles((S2APacketParticles) event.packet, event);
        }
    }

    private void processParticles(S2APacketParticles packet, ReceiveEvent event) {
        EnumParticleTypes type = packet.getParticleType();

        if (type == EnumParticleTypes.FOOTSTEP && SkyblockFeatures.config.MythologicalHelper) {
            handleBurrowParticles(packet);
        } else if (SkyblockInfo.localLocation.contains("Volcano")) {
            handleVolcanoParticles(packet, event);
        }

        handleAncestralParticles(packet);
    }

    private void handleBurrowParticles(S2APacketParticles packet) {
        boolean dupe = false;
        for (Burrow burrow : burrows) {
            if (burrow.pos.getX() == Math.floor(packet.getXCoordinate()) || burrow.pos.getZ() == Math.floor(packet.getZCoordinate())) {
                dupe = true;
            }
        }
        if (!dupe) {
            if (sendNotification) {
                sendNotification = false;
                Utils.sendMessage(ChatFormatting.GREEN + "Located a new Griffin Burrow!");
                Utils.playSound("random.orb", 0.1);
                Utils.setTimeout(() -> sendNotification = true, 15 * 1000);
            }
            BlockPos pos = new BlockPos(Math.floor(packet.getXCoordinate()), Math.floor(packet.getYCoordinate() - 1), Math.floor(packet.getZCoordinate()));
            Burrow burrow = new Burrow(pos);
            burrows.add(burrow);
        }
    }

    private void handleVolcanoParticles(S2APacketParticles packet, ReceiveEvent event) {
        if (packet.getParticleType() == EnumParticleTypes.CLOUD && SkyblockFeatures.config.geyserBoundingBox) {
            geyser = packet;
            fishingHook = findFishingHook();
        }

        if (fishingHook != null && SkyblockFeatures.config.hideGeyserParticles) {
            handleGeyserParticles(packet, event);
        }
    }

    private Entity findFishingHook() {
        for (Entity entity : Utils.GetMC().theWorld.loadedEntityList) {
            if (entity instanceof EntityFishHook && !(((EntityFishHook) entity).angler instanceof EntityOtherPlayerMP)) {
                return entity;
            }
        }
        return null;
    }

    private void handleGeyserParticles(S2APacketParticles packet, ReceiveEvent event) {
        if (packet.getParticleType() == EnumParticleTypes.SMOKE_NORMAL) {
            if (!(packet.getYCoordinate() > fishingHook.posY - 0.2 && packet.getYCoordinate() < fishingHook.posY + 0.2)) {
                event.setCanceled(true);
            }
        } else {
            event.setCanceled(true);
        }
    }

    boolean spoonSearching = false;

    @SubscribeEvent
    public void onItemAbility(UseItemAbilityEvent event) {
        if (event.ability.itemId.equals("ANCESTRAL_SPADE")) {
            spoonSearching = true;
            lavaDripParticles.clear();
            Utils.setTimeout(() -> {
                spoonSearching = false;
            }, 3000);
        }
    }

    private boolean fromGriffinPet(Vec3i pos) {
        for (Entity e : Utils.GetMC().theWorld.loadedEntityList) {
            if (e instanceof EntityArmorStand && e.getDistance(pos.getX(), pos.getY(), pos.getZ()) < 3) {
                if (e.isInvisible()) return true;
            }
        }
        return false;
    }

    private void handleAncestralParticles(S2APacketParticles packet) {
        if (SkyblockFeatures.config.MythologicalHelper) {
            if (packet.getParticleType() == EnumParticleTypes.DRIP_LAVA) {
                Vec3i pos = new Vec3i(packet.getXCoordinate(), packet.getYCoordinate(), packet.getZCoordinate());
                double dist = Utils.GetMC().thePlayer.getDistance(packet.getXCoordinate(), packet.getYCoordinate(), packet.getZCoordinate());

                Burrow closest = getClosestBurrow(pos);

                if (dist > 8 && !lavaDripParticles.contains(new Vec3(pos)) && spoonSearching) {
                    if (fromGriffinPet(pos)) return;

                    if (closest != null) {
                        if (Utils.GetMC().thePlayer.getDistance(closest.pos.getX(), closest.pos.getY(), closest.pos.getZ()) < 35) {
                            return;
                        }
                    }
                    lavaDripParticles.add(new Vec3(packet.getXCoordinate(), packet.getYCoordinate(), packet.getZCoordinate()));
                }

                if (closest != null) {
                    if (closest.pos.distanceSq(pos.getX(), pos.getY(), pos.getZ()) == 1) {
                        closest.type = "Treasure";
                    }
                }
            }
            if (packet.getParticleType() == EnumParticleTypes.CRIT) {
                Vec3i pos = new Vec3i(packet.getXCoordinate(), packet.getYCoordinate(), packet.getZCoordinate());

                Burrow closest = getClosestBurrow(pos);

                if (closest != null && closest.pos.distanceSq(pos) == 1) {
                    closest.type = "Mob";
                }
            }
        }
    }

    public static Burrow getClosestBurrow(Vec3i pos) {
        Burrow closest = null;
        for (Burrow burrow : burrows) {
            if (closest == null || burrow.pos.distanceSq(closest.pos) < closest.pos.distanceSq(pos)) {
                closest = burrow;
            }
        }
        return closest;
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (!Utils.inSkyblock || event.type == 2 || !SkyblockFeatures.config.MythologicalHelper) {
            return;
        }

        String unformatted = Utils.cleanColor(event.message.getUnformattedText());
        if (unformatted.matches("^(Wow!|Yikes!|Uh oh!|Oh!|Oi!|Danger!|Woah!|Good Grief!)\\s+You\\sdug\\sout.*") ||
                unformatted.startsWith("You dug out a Griffin Burrow") ||
                unformatted.startsWith("You finished the Griffin burrow chain")) {
            resetClosestBurrow();
        }
    }

    public void resetClosestBurrow() {
        endPos = null;
        startPos = null;
        lavaDripParticles.clear();
        predictedBurrowPosition = null;

        Utils.setTimeout(() -> {
            Comparator<Burrow> burrowComparator = Comparator.comparingDouble(burrow -> Utils.GetMC().thePlayer.getDistanceSq(burrow.pos));

            List<Burrow> sortedBurrows = new ArrayList<>(burrows);
            sortedBurrows.sort(burrowComparator);

            if (!sortedBurrows.isEmpty()) {
                Burrow closestBurrow = sortedBurrows.get(0);
                if (closestBurrow != null) {
                    prevBurrow = closestBurrow.pos;
                }
                sortedBurrows = sortedBurrows.stream().filter((a) -> Utils.GetMC().thePlayer.getDistanceSq(a.pos) > 25).collect(Collectors.toList());
                burrows = sortedBurrows;
            }
        }, 300);
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        renderGeyserBoundingBox(event.partialTicks);
        renderSpoonLine();
        renderBurrowBoundingBox(event.partialTicks);
        renderPointerArrowLine();
    }

    private void renderGeyserBoundingBox(float partialTicks) {
        if (geyser != null && SkyblockFeatures.config.geyserBoundingBox) {
            double x = geyser.getXCoordinate();
            double y = 118;
            double z = geyser.getZCoordinate();
            Color color = fishingHook != null ?
                    (fishingHook.getDistance(geyser.getXCoordinate(), fishingHook.posY, geyser.getZCoordinate()) <= 1 ?
                            new Color(85, 255, 85) : new Color(255, 85, 255)) :
                    new Color(255, 85, 255);
            RenderUtil.drawOutlinedFilledBoundingBox(new AxisAlignedBB(x - 1, y - 0.1, z - 1, x + 1, y - 0.09, z + 1), color, partialTicks);
        }
    }

    static Vec3 previousParticle = null;

    private void renderSpoonLine() {
        if (SkyblockFeatures.config.MythologicalHelper) {
            try {
                double index = 0;
                for (Vec3 lavaDrip : lavaDripParticles) {
                    index++;
                    if (previousParticle == null) {
                        previousParticle = lavaDrip;
                        continue;
                    }

                    GlStateManager.disableCull();
                    RenderUtil.draw3DArrowLine(previousParticle, lavaDrip, SkyblockFeatures.config.MythologicalHelperActualColor);
                    GlStateManager.enableCull();

                    double xLavaDripDif = previousParticle.xCoord - lavaDrip.xCoord;
                    double yLavaDripDif = previousParticle.yCoord - lavaDrip.yCoord;
                    double zLavaDripDif = previousParticle.zCoord - lavaDrip.zCoord;

                    // Draw prediction by repeating the same last translation 300 times
                    if (index == lavaDripParticles.size()) {
                        Vec3 bestPredictionPosition = null;
                        Vec3 bestSolidLinePosition = null;

                        for (int i = 0; i < 300; i++) {
                            Vec3 predictionPosition = lavaDrip.add(new Vec3(-xLavaDripDif * i, -yLavaDripDif * i, -zLavaDripDif * i));

                            if (endPos != null && startPos != null && SkyblockFeatures.config.MythologicalHelperPrediction) {
                                double xStartStopDif = (endPos.xCoord - startPos.xCoord) / 300d;
                                double zStartStopDif = (endPos.zCoord - startPos.zCoord) / 300d;

                                for (int j = 0; j < 500; j++) {
                                    Vec3 solidLinePoint = startPos.add(new Vec3(xStartStopDif * j, predictionPosition.yCoord, zStartStopDif * j));
                                    if (bestPredictionPosition == null) {
                                        bestPredictionPosition = predictionPosition;
                                        bestSolidLinePosition = solidLinePoint;
                                        continue;
                                    }
                                    if (predictionPosition.distanceTo(solidLinePoint) < bestPredictionPosition.distanceTo(bestSolidLinePosition)) {
                                        bestPredictionPosition = predictionPosition;
                                        bestSolidLinePosition = solidLinePoint;
                                    }
                                }
                            }

                            RenderUtil.draw3DArrowLine(previousParticle, predictionPosition, SkyblockFeatures.config.MythologicalHelperPredictionColor);
                            previousParticle = predictionPosition;
                        }
                        if (bestPredictionPosition != null) {
                            BlockPos height = Utils.GetMC().theWorld.getHeight(new BlockPos(bestPredictionPosition));
                            if (height.getY() < 10) height = height.up(71);

                            predictedBurrowPosition = height;
                        }
                        previousParticle = null;
                        return;
                    }

                    previousParticle = lavaDrip;
                }
            } catch (Exception e) {
                e.printStackTrace();
                // Handle exception
            }
        }
    }


    static BlockPos predictedBurrowPosition = null;

    private void renderBurrowBoundingBox(float partialTicks) {
        if (!Utils.inSkyblock || !SkyblockFeatures.config.MythologicalHelper) return;

        try {
            for (Burrow burrow : burrows) {
                String type = ChatFormatting.RED + "(" + burrow.type + ")";
                Color color = SkyblockFeatures.config.MythologicalHelperDefaultColor;
                if (Objects.equals(burrow.type, "Treasure")) {
                    color = SkyblockFeatures.config.MythologicalHelperTreasureColor;
                } else if (Objects.equals(burrow.type, "Mob")) {
                    color = SkyblockFeatures.config.MythologicalHelperMobColor;
                }

                RenderUtil.drawWaypoint(burrow.pos, color, ChatFormatting.GOLD + "Burrow " + type, partialTicks, true);
            }
            if (predictedBurrowPosition != null) {
                RenderUtil.drawWaypoint(predictedBurrowPosition, Color.cyan, ChatFormatting.YELLOW + "Predicted Location", partialTicks, true);
            }
        } catch (Exception ignored) {

        }
    }


    private void renderPointerArrowLine() {
        if (startPos != null && endPos != null) {
            GlStateManager.pushMatrix();
            GlStateManager.disableDepth();
            RenderUtil.draw3DArrowLine(startPos, endPos, SkyblockFeatures.config.MythologicalHelperNextColor);
            GlStateManager.enableDepth();
            GlStateManager.popMatrix();
        }
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent event) {
        if (!Utils.inSkyblock || !SkyblockFeatures.config.MythologicalHelper || prevBurrow == null || Utils.GetMC().theWorld == null) {
            return;
        }

        for (Entity entity : Utils.GetMC().theWorld.getLoadedEntityList()) {
            if (entity instanceof EntityArmorStand) {
                handleArmorStand((EntityArmorStand) entity);
            }
        }
    }

    private void handleArmorStand(EntityArmorStand armorStand) {
        ItemStack head = armorStand.getEquipmentInSlot(0);
        if (head != null && head.getUnlocalizedName().contains("arrow") && armorStand.getDistance(prevBurrow.getX(), prevBurrow.getY(), prevBurrow.getZ()) < 5) {
            double angle = Math.toRadians(armorStand.rotationYaw + 180);
            double lineLength = 500.0; // Adjust the line length as needed
            double endX = armorStand.posX + lineLength * Math.sin(angle);
            double endY = armorStand.posY + 2;
            double endZ = armorStand.posZ - lineLength * Math.cos(angle);
            Vec3 startPos2 = new Vec3(armorStand.posX, armorStand.posY + 2, armorStand.posZ);
            Vec3 endPos2 = new Vec3(endX, endY, endZ);
            startPos = startPos2.addVector(-0.5, 0, -0.5);
            endPos = endPos2.addVector(-0.5, 0, -0.5);
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        burrows.clear();
        startPos = null;
        endPos = null;
        geyser = null;
        predictedBurrowPosition = null;
        lavaDripParticles.clear();
    }
}
