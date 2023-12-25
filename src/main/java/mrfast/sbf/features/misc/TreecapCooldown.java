package mrfast.sbf.features.misc;

import java.awt.Color;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.events.BlockChangeEvent;
import mrfast.sbf.utils.ItemUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;

public class TreecapCooldown {
    public static double seconds = 2.0;
    private static final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onPlayerInteractEvent(BlockChangeEvent event) {
        if(!Utils.inSkyblock || mc.thePlayer.getHeldItem() == null || !SkyblockFeatures.config.treecapCooldown) return;

        if(mc.thePlayer.getHeldItem().getDisplayName().toLowerCase().contains("treecapitator") && event.getNew().getBlock() instanceof BlockAir && event.getOld().getBlock() instanceof BlockLog && Utils.GetMC().thePlayer.getDistanceSq(event.pos) < 20) {
            if(seconds<=0) {
                seconds = 2;
            }
        }
    }

    @SubscribeEvent
	public void onTick(RenderTickEvent event) {
		if(!Utils.inSkyblock || !SkyblockFeatures.config.treecapCooldown || Minecraft.getMinecraft().currentScreen != null) return;
		
		boolean hasTreecap = false;

        if(SkyblockFeatures.config.treecapHeld) {
            ItemStack stack = Utils.GetMC().thePlayer.getHeldItem();
            if(stack==null||ItemUtils.getSkyBlockItemID(stack)==null) return;
	 
            if(ItemUtils.getSkyBlockItemID(stack).equals("TREECAPITATOR_AXE")) {
                hasTreecap=true;
            }
        } else {
            for (int i = 0; i < 8; i++) {
                if (Utils.GetMC().thePlayer.inventory.mainInventory[i] == null) continue;
                ItemStack stack = Utils.GetMC().thePlayer.inventory.mainInventory[i];
		if(ItemUtils.getSkyBlockItemID(stack)==null) return;
                if(ItemUtils.getSkyBlockItemID(stack).equals("TREECAPITATOR_AXE")) {
                    hasTreecap=true;
                }
            }
        }


		if (hasTreecap) {
            ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
            int guiLeft = (sr.getScaledWidth() - 176) / 2;
            int guiTop = (sr.getScaledHeight() - 222) / 2;
            if (seconds>0) {
                float x = guiLeft + 85;
    			int y = guiTop + 120;
                if(seconds <= 10) x = guiLeft + 82.5f;

    			Minecraft.getMinecraft().fontRendererObj.drawString(Utils.round(seconds,1), x, y, new Color(255, 85, 85).getRGB(), true);
			} else {

                int x = guiLeft + 85;
    			int y = guiTop + 120;
    			
    			Minecraft.getMinecraft().fontRendererObj.drawString("✔", x, y, new Color(85, 255, 85).getRGB(), true);
			}
		}
	}

    @SubscribeEvent
    public void onSeconds(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START || !Utils.inSkyblock ) return;
        if (seconds > 0) {
            seconds-=0.05;
        }
    }

}
