package mrfast.skyblockfeatures.features.misc;

import java.awt.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import mrfast.skyblockfeatures.SkyblockFeatures;
import mrfast.skyblockfeatures.events.SecondPassedEvent;
import mrfast.skyblockfeatures.utils.Utils;

public class ConjuringCooldown {
    public static int seconds = 25;
    public static  String display = EnumChatFormatting.BLUE + "Conjuring: " + EnumChatFormatting.GREEN + "Ready!";
    private static final Minecraft mc = Minecraft.getMinecraft();
    RenderManager renderManager = mc.getRenderManager();
    
    @SubscribeEvent
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if(!Utils.inSkyblock|| !SkyblockFeatures.config.ConjuringCooldown ||mc.thePlayer.getHeldItem() == null) return;
        if(mc.thePlayer.getHeldItem().getDisplayName().toLowerCase().contains("conjuring")) {
            if(ready) {
                seconds = 25;
                ready = false;
            }
        }
    }

    @SubscribeEvent
	public void getBone(RenderTickEvent event) {
		if(!Utils.inSkyblock || Minecraft.getMinecraft().currentScreen instanceof GuiScreen || !SkyblockFeatures.config.ConjuringCooldown) return;
		
		ItemStack item = Minecraft.getMinecraft().thePlayer.getHeldItem();
		
		if (item == null) return;
		
		if (item.getDisplayName().contains("Conjuring")) {
			
			if (!ready) {
    			ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
    			int guiLeft = (sr.getScaledWidth() - 176) / 2;
    			int guiTop = (sr.getScaledHeight() - 222) / 2;
    			
                float x = guiLeft + 82.5f;
    			int y = guiTop + (int) 120; 

                if(seconds <= 10) x = guiLeft + 85;
    			
    			Minecraft.getMinecraft().fontRendererObj.drawString(seconds+"", x, y, new Color(255, 85, 85).getRGB(), true);
			} else {
    			ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
    			int guiLeft = (sr.getScaledWidth() - 176) / 2;
    			int guiTop = (sr.getScaledHeight() - 222) / 2;
    			
    			int x = guiLeft + 85;
    			int y = guiTop + (int) 120;
    			
    			Minecraft.getMinecraft().fontRendererObj.drawString("✔", x, y, new Color(85, 255, 85).getRGB(), true);
			}
		}
	}

    static boolean ready = false;
    @SubscribeEvent
    public void onSeconds(SecondPassedEvent event) {
        if(!Utils.inSkyblock || !SkyblockFeatures.config.ConjuringCooldown) return;

        if (seconds < 26 && seconds > 0) {
            seconds--;
        }
        if (seconds == 0) {
            ready = true;
            return;
        }
    }

}
