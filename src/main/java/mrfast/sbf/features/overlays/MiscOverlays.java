package mrfast.sbf.features.overlays;

import java.text.SimpleDateFormat;
import java.util.*;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.features.overlays.maps.CrystalHollowsMap;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.GuiUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MiscOverlays {
    public static Minecraft mc = Utils.GetMC();


    @SubscribeEvent
    public void renderHealth(RenderGameOverlayEvent.Pre event) {
        if(Utils.inSkyblock) {
            if (event.type == RenderGameOverlayEvent.ElementType.FOOD && SkyblockFeatures.config.hideHungerBar) {
                event.setCanceled(true);
            }
            if (event.type == RenderGameOverlayEvent.ElementType.HEALTH && SkyblockFeatures.config.hideHealthHearts) {
                event.setCanceled(true);
            }
            if (event.type == RenderGameOverlayEvent.ElementType.ARMOR && SkyblockFeatures.config.hideArmorBar) {
                event.setCanceled(true);
            }
        }
    }
}
