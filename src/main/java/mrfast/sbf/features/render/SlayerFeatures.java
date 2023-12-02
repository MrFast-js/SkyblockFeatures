package mrfast.sbf.features.render;

import com.mojang.realmsclient.gui.ChatFormatting;
import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.SkyblockInfo;
import mrfast.sbf.events.SkyblockMobEvent;
import mrfast.sbf.utils.RenderUtil;
import mrfast.sbf.utils.ScoreboardUtil;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SlayerFeatures {
    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if(Utils.GetMC().theWorld == null || Utils.GetMC().thePlayer == null || SkyblockInfo.getLocation()==null) return;
        if(!SkyblockFeatures.config.highlightBeacons || !SkyblockInfo.map.equals("The End")) return;

        for(TileEntity e:Utils.GetMC().theWorld.loadedTileEntityList) {
            if(e instanceof TileEntityBeacon) {
                BlockPos p = e.getPos();
                AxisAlignedBB aabb = new AxisAlignedBB(p.getX(), p.getY(), p.getZ(), p.getX()+1, p.getY()+1, p.getZ()+1);
                if(SkyblockFeatures.config.highlightBeaconsThroughWalls) GlStateManager.disableDepth();
                RenderUtil.drawOutlinedFilledBoundingBox(aabb, SkyblockFeatures.config.highlightBeaconsColor, event.partialTicks);
                GlStateManager.enableDepth();
            }
        }
    }

    long slayerStarted = 0;
    long slayerSpawned = 0;

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if(!SkyblockFeatures.config.slayerTimer) return;
        String msg = event.message.getUnformattedText();
        if(msg.trim().startsWith("SLAYER QUEST STARTED!")) {
            slayerStarted = System.currentTimeMillis();
        }
        if(msg.trim().startsWith("NICE! SLAYER BOSS SLAIN!")) {
            double spawn = System.currentTimeMillis()-slayerStarted;
            double spawnTime = Math.ceil(spawn / 1000);
            double kill = System.currentTimeMillis()-slayerSpawned;
            double killTime = Math.ceil(kill / 1000);
            String totalTime = Utils.secondsToTime((long) Math.ceil(spawnTime + killTime));

            Utils.setTimeout(()-> {
                    Utils.sendMessage(ChatFormatting.GOLD+ChatFormatting.BOLD.toString() + "Slayer Timer\n" +
                            ChatFormatting.AQUA + "        • Total Time: " + totalTime + "\n" +
                            ChatFormatting.YELLOW + "        • Spawn: " + Utils.secondsToTime((long) spawnTime) + "\n" +
                            ChatFormatting.YELLOW + "        • Kill: " + Utils.secondsToTime((long) killTime));
            },10);
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
            }
        }
    }
}
