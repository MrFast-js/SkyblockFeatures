package mrfast.skyblockfeatures.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiChest;
import mrfast.skyblockfeatures.SkyblockFeatures;
import mrfast.skyblockfeatures.utils.Utils;

@Mixin(Gui.class)
public abstract class MixinGui {
    @Inject(method = "drawGradientRect", at = @At(value = "HEAD"), cancellable = true)
    private void connect(int left, int top, int right, int bottom, int startColor, int endColor, CallbackInfo ci) {
        if(SkyblockFeatures.config.hideWhiteSquare && Utils.GetMC().theWorld != null && startColor==-2130706433) {
            ci.cancel();
        }
    }
}
