package mrfast.sbf.mixins.transformers;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.API.ItemAbilityAPI;
import mrfast.sbf.utils.ItemUtils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(RenderItem.class)
public abstract class MixinRenderItem {
    @Inject(method = "renderItemOverlayIntoGUI", at = @At("HEAD"))
    private void onRenderItemOverlayIntoGUI(FontRenderer fr, ItemStack stack, int xPosition, int yPosition, String text, CallbackInfo info) {
        if (stack != null && SkyblockFeatures.config.cooldownTracker) {
            String id = ItemUtils.getSkyBlockItemID(stack);
            if(id!=null && ItemAbilityAPI.activeCooldowns.containsKey(id)) {
                ItemAbilityAPI.CooldownItem cooldownItem = ItemAbilityAPI.activeCooldowns.get(id);
                double fillPercent = getFillPercent(cooldownItem);
                int type = SkyblockFeatures.config.cooldownTrackerType;
                if(fillPercent>1) return;
                if(type==0) {
                    Color backgroundColor = new Color(SkyblockFeatures.config.cooldownTrackerSquareColor.getRed(), SkyblockFeatures.config.cooldownTrackerSquareColor.getGreen(), SkyblockFeatures.config.cooldownTrackerSquareColor.getBlue(), 100);
                    Gui.drawRect(xPosition, (int) (yPosition + (16 * fillPercent)), xPosition + 16, yPosition + 16, backgroundColor.getRGB());
                }
                if(type==1) {
                    int j = (int) Math.round(13.0 - (1 - fillPercent) * 13.0);
                    int i = (int) Math.round(255.0 - (1 - fillPercent) * 255.0);
                    GlStateManager.disableLighting();
                    GlStateManager.disableDepth();
                    GlStateManager.disableTexture2D();
                    GlStateManager.disableAlpha();
                    GlStateManager.disableBlend();
                    Tessellator tessellator = Tessellator.getInstance();
                    WorldRenderer worldrenderer = tessellator.getWorldRenderer();
                    Color color = SkyblockFeatures.config.cooldownTrackerBarColor;
                    this.draw(worldrenderer, xPosition + 2, yPosition + 13, 13, 2, 0, 0, 0, 255);
                    this.draw(worldrenderer, xPosition + 2, yPosition + 13, j, 1, color.getRed(), color.getGreen(), color.getBlue(), 255);
                    GlStateManager.enableAlpha();
                    GlStateManager.enableTexture2D();
                    GlStateManager.enableLighting();
                    GlStateManager.enableDepth();
                }
            }
        }
    }

    @Unique
    private static double getFillPercent(ItemAbilityAPI.CooldownItem cooldownItem) {
        double fillPercent = 0;
        if(cooldownItem.leftClick!=null) {
            fillPercent = cooldownItem.leftClick.currentCount/ cooldownItem.leftClick.cooldownSeconds;
        }
        if(cooldownItem.rightClick!=null) {
            fillPercent = cooldownItem.rightClick.currentCount/ cooldownItem.rightClick.cooldownSeconds;
        }
        if(cooldownItem.sneakLeftClick!=null) {
            fillPercent = cooldownItem.sneakLeftClick.currentCount/ cooldownItem.sneakLeftClick.cooldownSeconds;
        }
        if(cooldownItem.sneakRightClick!=null) {
            fillPercent = cooldownItem.sneakRightClick.currentCount/ cooldownItem.sneakRightClick.cooldownSeconds;
        }
        return fillPercent;
    }

    @Unique
    private void draw(WorldRenderer renderer, int x, int y, int width, int height, int red, int green, int blue, int alpha) {
        renderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        renderer.pos(x, y, 0.0).color(red, green, blue, alpha).endVertex();
        renderer.pos(x, y + height, 0.0).color(red, green, blue, alpha).endVertex();
        renderer.pos(x + width, y + height, 0.0).color(red, green, blue, alpha).endVertex();
        renderer.pos(x + width, y, 0.0).color(red, green, blue, alpha).endVertex();
        Tessellator.getInstance().draw();
    }
}
