package mrfast.sbf.features.items;

import com.mojang.realmsclient.gui.ChatFormatting;
import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.events.UseItemAbilityEvent;
import mrfast.sbf.utils.RenderUtil;
import mrfast.sbf.utils.Utils;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FireFreezeDisplay {
    Vec3 activatedPos;
    Long activatedAt;
    @SubscribeEvent
    public void onItemUse(UseItemAbilityEvent event) {
        if(SkyblockFeatures.config.fireFreezeStaffFreezeTimer && event.ability.itemId.equals("FIRE_FREEZE_STAFF")) {
            activatedAt = System.currentTimeMillis();
            activatedPos = Utils.GetMC().thePlayer.getPositionVector();
        }
    }
    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if(activatedPos!=null && SkyblockFeatures.config.fireFreezeStaffFreezeTimer) {
            String seconds = Utils.msToSeconds(5000-(System.currentTimeMillis()-activatedAt),1);
            RenderUtil.draw3DStringWithShadow(activatedPos.addVector(0,1,0), ChatFormatting.YELLOW+seconds,event.partialTicks);

            if(System.currentTimeMillis()-activatedAt>5000) {
                activatedAt = null;
                activatedPos = null;
            }
        }
    }
}
