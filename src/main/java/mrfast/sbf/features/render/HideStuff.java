package mrfast.sbf.features.render;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.utils.Utils;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HideStuff {
	@SubscribeEvent
	public void renderHealth(RenderGameOverlayEvent.Pre event) {
		if(Utils.inSkyblock) {
			if (event.type == RenderGameOverlayEvent.ElementType.FOOD && SkyblockFeatures.config.hungerbar) {
				event.setCanceled(true);
			}
			if (event.type == RenderGameOverlayEvent.ElementType.HEALTH && SkyblockFeatures.config.healthsbar) {
				event.setCanceled(true);
			}
			if (event.type == RenderGameOverlayEvent.ElementType.ARMOR && SkyblockFeatures.config.armorbar) {
				event.setCanceled(true);
			}
		}
	}
}