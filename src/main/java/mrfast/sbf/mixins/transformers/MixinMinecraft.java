package mrfast.sbf.mixins.transformers;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Redirect(method = "startGame", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;fontRendererObj:Lnet/minecraft/client/gui/FontRenderer;"))
    public void startFontRenderer(Minecraft instance, FontRenderer value) {
        if(SkyblockFeatures.config.customFont) {
            Utils.GetMC().fontRendererObj = new FontRenderer(Utils.GetMC().gameSettings, new ResourceLocation("skyblockfeatures","font/ascii.png"), Utils.GetMC().renderEngine, false);
        } else {
            Utils.GetMC().fontRendererObj = new FontRenderer(Utils.GetMC().gameSettings, new ResourceLocation("textures/font/ascii.png"), Utils.GetMC().renderEngine, false);
        }
    }
}
