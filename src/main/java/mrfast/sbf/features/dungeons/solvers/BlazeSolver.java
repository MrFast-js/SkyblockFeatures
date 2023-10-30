package mrfast.sbf.features.dungeons.solvers;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.utils.RenderUtil;
import mrfast.sbf.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Modified from SkyblockMod under GNU Lesser General Public License v3.0
 * https://github.com/bowser0000/SkyblockMod/blob/master/COPYING
 * @author Bowser0000
 */
public class BlazeSolver {

    static boolean higherToLower = false;
    static boolean foundChest = false;
    static List<Blaze> blazes = new ArrayList<>();

    public static class Blaze {
        int health = 0;
        Entity EntityBlaze;

        public Blaze(Entity entity,int hp) {
            health = hp;
            EntityBlaze = entity;
        }
    }
    
    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        higherToLower = false;
        foundChest = false;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START || !Utils.inDungeons || !SkyblockFeatures.config.blazeSolver) return;

        EntityPlayerSP player = Utils.GetMC().thePlayer;
        World world = Utils.GetMC().theWorld;

        if (world != null && player != null) {
            blazes.clear();
            List<Entity> entities = world.getLoadedEntityList();

            for (Entity entity : entities) {
                if (entity.getName().contains("Blaze") && entity.getName().contains("/")) {
                    String blazeName = Utils.cleanColor(entity.getName().replaceAll(",", ""));
                    try {
                        int health = Integer.parseInt(blazeName.substring(blazeName.indexOf("/") + 1, blazeName.length() - 1));
                        blazes.add(new Blaze(entity, health));
                    } catch (NumberFormatException ex) {
                        ex.printStackTrace();
                    }
                }
            }

            if (!foundChest) {
                new Thread(() -> {
                    Iterable<BlockPos> blocks = BlockPos.getAllInBox(new BlockPos(player.posX - 27, 69, player.posZ - 27), new BlockPos(player.posX + 27, 70, player.posZ + 27));
                    for (BlockPos blockPos : blocks) {
                        Block block = world.getBlockState(blockPos).getBlock();
                        if (block == Blocks.chest && world.getBlockState(blockPos.add(0, 1, 0)).getBlock() == Blocks.iron_bars) {
                            Block blockbelow = world.getBlockState(blockPos.add(0, -1, 0)).getBlock();
                            if (blockbelow == Blocks.stone) {
                                higherToLower = false;
                                foundChest = true;
                            } else if (blockbelow == Blocks.air) {
                                higherToLower = true;
                                foundChest = true;
                            } else {
                                return;
                            }
                        }
                    }
                }).start();
            }
        }
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        if (Utils.inDungeons && SkyblockFeatures.config.blazeSolver) {
            if (foundChest) {
                // Low-High
                blazes.sort(Comparator.comparingInt(a -> a.health));

                
                Entity currentBlaze = null;
                Entity nextBlaze = null;
                try {
                    if(higherToLower) {
                        if(blazes.get(blazes.size()-1)!=null) currentBlaze = blazes.get(blazes.size()-1).EntityBlaze;
                        if(blazes.get(blazes.size()-2)!=null) nextBlaze = blazes.get(blazes.size()-2).EntityBlaze;
                    } else {
                        if(blazes.get(0)!=null) currentBlaze = blazes.get(0).EntityBlaze;
                        if(blazes.get(0)!=null) nextBlaze = blazes.get(1).EntityBlaze;
                    }
                } catch (Exception e) {
                    // TODO: handle exception
                }

                // Highlight current blaze
                if(currentBlaze !=null) {
                    AxisAlignedBB aabb = new AxisAlignedBB(currentBlaze.posX - 0.5, currentBlaze.posY - 2, currentBlaze.posZ - 0.5, currentBlaze.posX + 0.5, currentBlaze.posY, currentBlaze.posZ + 0.5);
                    RenderUtil.drawOutlinedFilledBoundingBox(aabb, new Color(0x00FF00), event.partialTicks);
                }
                // Highlight next blaze
                if(nextBlaze !=null) {
                    AxisAlignedBB aabb = new AxisAlignedBB(nextBlaze.posX - 0.5, nextBlaze.posY - 2, nextBlaze.posZ - 0.5, nextBlaze.posX + 0.5, nextBlaze.posY, nextBlaze.posZ + 0.5);
                    RenderUtil.drawOutlinedFilledBoundingBox(aabb, new Color(0xFFFF00), event.partialTicks);
                }
                // Draw line to next blaze
                if(nextBlaze !=null && currentBlaze !=null && nextBlaze.getPositionVector() !=null && currentBlaze.getPositionVector()!=null) {
                    RenderUtil.draw3DLine(currentBlaze.getPositionVector(), nextBlaze.getPositionVector(), 0, new Color(0x00FFFF), event.partialTicks);
                }
            }
        }
    }
}