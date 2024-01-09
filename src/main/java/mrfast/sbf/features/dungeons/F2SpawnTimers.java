package mrfast.sbf.features.dungeons;

import com.mojang.realmsclient.gui.ChatFormatting;
import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.utils.RenderUtil;
import mrfast.sbf.utils.Utils;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class F2SpawnTimers {
    boolean startCounting = false;
    double time = 7.75;
    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if(!SkyblockFeatures.config.floor2SpawnTimers) return;
        String clean = event.message.getUnformattedText();
        if(clean.equals("[BOSS] Scarf: If you can beat my Undeads, I'll personally grant you the privilege to replace them.")) {
            startCounting=true;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if(!SkyblockFeatures.config.floor2SpawnTimers) return;

        if(event.phase != TickEvent.Phase.START) return;

        if(startCounting) {
            time -= 0.05;
            if (time<=-5) {
                startCounting = false;
                time = 7.75;
            }
        }
    }

    BlockPos priestPos = new BlockPos(-29,71,-4);
    BlockPos warriorPos = new BlockPos(13,71,-4);
    BlockPos magePos = new BlockPos(13,71,-23);
    BlockPos archPos = new BlockPos(-29,71,-23);

    @SubscribeEvent
    public void onRender3d(RenderWorldLastEvent event) {
        if(!SkyblockFeatures.config.floor2SpawnTimers) return;

        if(startCounting) {
            if(time+0.3>0) {
                RenderUtil.renderWaypointText(priestPos,"Priest "+ChatFormatting.YELLOW+Utils.round(time+0.3,1)+"s");
            }
            if(time+0.2>0) {
                RenderUtil.renderWaypointText(warriorPos,"Warrior "+ChatFormatting.YELLOW+Utils.round(time+0.2,1)+"s");
            }
            if(time+0.4>0) {
                RenderUtil.renderWaypointText(magePos,"Mage "+ChatFormatting.YELLOW+Utils.round(time+0.4,1)+"s");
            }
            if(time+0.5>0) {
                RenderUtil.renderWaypointText(archPos,"Archer "+ChatFormatting.YELLOW+Utils.round(time+0.5,1)+"s");
            }
        }
    }
}
