package mrfast.skyblockfeatures.features.mining;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mojang.realmsclient.gui.ChatFormatting;

import java.awt.Color;

import mrfast.skyblockfeatures.SkyblockFeatures;
import mrfast.skyblockfeatures.utils.RenderUtil;
import mrfast.skyblockfeatures.core.SkyblockInfo;
import mrfast.skyblockfeatures.gui.GuiManager;
import mrfast.skyblockfeatures.utils.Utils;
import net.minecraft.block.BlockChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class MetalDetectorSolver {
    List<Vec3> chests = new ArrayList<>();
    List<BlockPos> foundChests = new ArrayList<>();
    List<Vec3> treasureLocations = new ArrayList<>(Arrays.asList(
        new Vec3(-38, -22, 26),
        new Vec3(38, -22, -26),
        new Vec3(-40, -22, 18),
        new Vec3(-41, -20, 22),
        new Vec3(-5, -21, 16),
        new Vec3(40, -22, -30),
        new Vec3(-42, -20, -28),
        new Vec3(-43, -22, -40),
        new Vec3(42, -19, -41),
        new Vec3(43, -21, -16),
        new Vec3(-1, -22, -20),
        new Vec3(6, -21, 28),
        new Vec3(7, -21, 11),
        new Vec3(7, -21, 22),
        new Vec3(-12, -21, -44),
        new Vec3(12, -22, 31),
        new Vec3(12, -22, -22),
        new Vec3(12, -21, 7),
        new Vec3(12, -21, -43),
        new Vec3(-14, -21, 43),
        new Vec3(-14, -21, 22),
        new Vec3(-17, -21, 20),
        new Vec3(-20, -22, 0),
        new Vec3(1, -21, 20),
        new Vec3(19, -22, 29),
        new Vec3(20, -22, 0),
        new Vec3(20, -21, -26),
        new Vec3(-23, -22, 40),
        new Vec3(22, -21, -14),
        new Vec3(-24, -22, 12),
        new Vec3(23, -22, 26),
        new Vec3(23, -22, -39),
        new Vec3(24, -22, 27),
        new Vec3(25, -22, 17),
        new Vec3(29, -21, -44),
        new Vec3(-31, -21, -12),
        new Vec3(-31, -21, -40),
        new Vec3(30, -21, -25),
        new Vec3(-32, -21, -40),
        new Vec3(-36, -20, 42),
        new Vec3(-37, -21, -14),
        new Vec3(-37, -21, -22)
    ));
    List<Vec3> PossibletreasureLocations = new ArrayList<>();
    Vec3 center = null;
    double distanceToTreasure = 0;
    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        center = null;
        bestPos = null;
    }
    double sameDistance = 0;
    double lastDistance = 0;

    @SubscribeEvent
	public void onEvent(ClientChatReceivedEvent event) {
        if(Utils.GetMC().thePlayer==null || !SkyblockFeatures.config.MetalDetectorSolver) return;
        if (event.type == 2) {
            String actionBar = event.message.getFormattedText();
            String[] actionBarSplit = actionBar.split(" ");
            boolean nextSegmentIsDistance = false;
            if(lastDistance==distanceToTreasure) {
                sameDistance++;
            } else {
                sameDistance = 0;
            }
            lastDistance=distanceToTreasure;
            for(String segment:actionBarSplit) {
                if(nextSegmentIsDistance) {
                    if(segment.contains(".")) distanceToTreasure = Double.parseDouble(Utils.cleanColor(segment).replaceAll("[^0-9]", ""))/10;
                    else distanceToTreasure = Double.parseDouble(Utils.cleanColor(segment).replaceAll("[^0-9]", ""));
                    nextSegmentIsDistance = false;
                }
                if(segment.contains("TREASURE")) {
                    nextSegmentIsDistance = true;
                }
            }
        } else {
            if(event.message.getUnformattedText().contains("with your Metal Detector")) {
                distanceToTreasure = 0;
                Utils.setTimeout(()->{
                    bestPos = null;
                    announcedFoundIt = false;
                    announcedRecalculating = false;
                }, 2000);
            }
        }
    }
    Vec3 bestPos = null;
    int ticks = 0;
    boolean announcedFoundIt = false;
    boolean announcedRecalculating = false;

    BlockPos LastPosition = null;
    @SubscribeEvent
    public void onRenderWorld(ClientTickEvent event) {
        if(Utils.GetMC().thePlayer==null || !SkyblockFeatures.config.MetalDetectorSolver) return;
        if(SkyblockInfo.getInstance().getLocation()!=null) {
            if(!SkyblockInfo.getInstance().getMap().equals("Crystal Hollows")) return;
        }
        ticks++;
        if(ticks >= 4 && Utils.GetMC().thePlayer.getHeldItem() != null && Utils.GetMC().thePlayer.getHeldItem().getDisplayName().contains("Detector") && distanceToTreasure!=0 && center!=null) {
            if(bestPos != null) {
                if(!announcedFoundIt) {
                    announcedFoundIt = true;
                    GuiManager.createTitle(ChatFormatting.AQUA+"Treasure Found!", 10);
                }
                Vec3 actualBestPos = center.add(bestPos);
    
                if(actualBestPos.distanceTo(Utils.GetMC().thePlayer.getPositionVector())<5) {
                    if(!(Utils.GetMC().theWorld.getBlockState(new BlockPos(actualBestPos)).getBlock() instanceof BlockChest)) {
                        distanceToTreasure = 0;
                        bestPos = null;
                        announcedFoundIt = false;
                        announcedRecalculating = false;
                    }
                }
            }
            if(bestPos == null && !announcedRecalculating) {
                announcedRecalculating = true;
                GuiManager.createTitle(ChatFormatting.RED+"Stand Still. Recalculating..", 40);
            }
            if(sameDistance>=2 && bestPos == null) {                
                ticks = 0;
                PossibletreasureLocations = treasureLocations;
                bestPos = null;
                for(Vec3 pos:PossibletreasureLocations) {
                    if(bestPos == null) {
                        bestPos = pos;
                    } else {
                        Vec3 actualPos = center.add(pos);
                        Vec3 actualBestPos = center.add(bestPos);
                        
                        double distToBest = Math.abs(actualBestPos.distanceTo(Utils.GetMC().thePlayer.getPositionVector())-distanceToTreasure);
                        double distToThis = Math.abs(actualPos.distanceTo(Utils.GetMC().thePlayer.getPositionVector())-distanceToTreasure);
                        if(distToThis<distToBest) {
                            bestPos = pos;
                        }
                    }
                }
            }
        }

        if(center == null) {
            for(Entity entity:Utils.GetMC().theWorld.loadedEntityList) {
                if(entity instanceof EntityArmorStand) {
                    if(entity.getCustomNameTag().contains("Keeper of Lapis")) {
                        center = entity.getPositionVector().addVector(-33, 0, -3);
                    }
                    if(entity.getCustomNameTag().contains("Keeper of Gold")) {
                        center = entity.getPositionVector().addVector(3, 0, -33);
                    }
                    if(entity.getCustomNameTag().contains("Keeper of Emerald")) {
                        center = entity.getPositionVector().addVector(-3, 0, 33);
                    }
                    if(entity.getCustomNameTag().contains("Keeper of Diamond")) {
                        center = entity.getPositionVector().addVector(33, 0, 3);
                    }
                }
            }
        }
    }
        
    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if(Utils.GetMC().thePlayer==null || !SkyblockFeatures.config.MetalDetectorSolver) return;
        if(center == null) return;
        GlStateManager.disableDepth();
        if(bestPos != null) {
            BlockPos pos = new BlockPos(bestPos.xCoord+center.xCoord, bestPos.yCoord+center.yCoord, bestPos.zCoord+center.zCoord);
            RenderUtil.drawWaypoint(pos, new Color(0x00FFFF),ChatFormatting.GOLD+"Treasure", event.partialTicks);
        }
        GlStateManager.enableDepth();
    }
    
}
