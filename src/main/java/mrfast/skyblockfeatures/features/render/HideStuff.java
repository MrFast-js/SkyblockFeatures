package mrfast.skyblockfeatures.features.render;

import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import mrfast.skyblockfeatures.SkyblockFeatures;
import mrfast.skyblockfeatures.utils.Utils;

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