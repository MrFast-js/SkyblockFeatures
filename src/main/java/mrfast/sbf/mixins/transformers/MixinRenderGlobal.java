package mrfast.sbf.mixins.transformers;

import mrfast.sbf.mixins.hooks.RenderGlobalHook;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


/**
 * Adapted from Skyhanni under GNU LGPL v2.1 license
 * @link https://github.com/hannibal002/SkyHanni/blob/beta/LICENSE
 */
@Mixin(RenderGlobal.class)
public abstract class MixinRenderGlobal {
    @Unique
    private final RenderGlobalHook skyblockFeatures$hook = new RenderGlobalHook();

    @Inject(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderGlobal;isRenderEntityOutlines()Z"))
    public void renderEntitiesOutlines(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci) {
        skyblockFeatures$hook.renderEntitiesOutlines(camera, partialTicks);
    }

    @Inject(method = "isRenderEntityOutlines", at = @At(value = "HEAD"), cancellable = true)
    public void isRenderEntityOutlinesWrapper(CallbackInfoReturnable<Boolean> cir) {
        skyblockFeatures$hook.shouldRenderEntityOutlines(cir);
    }

    @Inject(method = "renderEntityOutlineFramebuffer", at = @At(value = "RETURN"))
    public void afterFramebufferDraw(CallbackInfo callbackInfo) {
        GlStateManager.enableDepth();
    }
}