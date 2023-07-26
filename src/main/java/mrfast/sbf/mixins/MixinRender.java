package mrfast.sbf.mixins;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.events.CheckRenderEntityEvent;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

@Mixin(Render.class)
public abstract class MixinRender<T extends Entity> {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void shouldRender(T livingEntity, ICamera camera, double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> cir) {
        if(!Utils.isNPC(livingEntity) && livingEntity.getDistanceToEntity(Utils.GetMC().thePlayer) > 49 && Utils.inSkyblock && SkyblockFeatures.config.HideFarEntity && !Utils.inDungeons) {
            cir.setReturnValue(false);
        }
        
        try {
            if (MinecraftForge.EVENT_BUS.post(new CheckRenderEntityEvent<T>(livingEntity, camera, camX, camY, camZ))) cir.setReturnValue(false);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Inject(method="renderLivingLabel",at=@At("HEAD"), cancellable = true)
    public void renderLivingLabel(T entityIn, String str, double x, double y, double z, int maxDistance, CallbackInfo ci) {
        if(!Utils.isNPC(entityIn) && entityIn instanceof EntityPlayer && SkyblockFeatures.config.hidePlayerNametags) {
            ci.cancel();
        }
        else if(SkyblockFeatures.config.hideAllNametags) {
            ci.cancel();
        }
    }
}
