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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    }
    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        skyblockMobHashMap.clear();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if(Utils.GetMC().theWorld==null) return;
        for(Entity entity:Utils.GetMC().theWorld.loadedEntityList) {
            if(entity instanceof EntityArmorStand && !skyblockMobHashMap.containsKey(entity) && entity.hasCustomName() && entity.getDisplayName().getUnformattedText().contains("❤")) {
                if(Utils.GetMC().thePlayer.getDistanceToEntity(entity)>20) continue;
                Entity potentialMob = Utils.GetMC().theWorld.getEntityByID(entity.getEntityId()-1);
                if(potentialMob==null || !potentialMob.isEntityAlive()) continue;

                SkyblockMob sbMob = new SkyblockMob(entity, potentialMob);
                skyblockMobHashMap.put(entity,sbMob);
            }
        }
        for (SkyblockMob sbMob : skyblockMobHashMap.values()) {
            if(Utils.GetMC().thePlayer.getDistanceToEntity(sbMob.skyblockMob)>20) continue;

            boolean nullBefore = sbMob.skyblockMobId==null;
            if(nullBefore) {
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

            if (sbMob.skyblockMob == null) {
                iterator.remove();
            } else if (!sbMob.skyblockMob.isEntityAlive()) {
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
        String dungeonMobRegex = "✯?\\s*(?:Flaming|Speedy|Fortified|Stormy|Healthy)?\\s*([\\w\\s]+?)\\s*([\\d.,]+[mkM?]*|[?]+)❤";

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
                sbMob.skyblockMobId = matcher.group();
            }
            if(regexBeingUsed.equals(dungeonMobRegex)) {
                sbMob.skyblockMobId = matcher.group(1);
            }
        }
    }

    public static List<SkyblockMob> getLoadedSkyblockMobs() {
        return (List<SkyblockMob>) skyblockMobHashMap.values();
    }
}
