package mrfast.sbf.mixins.transformers;

import mrfast.sbf.utils.EntityOutlineRenderer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

/**
 * Adapted from Skyhanni under GNU LGPL v2.1 license
 * @link https://github.com/hannibal002/SkyHanni/blob/beta/LICENSE
 */
@Mixin(RenderGlobal.class)
public abstract class MixinRenderGlobal {
    @Shadow protected abstract boolean isRenderEntityOutlines();

    @Redirect(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderGlobal;isRenderEntityOutlines()Z", ordinal = 0))
    private boolean onRenderEntities(RenderGlobal renderGlobal) {
        return false;
    }

    // Remove condition by always returning true
    @Redirect(method = "isRenderEntityOutlines", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;isSpectator()Z", ordinal = 0))
    private boolean isSpectatorDisableCheck(EntityPlayerSP entityPlayerSP) {
        return true;
    }

    @Redirect(method = "isRenderEntityOutlines", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/settings/KeyBinding;isKeyDown()Z", ordinal = 0))
    private boolean isKeyDownDisableCheck(KeyBinding keyBinding) {
        return EntityOutlineRenderer.shouldRenderEntityOutlines();
    }

    @Inject(method = "renderEntities", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V", shift = At.Shift.BEFORE, ordinal = 2, args = {"ldc=entities"}), locals = LocalCapture.CAPTURE_FAILSOFT) // Non-optifine version
    private void renderEntities(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci, int pass, double d0, double d1, double d2) {
        displayOutlines(d0, d1, d2, camera, partialTicks);
    }

    private void displayOutlines(double x, double y, double z, ICamera camera, float partialTicks) {
        if (isRenderEntityOutlines()) {
            EntityOutlineRenderer.renderEntityOutlines(camera,partialTicks,x,y,z);
        }
    }
}