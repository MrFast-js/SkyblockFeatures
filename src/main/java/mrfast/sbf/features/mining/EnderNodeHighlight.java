package mrfast.sbf.features.mining;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.SkyblockInfo;
import mrfast.sbf.events.BlockChangeEvent;
import mrfast.sbf.events.PacketEvent;
import mrfast.sbf.events.SecondPassedEvent;
import mrfast.sbf.utils.RenderUtil;
import mrfast.sbf.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockObsidian;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EnderNodeHighlight {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public int seconds = 0;

    @SubscribeEvent
    public void onPlayerInteractEvent(SecondPassedEvent event) {
        if (Utils.inSkyblock && SkyblockFeatures.config.highlightEnderNodes && SkyblockInfo.localLocation.contains("The End")) {
            seconds++;
            if (seconds % 5 == 0) {
                enderParticles.clear();
                drawnPositions.clear();
                seconds = 0;
            }
        }
    }

    @SubscribeEvent
    public void onPlayerInteractEvent(BlockChangeEvent event) {
        if (Utils.inSkyblock && SkyblockFeatures.config.highlightEnderNodes && SkyblockInfo.localLocation.contains("The End")) {
            BlockPos p1 = Utils.GetMC().thePlayer.getPosition();
            BlockPos p2 = event.pos;
            if (p1.distanceSq(p2.getX(), p2.getY(), p2.getZ()) < 75) {
                enderParticles.clear();
                drawnPositions.clear();
            }
        }
    }

    List<Vec3> drawnPositions = new ArrayList<>();

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (!Utils.inSkyblock) return;
        if (SkyblockFeatures.config.highlightEnderNodes && SkyblockInfo.localLocation.contains("The End")) {
            try {
                if (SkyblockFeatures.config.highlightEnderNodesWalls) GlStateManager.disableDepth();

                for (Vec3 packet : enderParticles) {
                    boolean dupe = false;
                    double x = packet.xCoord;
                    double y = packet.yCoord;
                    double z = packet.zCoord;
                    if (!drawnPositions.contains(packet)) {
                        for (Vec3 packet2 : drawnPositions) {
                            if (packet.distanceTo(packet2) < 1.5) {
                                dupe = true;
                            }
                        }
                        if (dupe) continue;
                    }
                    if ((x - Math.floor(x)) == 0.25) {
                        BlockPos blockPos = new BlockPos(Math.round(x - 1.25), Math.round(y - 0.5), Math.round(z - 0.5));
                        addNode(blockPos, event.partialTicks, packet);
                    }
                    if ((y - Math.floor(y)) == 0.25) {
                        BlockPos blockPos = new BlockPos(Math.round(x - 1), Math.round(y - 1.25), Math.round(z - 0.5));
                        addNode(blockPos, event.partialTicks, packet);
                    }
                    if ((z - Math.floor(z)) == 0.25) {
                        BlockPos blockPos = new BlockPos(Math.round(x - 0.5), Math.round(y - 0.5), Math.round(z - 1.25));
                        addNode(blockPos, event.partialTicks, packet);
                    }
                    if ((x - Math.floor(x)) == 0.75) {
                        BlockPos blockPos = new BlockPos(Math.round(x + 0.25), Math.round(y - 0.5), Math.round(z - 0.5));
                        addNode(blockPos, event.partialTicks, packet);
                    }
                    if ((z - Math.floor(z)) == 0.75) {
                        BlockPos blockPos = new BlockPos(Math.round(x - 0.5), Math.round(y - 0.5), Math.round(z + 0.25));
                        addNode(blockPos, event.partialTicks, packet);
                    }
                }
                GlStateManager.enableDepth();
            } catch (Exception e) {
                // e.printStackTrace();
            }
        }
    }

    public void addNode(BlockPos blockPos, float partialTicks, Vec3 packet) {
        Color endColor = SkyblockFeatures.config.highlightEnderNodesEndstoneColor;
        Color obiColor = SkyblockFeatures.config.highlightEnderNodesObiColor;
        Block block = Utils.GetMC().theWorld.getBlockState(blockPos).getBlock();
        Color color = block instanceof BlockObsidian ? obiColor : endColor;
        RenderUtil.drawOutlinedFilledBoundingBox(blockPos, color, partialTicks);
        drawnPositions.add(packet);
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        particles.clear();
        drawnPositions.clear();
        enderParticles.clear();
    }

    List<Vec3> particles = new ArrayList<>();
    List<Vec3> enderParticles = new ArrayList<>();

    @SubscribeEvent
    public void onReceivePacket(PacketEvent.ReceiveEvent event) {
        if (Utils.GetMC() == null || Utils.GetMC().thePlayer == null) return;
        boolean inEnd = SkyblockInfo.map.equals("The End");
        if (event.packet instanceof S2APacketParticles && SkyblockFeatures.config.highlightEnderNodes && inEnd) {
            S2APacketParticles packet = (S2APacketParticles) event.packet;
            EnumParticleTypes type = packet.getParticleType();
            Vec3 pos = new Vec3(packet.getXCoordinate(), packet.getYCoordinate(), packet.getZCoordinate());
            if (type == EnumParticleTypes.PORTAL && !enderParticles.contains(pos)) {
                enderParticles.add(pos);
            }
        }
    }
}
