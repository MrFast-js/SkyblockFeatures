package mrfast.sbf.features.render;

import com.mojang.realmsclient.gui.ChatFormatting;
import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.SkyblockInfo;
import mrfast.sbf.core.SkyblockMobDetector;
import mrfast.sbf.events.RenderEntityOutlineEvent;
import mrfast.sbf.events.SkyblockMobEvent;
import mrfast.sbf.utils.RenderUtil;
import mrfast.sbf.utils.ScoreboardUtil;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;
import java.util.stream.Collectors;

public class SlayerFeatures {
    SkyblockMobDetector.SkyblockMob spawnedSlayer = null;

    public boolean isMiniboss(SkyblockMobDetector.SkyblockMob sbMob) {
        if (sbMob == null) return false;
        List<String> miniboss = new ArrayList<>(Arrays.asList("Revenant Sycophant", "Revenant Champion", "Deformed Revenant", "Atoned Champion", "Atoned Revenant", "Tarantula Vermin", "Tarantula Beast", "Mutant Tarantula", "Pack Enforcer", "Sven Follower", "Sven Alpha", "Voidling Devotee", "Voidling Radical", "Voidcrazed Maniac", "Flare Demon", "Kindleheart Demon", "Burningsoul Demon"));
        return miniboss.contains(sbMob.skyblockMobId);
    }

