package mrfast.sbf.features.dungeons.solvers;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.utils.RenderUtil;
import mrfast.sbf.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ThreeWeirdosSolver {
    String[] riddleAnswers = {"The reward is not in my chest!",
                              "At least one of them is lying, and the reward is not in",
                              "My chest doesn't have the reward. We are all telling the truth",
                              "My chest has the reward and I'm telling the truth",
                              "The reward isn't in any of our chests",
                              "Both of them are telling the truth."};
    BlockPos answerChest = null;
    List<BlockPos> checking = new ArrayList<>();

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        checking.clear();
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onChatMesaage(ClientChatReceivedEvent event) {
        if (!Utils.inDungeons || event.type == 2) return;
        String message = Utils.cleanColor(event.message.getUnformattedText());
        // §e[NPC] §cLino§f: The reward is in my chest!
        for(String riddleAnswer:riddleAnswers) {
            if(!message.contains(riddleAnswer)) continue;
            
            String npcName = message.substring(6, message.indexOf(":"));
            Utils.SendMessage(npcName+ChatFormatting.RESET+" has the reward!");
            for(Entity entity:Utils.GetMC().theWorld.loadedEntityList) {
                if(!(entity instanceof EntityArmorStand)) continue;
                if(!entity.getCustomNameTag().contains(npcName)) continue;
                
                for(TileEntity tileEntity:Utils.GetMC().theWorld.loadedTileEntityList) {
                    Utils.SendMessage("Got thing");
                    BlockPos pos = tileEntity.getPos();
                    int[][] directionOffsets = {
                        {0, 1},    // North
                        {-1, 0},   // West
                        {1, 0},    // East
                        {0, -1},   // South
                    };
                    for (int[] offset : directionOffsets) {

                        int xOffset = offset[0];
                        int zOffset = offset[1];
            
                        BlockPos blockPos = pos.add(xOffset, 0, zOffset);
                        Utils.SendMessage("Doing offset: "+blockPos.toString());

                        checking.add(blockPos);
                        Block block = Utils.GetMC().theWorld.getBlockState(blockPos).getBlock();
                        if(block instanceof BlockChest) {
                            answerChest=blockPos;
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        if (!Utils.inDungeons || answerChest==null) return;
        AxisAlignedBB aabb = new AxisAlignedBB(answerChest, answerChest.add(1, 1, 1));
        RenderUtil.drawOutlinedFilledBoundingBox(aabb,Color.CYAN,event.partialTicks);
        for(BlockPos pos:checking) {
            AxisAlignedBB aabb2 = new AxisAlignedBB(pos, pos.add(1, 1, 1));
            RenderUtil.drawOutlinedFilledBoundingBox(aabb2,Color.RED,event.partialTicks);
        }
    }
}
