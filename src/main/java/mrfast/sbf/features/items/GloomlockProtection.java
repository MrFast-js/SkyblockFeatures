package mrfast.sbf.features.items;

import com.mojang.realmsclient.gui.ChatFormatting;
import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.events.UseItemAbilityEvent;
import mrfast.sbf.utils.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class GloomlockProtection {
    @SubscribeEvent
    public void onItemAbility(UseItemAbilityEvent event) {
        if(!SkyblockFeatures.config.gloomlockGrimoireProtection) return;

        if(event.ability.abilityName.equals("Life Tap") && event.ability.itemId.equals("GLOOMLOCK_GRIMOIRE")) {
            int currentHealth = Utils.health;
            int maxHealth = Utils.maxHealth;
            if(currentHealth<=Math.round((float) maxHealth *0.25)) {
                Utils.sendMessage(ChatFormatting.RED+"This ability was blocked due to < 25% health");
                event.setCanceled(true);
            }
        }
    }
}
