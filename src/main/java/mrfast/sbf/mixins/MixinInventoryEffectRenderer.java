package mrfast.sbf.mixins;

import net.minecraft.client.renderer.InventoryEffectRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.utils.Utils;

/**
 * Taken from NotEnoughUpdates under GNU LESSER GENERAL PUBLIC LICENSE
 * https://github.com/Moulberry/NotEnoughUpdates/blob/master/COPYING
 *
 * @author Moulberry
 */
@Mixin({InventoryEffectRenderer.class})
public class MixinInventoryEffectRenderer {

    @ModifyVariable(method="updateActivePotionEffects", at=@At(value="STORE"))
    public boolean hasVisibleEffect_updateActivePotionEffects(boolean hasVisibleEffect) {
        if(SkyblockFeatures.config.hidepotion && Utils.inSkyblock) {
            return false;
        } else {
            return hasVisibleEffect;
        }
    }

}
