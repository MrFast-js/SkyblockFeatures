package mrfast.sbf.features.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.events.CheckRenderEntityEvent;
import mrfast.sbf.utils.RenderUtil;
import mrfast.sbf.utils.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityGiantZombie;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team.EnumVisible;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;

public class PlayerDiguiser {
    public static HashMap<Entity,Entity> tracker = new HashMap<>();
    public static HashMap<Entity,String> tabnameTracker = new HashMap<>();
    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        try {
            if(SkyblockFeatures.config.playerDiguiser) {
                for(Entity entity:tracker.keySet()) Utils.GetMC().theWorld.removeEntityFromWorld(tracker.get(entity).getEntityId());
                tracker.clear();
                tabnameTracker.clear();
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }
    public static Entity getEntity() {
        Entity end = null;
        switch (SkyblockFeatures.config.DisguisePlayersAs) {
            case 0:
                end = new EntityCow(Utils.GetMC().theWorld);
                break;
            case 1:
                end = new EntityPig(Utils.GetMC().theWorld);
                break;
            case 2:
                end = new EntitySheep(Utils.GetMC().theWorld);
                break;
            case 3:
                end = new EntityZombie(Utils.GetMC().theWorld);
                break;
            case 4:
                end = new EntityVillager(Utils.GetMC().theWorld);
                break;
            case 5:
                end = new EntityEnderman(Utils.GetMC().theWorld);
                break;
            case 6:
                end = new EntityGiantZombie(Utils.GetMC().theWorld);
                break;
            case 7:
                end = new EntityArrow(Utils.GetMC().theWorld);
                break;
            case 8:
                end = new EntityArrow(Utils.GetMC().theWorld);
                break;
            case 9:
                end = new EntityArrow(Utils.GetMC().theWorld);
                break;
            default:
                end = new EntityCow(Utils.GetMC().theWorld);
                break;
        }
        return end;
    }

    int storedSelection = 0;
    boolean storedToggle = false;

    @SubscribeEvent
    public void onCheckRender(CheckRenderEntityEvent event) {
        if (!Utils.inSkyblock || !SkyblockFeatures.config.playerDiguiser) return;
        try {
            if(storedSelection!=SkyblockFeatures.config.DisguisePlayersAs || storedToggle!=SkyblockFeatures.config.playerDiguiser) {
                for(Entity entity:tracker.keySet()) Utils.GetMC().theWorld.removeEntityFromWorld(tracker.get(entity).getEntityId());
                tracker.clear();
                tabnameTracker.clear();
                storedSelection=SkyblockFeatures.config.DisguisePlayersAs;
                storedToggle=SkyblockFeatures.config.playerDiguiser;
            }
        } catch (Exception e) {
            // TODO: handle exception
        }

        try {
            if (event.entity instanceof EntityPlayer && SkyblockFeatures.config.playerDiguiser && event.entity!=Utils.GetMC().thePlayer && !event.entity.isInvisible() && !event.entity.isDead) {
                if(tracker.containsKey(event.entity)) {
                    if(SkyblockFeatures.config.DisguisePlayersAs == 7) return;
                    if(SkyblockFeatures.config.DisguisePlayersAs == 8) return;
                    if(SkyblockFeatures.config.DisguisePlayersAs == 9) return;

                    Entity disguise = tracker.get(event.entity);
                    disguise.setPosition(event.entity.posX, event.entity.posY, event.entity.posZ);
                    disguise.rotationYaw = event.entity.rotationYaw;
                    disguise.rotationPitch = event.entity.rotationPitch;
                    disguise.setRotationYawHead(event.entity.getRotationYawHead()); 
                    disguise.spawnRunningParticles();
                    event.setCanceled(true);
                } else {
                    Entity disguise = getEntity();
                    Utils.GetMC().theWorld.addEntityToWorld((int) Math.floor(Math.random()*10000000), disguise);
                    if(SkyblockFeatures.config.DisguisePlayersAs == 7) disguise.setInvisible(true);
                    if(SkyblockFeatures.config.DisguisePlayersAs == 8) disguise.setInvisible(true);
                    if(SkyblockFeatures.config.DisguisePlayersAs == 9) disguise.setInvisible(true);

                    tracker.put(event.entity, disguise);
                }
            }
        } catch (Exception ignored) {

        }
    }
    @SubscribeEvent
    public void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        // Check if the player is wearing a skull
        if (event.entityPlayer.getEntityData().getByte("SkullOwner") == 3) { // Player skull type
            // Cancel the rendering of player skulls
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        if (!Utils.inSkyblock || !SkyblockFeatures.config.playerDiguiser) return;
        try {
            for(Entity original:tracker.keySet()) {
                if(original==null || original.isDead) {
                    Utils.GetMC().theWorld.removeEntityFromWorld(tracker.get(original).getEntityId());
                    tracker.remove(original);
                    continue;
                }
                String originalName = original.getName();
                ScorePlayerTeam scoreplayerteam = (ScorePlayerTeam) ((EntityPlayer) original).getTeam();
                if (scoreplayerteam != null) {
                    originalName = scoreplayerteam.getColorPrefix()+originalName;
                    scoreplayerteam.setNameTagVisibility(EnumVisible.NEVER);
                }
                if(!Utils.isNPC(original)) {
                    if(SkyblockFeatures.config.hidePlayerNametags) {
                        continue;
                    }
                    if(SkyblockFeatures.config.DisguisePlayersAs==4) {
                        RenderUtil.draw3DStringWithShadow(original.getPositionVector().add(new Vec3(0,2.6,0)), "Jerry", 0xFFFFFF, event.partialTicks);
                        RenderUtil.draw3DStringWithShadow(original.getPositionVector().add(new Vec3(0,2.3,0)), ChatFormatting.YELLOW+""+ChatFormatting.BOLD+"CLICK", 0xFFFFFF, event.partialTicks);
                    } else if(SkyblockFeatures.config.DisguisePlayersAs == 3) {
                        RenderUtil.draw3DStringWithShadow(original.getPositionVector().add(new Vec3(0,2.3,0)), originalName, 0xFFFFFF, event.partialTicks);
                    } else if(SkyblockFeatures.config.DisguisePlayersAs == 8) {
                        RenderUtil.draw3DStringWithShadow(original.getPositionVector().add(new Vec3(0,1.8,0)), originalName, 0xFFFFFF, event.partialTicks);
                    } else if(SkyblockFeatures.config.DisguisePlayersAs == 5) {
                        RenderUtil.draw3DStringWithShadow(original.getPositionVector().add(new Vec3(0,3.3,0)), originalName, 0xFFFFFF, event.partialTicks);
                    } else if(SkyblockFeatures.config.DisguisePlayersAs == 7) {
                        RenderUtil.draw3DStringWithShadow(original.getPositionVector().add(new Vec3(0,1.3,0)), originalName, 0xFFFFFF, event.partialTicks);
                    } else {
                        RenderUtil.draw3DStringWithShadow(original.getPositionVector().add(new Vec3(0,1.7,0)), originalName, 0xFFFFFF, event.partialTicks);
                    }
                }
            }   
        } catch (Exception e) {
            // TODO: handle exception
        }
    }


}
