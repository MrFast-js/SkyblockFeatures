package mrfast.sbf.mixins.transformers;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.Item;

@Mixin(ItemRenderer.class)
public class MixinItemRenderer {

    @Inject(method = "renderItemInFirstPerson(F)V",at = @At(value = "INVOKE",shift = At.Shift.AFTER, target = "Lnet/minecraft/client/renderer/GlStateManager;pushMatrix()V"))
    public void transformFirstPersonItem(CallbackInfo ci) {
        if(!SkyblockFeatures.config.smallItems) return;

        if(Utils.GetMC().thePlayer.getHeldItem() != null) {
            GlStateManager.translate(30*0.01, -5*0.01, -60*0.01);
        }
    }
}