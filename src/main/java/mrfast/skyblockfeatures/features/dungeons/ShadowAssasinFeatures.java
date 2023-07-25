package mrfast.skyblockfeatures.features.dungeons;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.skyblockfeatures.SkyblockFeatures;
import mrfast.skyblockfeatures.gui.GuiManager;
import mrfast.skyblockfeatures.utils.RenderUtil;
import mrfast.skyblockfeatures.utils.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ShadowAssasinFeatures {
    List<EntityPlayer> shadowAssassins = new ArrayList<>();
    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        if(Utils.GetMC().theWorld==null || !(SkyblockFeatures.config.shadowAssassinNotify || SkyblockFeatures.config.boxShadowAssasins)) return;
        for(EntityPlayer entity:Utils.GetMC().theWorld.playerEntities) {
            if(entity.getName().equals("Shadow Assassin")) {
                boolean unspawned = entity.isInvisible();

                if(unspawned && SkyblockFeatures.config.boxShadowAssasins) {
                    RenderUtil.drawOutlinedFilledBoundingBox(entity.getEntityBoundingBox(), new Color(0x8000FF), 0);
                }
                if(SkyblockFeatures.config.shadowAssassinNotify && unspawned && !shadowAssassins.contains(entity) && Utils.GetMC().thePlayer.getDistanceToEntity(entity)<16) {
                    GuiManager.createTitle(ChatFormatting.LIGHT_PURPLE+"Shadow Assasin", 20);
                    shadowAssassins.add(entity);
                }
            }
        }
    }

    @SubscribeEvent
    public void onLoad(WorldEvent.Load event) {
        shadowAssassins.clear();
    }
}
