package mrfast.sbf.mixins.transformers;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiChest;

@Mixin(Gui.class)
public abstract class MixinGui {
    @Inject(method = "drawGradientRect", at = @At(value = "HEAD"), cancellable = true)
    private void connect(int left, int top, int right, int bottom, int startColor, int endColor, CallbackInfo ci) {
        if(SkyblockFeatures.config.hideWhiteSquare && Utils.GetMC().theWorld != null && startColor==-2130706433) {
            ci.cancel();
        }
    }
}
