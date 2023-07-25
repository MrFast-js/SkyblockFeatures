package mrfast.skyblockfeatures.mixins;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import mrfast.skyblockfeatures.SkyblockFeatures;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

/**
 * Modified from OldAnimations under GNU Lesser General Public License v3.0
 * https://github.com/Sk1erLLC/OldAnimations/blob/master/LICENSE
 *
 * @author Sk1erLLC
 */
@Mixin({ LayerHeldItem.class })
public abstract class MixinLayerHeldItem {

    @Shadow
    @Final
    private RendererLivingEntity<?> livingEntityRenderer;

    @Inject(method = { "doRenderLayer" }, at = { @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelBiped;postRenderArm(F)V", ordinal = 0) }, cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void doRenderLayer(final EntityLivingBase entitylivingbaseIn, final float p_177141_2_, final float p_177141_3_, final float partialTicks, final float p_177141_5_, final float p_177141_6_, final float p_177141_7_, final float scale, final CallbackInfo ci, ItemStack itemstack) {
        if (entitylivingbaseIn instanceof EntityPlayer) {
            if (SkyblockFeatures.config.oldAnimations) { // block
                if (((EntityPlayer)entitylivingbaseIn).isBlocking()) {
                    if (entitylivingbaseIn.isSneaking()) {
                        ((ModelBiped)this.livingEntityRenderer.getMainModel()).postRenderArm(0.0325f);
                        GlStateManager.scale(1.05f, 1.05f, 1.05f);
                        GlStateManager.translate(-0.58f, 0.32f, -0.07f);
                        GlStateManager.rotate(-24405.0f, 137290.0f, -2009900.0f, -2654900.0f);
                    }
                    else {
                        ((ModelBiped)this.livingEntityRenderer.getMainModel()).postRenderArm(0.0325f);
                        GlStateManager.scale(1.05f, 1.05f, 1.05f);
                        GlStateManager.translate(-0.45f, 0.25f, -0.07f);
                        GlStateManager.rotate(-24405.0f, 137290.0f, -2009900.0f, -2654900.0f);
                    }
                }
                else {
                    ((ModelBiped)this.livingEntityRenderer.getMainModel()).postRenderArm(0.0625f);
                }
            }
            else {
                ((ModelBiped)this.livingEntityRenderer.getMainModel()).postRenderArm(0.0625f);
            }
            GlStateManager.translate(-0.0625f, 0.4375f, 0.0625f);
            if (((EntityPlayer)entitylivingbaseIn).fishEntity != null) {
                itemstack = new ItemStack((Item) Items.fishing_rod, 0);
            }
        }
        else {
            ((ModelBiped)this.livingEntityRenderer.getMainModel()).postRenderArm(0.0625f);
            GlStateManager.translate(-0.0625f, 0.4375f, 0.0625f);
        }
        final Item item = itemstack.getItem();
        final Minecraft minecraft = Minecraft.getMinecraft();
        if (item instanceof ItemBlock && Block.getBlockFromItem(item).getRenderType() == 2) {
            GlStateManager.translate(0.0f, 0.1875f, -0.3125f);
            GlStateManager.rotate(20.0f, 1.0f, 0.0f, 0.0f);
            GlStateManager.rotate(45.0f, 0.0f, 1.0f, 0.0f);
            final float f1 = 0.375f;
            GlStateManager.scale(-f1, -f1, f1);
        }
        if (entitylivingbaseIn.isSneaking()) {
            GlStateManager.translate(0.0f, 0.203125f, 0.0f);
        }
        minecraft.getItemRenderer().renderItem(entitylivingbaseIn, itemstack, ItemCameraTransforms.TransformType.THIRD_PERSON);
        GlStateManager.popMatrix();
        ci.cancel();
    }
}