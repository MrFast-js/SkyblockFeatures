package mrfast.sbf.features.dungeons.solvers;

import java.awt.Color;

import mrfast.sbf.utils.RenderUtil;
import mrfast.sbf.utils.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ThreeWeirdosSolver {
    String[] riddleAnswers = {"The reward is not in my chest!",
                              "At least one of them is lying, and the reward is not in",
                              "My chest doesn't have the reward. We are all telling the truth",
                              "My chest has the reward and I'm telling the truth",
                              "The reward isn't in any of our chests",
                              "Both of them are telling the truth."};
    TileEntity answerChest = null;
    
    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onChatMesaage(ClientChatReceivedEvent event) {
        if (!Utils.inDungeons || event.type == 2) return;
        String message = Utils.cleanColor(event.message.getUnformattedText());
        // §e[NPC] §cLino§f: The reward is in my chest!
        for(String riddleAnswer:riddleAnswers) {
            if(!message.contains(riddleAnswer)) continue;
          
            String npcName = message.substring(6, message.indexOf(":"));
            for(Entity entity:Utils.GetMC().theWorld.loadedEntityList) {
                if(!(entity instanceof EntityArmorStand && entity.getCustomNameTag().contains(npcName))) continue;
              
                for(TileEntity tileEntity:Utils.GetMC().theWorld.loadedTileEntityList) {
                    BlockPos pos = tileEntity.getPos();
                    Double dist = entity.getDistance(pos.getX(),pos.getY(),pos.getZ());
                    if(dist<0.75) {
                        answerChest = tileEntity;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        if (!Utils.inDungeons || answerChest==null) return;
        AxisAlignedBB aabb = new AxisAlignedBB(answerChest.getPos(), answerChest.getPos().add(1, 1, 1));
        RenderUtil.drawOutlinedFilledBoundingBox(aabb,Color.CYAN,event.partialTicks);
    }
}
