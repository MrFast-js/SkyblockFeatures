package mrfast.sbf.features.overlays;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class DamageOverlays {
    @SubscribeEvent
    public void onRenderHealth(RenderGameOverlayEvent.Post event) {
        if (event.type == RenderGameOverlayEvent.ElementType.HEALTH && Utils.inSkyblock && SkyblockFeatures.config.damagetint && Utils.GetMC().thePlayer!=null) {
            try {
                GlStateManager.pushMatrix();
                renderTint(Utils.Health, new ScaledResolution(Utils.GetMC())); 
                GlStateManager.popMatrix();
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }
    
    private void renderTint(float currentHealth, ScaledResolution resolution) {
        float threshold = (float) Utils.maxHealth /2;
        if (currentHealth <= threshold) {
            float f = (threshold - currentHealth) / threshold + 1.0F / threshold * 2.0F;
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);
            GlStateManager.tryBlendFuncSeparate(0, 769, 1, 0);
            GlStateManager.color(0.0F, f, f, 1.0F);
            Utils.GetMC().getTextureManager().bindTexture(tint);
            WorldRenderer worldRenderer = tessellator.getWorldRenderer();
            worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldRenderer.pos(0.0D, resolution.getScaledHeight(), -90.0D).tex(0.0D, 1.0D).endVertex();
            worldRenderer.pos(resolution.getScaledWidth(), resolution.getScaledHeight(), -90.0D).tex(1.0D, 1.0D).endVertex();
            worldRenderer.pos(resolution.getScaledWidth(), 0.0D, -90.0D).tex(1.0D, 0.0D).endVertex();
            worldRenderer.pos(0.0D, 0.0D, -90.0D).tex(0.0D, 0.0D).endVertex();
            tessellator.draw();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix(); 
        }
    }

    private static final Tessellator tessellator = Tessellator.getInstance();
    private static final ResourceLocation tint = new ResourceLocation("skyblockfeatures:gui/vignette.png");
}
