package mrfast.sbf.mixins.transformers;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.utils.Utils;

@Mixin(LayerArmorBase.class)
public abstract class MixinLayerArmorBase<T extends ModelBase> implements LayerRenderer<EntityLivingBase> {

    @Inject(method = "doRenderLayer", at = @At("HEAD"), cancellable = true)
    private void onRenderAllArmor(EntityLivingBase entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale, CallbackInfo ci) {
        if (entitylivingbaseIn instanceof EntityPlayer && SkyblockFeatures.config.DisguisePlayersAs == 8 && SkyblockFeatures.config.playerDiguiser && !Utils.isNPC(entitylivingbaseIn) && Utils.inSkyblock) {
            ci.cancel();
        }
    }

}