    @SubscribeEvent
    public void onRenderEntityOutlines(RenderEntityOutlineEvent event) {
        if (Utils.GetMC().theWorld == null || Utils.GetMC().thePlayer == null || SkyblockInfo.getLocation() == null)
            return;
        if (event.type == RenderEntityOutlineEvent.Type.XRAY) return;

        if (SkyblockFeatures.config.highlightSlayers) {
            for (Entity entity : Utils.GetMC().theWorld.loadedEntityList) {
                SkyblockMobDetector.SkyblockMob sbMob = SkyblockMobDetector.getSkyblockMob(entity);
                if (sbMob == null) continue;

                if (sbMob.skyblockMob == entity && sbMob.getSkyblockMobId() != null) {
                    if (SkyblockFeatures.config.highlightSlayerMiniboss) {
                        if (isMiniboss(sbMob)) {
                            event.queueEntityToOutline(sbMob.skyblockMob, SkyblockFeatures.config.highlightSlayerMinibossColor);
                        }
                    }
                    if (SkyblockFeatures.config.highlightSlayers && sbMob.getSkyblockMobId().endsWith("Slayer")) {
                        if (sbMob.getSkyblockMobId().contains("Voidgloom") && SkyblockFeatures.config.highlightVoidgloomColors) {
                            boolean hitPhase = sbMob.mobNameEntity.getCustomNameTag().contains("Hits");
                            boolean laserPhase = sbMob.skyblockMob.isRiding();

                            if (laserPhase) {
                                // Laser Phase
                                event.queueEntityToOutline(sbMob.skyblockMob, SkyblockFeatures.config.highlightVoidgloomLaserPhase);
                            } else if (hitPhase) {
                                // Hit Phase
                                event.queueEntityToOutline(sbMob.skyblockMob, SkyblockFeatures.config.highlightVoidgloomHitPhase);
                            } else {
                                // Default render
                                event.queueEntityToOutline(sbMob.skyblockMob, SkyblockFeatures.config.highlightSlayerColor);
                            }
                        } else {
                            // Default render
                            event.queueEntityToOutline(sbMob.skyblockMob, SkyblockFeatures.config.highlightSlayerColor);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void render3d(RenderWorldLastEvent event) {
        if (Utils.GetMC().theWorld == null || Utils.GetMC().thePlayer == null || SkyblockInfo.getLocation() == null)
            return;
        if (!SkyblockFeatures.config.highlightBeacons || !SkyblockInfo.map.equals("The End")) return;

        for (TileEntity e : Utils.GetMC().theWorld.loadedTileEntityList) {
            List<SkyblockMobDetector.SkyblockMob> nearbySlayers = SkyblockMobDetector.getLoadedSkyblockMobs().stream().filter((sbMob) -> sbMob.skyblockMob.getDistanceToEntity(Utils.GetMC().thePlayer) < 20).collect(Collectors.toList());
            boolean slayerNearby = false;
            for (SkyblockMobDetector.SkyblockMob sbMob : nearbySlayers) {
                if (sbMob == null || sbMob.getSkyblockMobId() == null) continue;
                if (sbMob.getSkyblockMobId().contains("Voidgloom")) {
                    slayerNearby = true;
                    break;
                }
            }

            if (e instanceof TileEntityBeacon && Utils.GetMC().thePlayer.getDistanceSq(e.getPos()) < 400 && slayerNearby) {
                BlockPos p = e.getPos();
                AxisAlignedBB aabb = new AxisAlignedBB(p.getX(), p.getY(), p.getZ(), p.getX() + 1, p.getY() + 1, p.getZ() + 1);
                if (SkyblockFeatures.config.highlightBeaconsThroughWalls) GlStateManager.disableDepth();
                RenderUtil.drawOutlinedFilledBoundingBox(aabb, SkyblockFeatures.config.highlightBeaconsColor, event.partialTicks);
                GlStateManager.enableDepth();
            }
        }
    }

    static long slayerStarted = System.currentTimeMillis();
    static long slayerSpawned = System.currentTimeMillis();

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        String msg = event.message.getUnformattedText();
        if (msg.trim().startsWith("SLAYER QUEST STARTED!")) {
            // use timeout because auto slayer will mess it up otherwise 
            Utils.setTimeout(() -> {

                slayerStarted = System.currentTimeMillis();
                if (Utils.isDeveloper()) {
                    Utils.sendMessage("Slayer quest started");
                }
            }, 100);
        }
        if (msg.trim().startsWith("NICE! SLAYER BOSS SLAIN!") || msg.trim().startsWith("SLAYER QUEST COMPLETE!")) {
            if (SkyblockFeatures.config.slayerTimer) {
                if (Utils.isDeveloper()) {
                    Utils.sendMessage("Slayer Killed! Sending timer..");
                }
                hasSlayerSpawned = false;

                double spawn = System.currentTimeMillis() - slayerStarted;
                double spawnTime = Math.ceil(spawn / 1000);
                double kill = System.currentTimeMillis() - slayerSpawned;
                double killTime = Math.ceil(kill / 1000);
                String totalTime = Utils.secondsToTime((long) Math.ceil(spawnTime + killTime));

                Utils.setTimeout(() -> {
                    Utils.sendMessage(ChatFormatting.GOLD + ChatFormatting.BOLD.toString() + "Slayer Timer\n" +
                            ChatFormatting.AQUA + "        • Total Time: " + totalTime + "\n" +
                            ChatFormatting.YELLOW + "        • Spawn: " + Utils.secondsToTime((long) spawnTime) + "\n" +
                            ChatFormatting.YELLOW + "        • Kill: " + Utils.secondsToTime((long) killTime));
                }, 10);
            }
            slayerSpawned = 0;
            slayerStarted = 0;
        }
    }

    boolean hasSlayerSpawned = false;

    @SubscribeEvent
    public void onSbMobSpawn(SkyblockMobEvent.Render event) {
        if (!SkyblockFeatures.config.slayerTimer || hasSlayerSpawned) return;
        if (event.getSbMob() == null || event.getSbMob().getSkyblockMobId() == null) return;
        if (event.getSbMob().getSkyblockMobId().endsWith("Slayer")) {
            boolean nextLine = false;
            String slayerName = "";

            for (String line : ScoreboardUtil.getSidebarLines(true)) {
                if (nextLine) {
                    nextLine = false;
                    slayerName = getActualSlayerName(line);
                }
                if (line.contains("Slayer Quest")) {
                    nextLine = true;
                }
                if (line.contains("Slay the boss!")) {
                    if (Utils.isDeveloper()) {
                        Utils.sendMessage("Detected sidebar slayer spawned");
                    }
                    hasSlayerSpawned = true;
                }
            }
            if (event.getSbMob().getSkyblockMobId().startsWith(slayerName) && hasSlayerSpawned) {
                slayerSpawned = System.currentTimeMillis();
                spawnedSlayer = event.getSbMob();
                if (Utils.isDeveloper()) {
                    Utils.sendMessage("Slayer Spawned");
                }
            }
        }
    }

    public String getActualSlayerName(String sidebarName) {
        if (sidebarName.contains("Revenant Horror V")) {
            return "Atoned Horror";
        }
        if (sidebarName.contains("Riftslalker Bloodfiend")) {
            return "Bloodfiend";
        }
        return sidebarName;
    }

}
