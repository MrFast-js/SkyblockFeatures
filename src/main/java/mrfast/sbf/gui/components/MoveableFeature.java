package mrfast.sbf.gui.components;

import mrfast.sbf.gui.GuiManager;
import mrfast.sbf.utils.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

public class MoveableFeature extends GuiButton {

    public UIElement element;
    public float relativeX;
    public float relativeY;
    public float actualX;
    public float actualY;
    public boolean hovered = false;

    public MoveableFeature(UIElement element) {
        super(-1, 0, 0, null);
        this.element = element;
        updateLocations();
    }

    private void updateLocations() {
        relativeX = element.getX();
        relativeY = element.getY();
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        updateLocations();
        ScaledResolution sr = new ScaledResolution(mc);
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        float guiScaleFactor = (1f / scaledResolution.getScaleFactor()) * 2;
        int screenWidth = sr.getScaledWidth();
        int screenHeight = sr.getScaledHeight();
        float actualX = (screenWidth * relativeX) - 2;
        float actualY = (screenHeight * relativeY) - 2;
        float elementWidth = element.getWidth() * guiScaleFactor;
        float elementHeight = element.getHeight() * guiScaleFactor;
        if (!element.getRequirement() && !GuiManager.showAllEnabledElements) return;
        this.actualX = actualX;
        this.actualY = actualY;

        float xWidth = actualX + elementWidth + 4;
        float yHeight = actualY + elementHeight + 4;

        hovered = mouseX >= actualX && mouseY >= actualY && mouseX < xWidth && mouseY < yHeight;

        GuiUtils.drawGraySquare(-2, -2, (int) ((elementWidth + 4) / 2) * scaledResolution.getScaleFactor(), (int) ((elementHeight + 4) / 2) * scaledResolution.getScaleFactor(), hovered);

        GlStateManager.pushMatrix();
        element.drawElementExample();
        GlStateManager.popMatrix();
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        return enabled && visible && hovered;
    }

    public UIElement getElement() {
        return element;
    }
}