package mrfast.skyblockfeatures.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.Item;
import mrfast.skyblockfeatures.SkyblockFeatures;
import mrfast.skyblockfeatures.utils.Utils;

/**
 * Modified from OldAnimations under GNU Lesser General Public License v3.0
 * https://github.com/Sk1erLLC/OldAnimations/blob/master/LICENSE
 *
 * @author Sk1erLLC
 */
@Mixin(ItemRenderer.class)
public class MixinItemRenderer {
    
    @Shadow
    @Final
    private Minecraft mc;
    private float swingProgress;

    @Inject(
        method = "Lnet/minecraft/client/renderer/ItemRenderer;renderItemInFirstPerson(F)V",
        at = @At(
            value = "INVOKE",
            shift = At.Shift.AFTER,
            target = "Lnet/minecraft/client/renderer/GlStateManager;pushMatrix()V"
        )
    )
    public void transformFirstPersonItem(CallbackInfo ci) {
        if(Utils.GetMC().thePlayer.getHeldItem() != null) {
            GlStateManager.translate(SkyblockFeatures.config.armX*0.01, SkyblockFeatures.config.armY*0.01, SkyblockFeatures.config.armZ*0.01);
        }
    }

    @Inject(method = "renderItemInFirstPerson", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getItemUseAction()Lnet/minecraft/item/EnumAction;", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void modifySwingProgress(float partialTicks, CallbackInfo ci, float f, AbstractClientPlayer player, float f1, float f2, float f3) {
        if (SkyblockFeatures.config.oldAnimations) {
            this.swingProgress = f1;
        }
    }

    @Inject(method = "renderItemInFirstPerson", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemRenderer;doBlockTransformations()V", shift = At.Shift.AFTER))
    private void modifySwing(float partialTicks, CallbackInfo ci) {
        if (SkyblockFeatures.config.oldAnimations) { // block hit
            GlStateManager.scale(0.83f, 0.88f, 0.85f);
            GlStateManager.translate(-0.3f, 0.1f, 0.0f);
        }
    }

    @Inject(method = "renderItemInFirstPerson", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemRenderer;transformFirstPersonItem(FF)V", ordinal = 1, shift = At.Shift.AFTER))
    private void modifyEat(float partialTicks, CallbackInfo ci) {
        if (SkyblockFeatures.config.oldAnimations) { // eating
            GlStateManager.scale(0.8f, 1.0f, 1.0f);
            GlStateManager.translate(-0.2f, -0.1f, 0.0f);
        }
    }


    @ModifyArg(method = "renderItemInFirstPerson", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemRenderer;transformFirstPersonItem(FF)V", ordinal = 1), index = 1)
    private float drinkSwingProgress(float swingProgress) {
        return SkyblockFeatures.config.oldAnimations ? this.swingProgress : 0.0f; //eating
    }

    @ModifyArg(method = "renderItemInFirstPerson", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemRenderer;transformFirstPersonItem(FF)V", ordinal = 2), index = 1)
    private float blockSwingProgress(float swingProgress) {
        return SkyblockFeatures.config.oldAnimations ? this.swingProgress : 0.0f; // block hit
    }

    @ModifyArg(method = "renderItemInFirstPerson", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemRenderer;transformFirstPersonItem(FF)V", ordinal = 3), index = 1)
    private float bowSwingProgress(float swingProgress) {
        return SkyblockFeatures.config.oldAnimations ? this.swingProgress : 0.0f; // bow
    }

    @Inject(method = "transformFirstPersonItem", at = {@At("HEAD")})
    private void transformFirstPersonItem(float equipProgress, float swingProgress, CallbackInfo ci) {
        if (SkyblockFeatures.config.oldAnimations && this.mc != null && this.mc.thePlayer != null && this.mc.thePlayer.getItemInUse() != null && this.mc.thePlayer.getItemInUse().getItem() != null && Item.getIdFromItem(this.mc.thePlayer.getItemInUse().getItem()) == 261) { // bow
            GlStateManager.translate(-0.01f, 0.05f, -0.06f);
        }
        if (SkyblockFeatures.config.oldAnimations && this.mc != null && this.mc.thePlayer != null && this.mc.thePlayer.getCurrentEquippedItem() != null && this.mc.thePlayer.getCurrentEquippedItem().getItem() != null && (Item.getIdFromItem(this.mc.thePlayer.getCurrentEquippedItem().getItem()) == 346 || Item.getIdFromItem(this.mc.thePlayer.getCurrentEquippedItem().getItem()) == 398)) { // rod
            GlStateManager.translate(0.08f, -0.027f, -0.33f);
            GlStateManager.scale(0.93f, 1.0f, 1.0f);
        }
        if (SkyblockFeatures.config.oldAnimations && this.mc != null && this.mc.thePlayer != null && this.mc.thePlayer.isSwingInProgress  && this.mc.thePlayer.getCurrentEquippedItem() != null && !this.mc.thePlayer.isEating() && !this.mc.thePlayer.isBlocking()) { // swing
            GlStateManager.scale(0.85f, 0.85f, 0.85f);
            GlStateManager.translate(-0.078f, 0.003f, 0.05f);
        }
    }

    
}