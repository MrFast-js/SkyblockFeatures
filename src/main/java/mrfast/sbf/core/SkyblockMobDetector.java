package mrfast.sbf.core;

import com.mojang.realmsclient.gui.ChatFormatting;
import mrfast.sbf.events.SkyblockMobEvent;
import mrfast.sbf.utils.RenderUtil;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SkyblockMobDetector {
    static HashMap<Entity,SkyblockMob> skyblockMobHashMap = new HashMap<>();

    public static class SkyblockMob {
        public Entity mobNameEntity;
        public Entity skyblockMob;
        public String skyblockMobId;
        public SkyblockMob(Entity armorStandEntity,Entity skyblockMob) {
            this.mobNameEntity = armorStandEntity;
            this.skyblockMob = skyblockMob;
        }
        public String getSkyblockMobId() {
            return this.skyblockMobId;
        }
        public Entity getSkyblockMob() {
            return this.skyblockMob;
        }

    }
    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        skyblockMobHashMap.clear();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if(Utils.GetMC().theWorld==null || !Utils.inSkyblock) return;

        for(Entity entity:Utils.GetMC().theWorld.loadedEntityList) {
            if(entity instanceof EntityArmorStand && !skyblockMobHashMap.containsKey(entity) && entity.hasCustomName() && entity.getDisplayName().getUnformattedText().contains("❤")) {
                if(Utils.GetMC().thePlayer.getDistanceToEntity(entity)>30) continue;
                Entity potentialMob = Utils.GetMC().theWorld.getEntityByID(entity.getEntityId()-1);
                if(potentialMob==null || !potentialMob.isEntityAlive()) continue;

                SkyblockMob sbMob = new SkyblockMob(entity, potentialMob);
                skyblockMobHashMap.put(entity,sbMob);
            }
        }
        for (SkyblockMob sbMob : skyblockMobHashMap.values()) {
            if(Utils.GetMC().thePlayer.getDistanceToEntity(sbMob.skyblockMob)>30) continue;

            if(sbMob.skyblockMobId==null) {
                updateMobData(sbMob);
                if(sbMob.skyblockMobId!=null) {
                    MinecraftForge.EVENT_BUS.post(new SkyblockMobEvent.Spawn(sbMob));
                }
            }
        }
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        Iterator<Map.Entry<Entity, SkyblockMob>> iterator = skyblockMobHashMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Entity, SkyblockMob> entry = iterator.next();
            SkyblockMob sbMob = entry.getValue();

            if (!sbMob.skyblockMob.isEntityAlive()) {
                iterator.remove();
                if(sbMob.skyblockMobId!=null) {
                    if(Utils.GetMC().thePlayer.getDistanceToEntity(sbMob.mobNameEntity)>30) continue;

                    MinecraftForge.EVENT_BUS.post(new SkyblockMobEvent.Death(sbMob));
                }
            } else {
                MinecraftForge.EVENT_BUS.post(new SkyblockMobEvent.Render(sbMob,event.partialTicks));
            }
        }
    }

    @SubscribeEvent
    public void onRenderMob(SkyblockMobEvent.Render event) {
        SkyblockMob sbMob = event.getSbMob();
        if(sbMob.skyblockMobId!=null && Utils.isDeveloper() && Utils.GetMC().thePlayer.canEntityBeSeen(sbMob.skyblockMob)) {
            Vec3 pos = new Vec3(sbMob.skyblockMob.posX,sbMob.skyblockMob.posY+1,sbMob.skyblockMob.posZ);
            GlStateManager.disableDepth();
            RenderUtil.draw3DString(pos, ChatFormatting.YELLOW+sbMob.skyblockMobId,event.partialTicks);
            GlStateManager.enableDepth();
        }
    }

    public static void updateMobData(SkyblockMob sbMob) {
        String rawMobName = Utils.cleanColor(sbMob.mobNameEntity.getDisplayName().getUnformattedText());

        String normalMobRegex = "\\[Lv(?:\\d+k?)] (.+?) [\\d.,]+[MkB]?/[\\d.,]+[MkB]?❤";
        String slayerMobRegex = "(?<=☠\\s)\\w+\\s\\w+\\s\\w+";
        String dungeonMobRegex = "✯?\\s*(?:Flaming|Super|Healing|Boomer|Golden|Speedy|Fortified|Stormy|Healthy)?\\s*([\\w\\s]+?)\\s*([\\d.,]+[mkM?]*|[?]+)❤";

        String regexBeingUsed = null;
        Pattern pattern;
        Matcher matcher = null;

        // Iterate through the regex patterns
        for (String regex : new String[]{normalMobRegex, slayerMobRegex, dungeonMobRegex}) {
            pattern = Pattern.compile(regex);
            matcher = pattern.matcher(rawMobName);

            if (matcher.find()) {
                regexBeingUsed = regex;
                break;  // Break out of the loop when a match is found
            }
        }

        if (regexBeingUsed != null) {
            if (regexBeingUsed.equals(normalMobRegex)) {
                sbMob.skyblockMobId = matcher.group(1);
            }
            if(regexBeingUsed.equals(slayerMobRegex)) {
                sbMob.skyblockMobId = matcher.group()+" Slayer";
            }
            if(regexBeingUsed.equals(dungeonMobRegex)) {
                sbMob.skyblockMobId = matcher.group(1);
                // To Help With Better Pest Detection
                if(rawMobName.startsWith("ൠ")) {
                    sbMob.skyblockMobId = matcher.group(1)+" Pest";
                }
            }
        }
    }

    public static List<SkyblockMob> getLoadedSkyblockMobs() {
        return new ArrayList<>(skyblockMobHashMap.values());
    }

    public static Entity getEntityByName(String id) {
        SkyblockMob sbMob = getLoadedSkyblockMobs()
                .stream()
                .filter(mob -> mob.getSkyblockMobId().equals(id))
                .findFirst()
                .orElse(null);
        if(sbMob!=null) return sbMob.skyblockMob;
        else return null;
    }

    public static List<Entity> getEntitiesByName(String id) {
        return getLoadedSkyblockMobs()
                .stream()
                .filter(mob -> mob.getSkyblockMobId().equals(id))
                .map(SkyblockMob::getSkyblockMob)
                .collect(Collectors.toList());
    }

    public static String getEntityId(Entity entity) {
        SkyblockMob sbMob = getLoadedSkyblockMobs()
                .stream()
                .filter(mob -> mob.getSkyblockMob().equals(entity))
                .findFirst()
                .orElse(null);
        if(sbMob!=null) return sbMob.skyblockMobId;
        else return null;
    }
}
