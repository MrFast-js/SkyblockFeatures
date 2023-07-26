package mrfast.sbf.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Shadow private Minecraft mc;

    @Inject(method = "hurtCameraEffect", at = @At("HEAD"), cancellable = true)
    private void onHurtcam(float partialTicks, CallbackInfo ci) {
        if (Utils.inSkyblock && SkyblockFeatures.config.noHurtcam) ci.cancel();
    }

    /**
     * Modified from OldAnimations under GNU Lesser General Public License v3.0
     * https://github.com/Sk1erLLC/OldAnimations/blob/master/LICENSE
     *
     * @author Sk1erLLC
     */
    @Inject(method = "renderHand", at = @At("TAIL"))
    private void renderHand(float partialTicks, int xOffset, CallbackInfo ci) {
        if (SkyblockFeatures.config.oldAnimations) {
            if (this.mc.thePlayer.getItemInUseCount() != 0 && this.mc.gameSettings.keyBindAttack.isKeyDown() && this.mc.gameSettings.keyBindUseItem.isKeyDown() && this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit.equals(MovingObjectPosition.MovingObjectType.BLOCK)) {
                this.swingItem(this.mc.thePlayer);
            }
        }
    }

    private void swingItem(EntityLivingBase entity) {
        ItemStack stack = entity.getHeldItem();
        if (stack != null && stack.getItem() != null && (!entity.isSwingInProgress || entity.swingProgressInt >= this.getArmSwingAnimationEnd(entity) / 2 || entity.swingProgressInt < 0)) {
            entity.swingProgressInt = -1;
            entity.isSwingInProgress = true;
        }
    }

    private int getArmSwingAnimationEnd(EntityLivingBase e) {
        return e.isPotionActive(Potion.digSpeed) ? (6 - (1 + e.getActivePotionEffect(Potion.digSpeed).getAmplifier())) : (e.isPotionActive(Potion.digSlowdown) ? (6 + (1 + e.getActivePotionEffect(Potion.digSlowdown).getAmplifier()) * 2) : 6);
    }
    

}
