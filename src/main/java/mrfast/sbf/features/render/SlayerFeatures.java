package mrfast.sbf.features.render;

import com.mojang.realmsclient.gui.ChatFormatting;
import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.SkyblockInfo;
import mrfast.sbf.core.SkyblockMobDetector;
import mrfast.sbf.events.SkyblockMobEvent;
import mrfast.sbf.utils.OutlineUtils;
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
        if(sbMob==null) return false;
        List<String> miniboss = new ArrayList<>(Arrays.asList("Revenant Sycophant","Revenant Champion","Deformed Revenant","Atoned Champion","Atoned Revenant","Tarantula Vermin", "Tarantula Beast", "Mutant Tarantula","Pack Enforcer","Sven Follower", "Sven Alpha", "Voidling Devotee", "Voidling Radical", "Voidcrazed Maniac","Flare Demon","Kindleheart Demon","Burningsoul Demon"));
        return miniboss.contains(sbMob.skyblockMobId);
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if(Utils.GetMC().theWorld == null || Utils.GetMC().thePlayer == null || SkyblockInfo.getLocation()==null) return;

        if(SkyblockFeatures.config.highlightSlayers) {
            for (Entity entity : Utils.GetMC().theWorld.loadedEntityList) {
                SkyblockMobDetector.SkyblockMob sbMob = SkyblockMobDetector.getSkyblockMob(entity);
                if (sbMob == null) continue;

                if (sbMob.skyblockMob == entity && sbMob.getSkyblockMobId() != null) {
                    if(SkyblockFeatures.config.highlightSlayerMiniboss) {
                        if (isMiniboss(sbMob)) {
                            OutlineUtils.renderOutline(sbMob.skyblockMob, SkyblockFeatures.config.highlightSlayerMinibossColor, false);
                        }
                    }
                    if(SkyblockFeatures.config.highlightSlayers && sbMob.getSkyblockMobId().endsWith("Slayer")) {
                        if (sbMob.getSkyblockMobId().contains("Voidgloom") && SkyblockFeatures.config.highlightVoidgloomColors) {
                            boolean hitPhase = sbMob.mobNameEntity.getCustomNameTag().contains("Hits");
                            boolean laserPhase = sbMob.skyblockMob.isRiding();

                            if (laserPhase) {
                                // Laser Phase
                                OutlineUtils.renderOutline(sbMob.skyblockMob, SkyblockFeatures.config.highlightVoidgloomLaserPhase, false);
                            } else if (hitPhase) {
                                // Hit Phase
                                OutlineUtils.renderOutline(sbMob.skyblockMob, SkyblockFeatures.config.highlightVoidgloomHitPhase, false);
                            } else {
                                // Default render
                                OutlineUtils.renderOutline(sbMob.skyblockMob, SkyblockFeatures.config.highlightSlayerColor, false);
                            }
                        } else {
                            // Default render
                            OutlineUtils.renderOutline(sbMob.skyblockMob, SkyblockFeatures.config.highlightSlayerColor, false);
                        }
                    }
                }
            }
        }

        if(!SkyblockFeatures.config.highlightBeacons || !SkyblockInfo.map.equals("The End")) return;
        for(TileEntity e:Utils.GetMC().theWorld.loadedTileEntityList) {
            List<SkyblockMobDetector.SkyblockMob> nearbySlayers = SkyblockMobDetector.getLoadedSkyblockMobs().stream().filter((sbMob)->sbMob.skyblockMob.getDistanceToEntity(Utils.GetMC().thePlayer)<20).collect(Collectors.toList());
            boolean slayerNearby = false;
            for(SkyblockMobDetector.SkyblockMob sbMob:nearbySlayers) {
                if(sbMob==null || sbMob.getSkyblockMobId()==null) continue;
                if(sbMob.getSkyblockMobId().contains("Voidgloom")) {
                    slayerNearby = true;
                    break;
                }
            }

            if(e instanceof TileEntityBeacon && Utils.GetMC().thePlayer.getDistanceSq(e.getPos())<400 && slayerNearby) {
                BlockPos p = e.getPos();
                AxisAlignedBB aabb = new AxisAlignedBB(p.getX(), p.getY(), p.getZ(), p.getX()+1, p.getY()+1, p.getZ()+1);
                if(SkyblockFeatures.config.highlightBeaconsThroughWalls) GlStateManager.disableDepth();
                RenderUtil.drawOutlinedFilledBoundingBox(aabb, SkyblockFeatures.config.highlightBeaconsColor, event.partialTicks);
                GlStateManager.enableDepth();
            }
        }
    }

    long slayerStarted = System.currentTimeMillis();
    long slayerSpawned = System.currentTimeMillis();

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        String msg = event.message.getUnformattedText();
        if(msg.trim().startsWith("SLAYER QUEST STARTED!")) {
            slayerStarted = System.currentTimeMillis();
        }
        if(msg.trim().startsWith("NICE! SLAYER BOSS SLAIN!") || msg.trim().startsWith("SLAYER QUEST COMPLETE!")) {
            if(SkyblockFeatures.config.slayerTimer) {
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

    @SubscribeEvent
    public void onSbMobSpawn(SkyblockMobEvent.Spawn event) {
        if(!SkyblockFeatures.config.slayerTimer) return;

        if(event.getSbMob().getSkyblockMobId().endsWith("Slayer")) {
            boolean nextLine = false;
            String slayerName = "";
            boolean hasSlayerSpawned = false;

            for(String line: ScoreboardUtil.getSidebarLines(true)) {
                if(nextLine) {
                    nextLine = false;
                    slayerName = line;
                }
                if(line.contains("Slayer Quest")) {
                    nextLine = true;
                }
                if(line.contains("Slay the boss!")) {
                    hasSlayerSpawned = true;
                }
            }
            if(event.getSbMob().getSkyblockMobId().startsWith(slayerName) && hasSlayerSpawned) {
                slayerSpawned = System.currentTimeMillis();
                spawnedSlayer = event.getSbMob();
                Utils.sendMessage("Slayer Spawned");
            }
        }
    }
}
