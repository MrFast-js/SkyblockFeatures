package mrfast.sbf.gui.components;
import java.awt.Color;

import mrfast.sbf.utils.GuiUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

public class MoveableFeature extends GuiButton {

    public UIElement element;
    public float x;
    public float y;
    public float Width;
    public float Height;

    public MoveableFeature(UIElement element) {
        super(-1, 0, 0, null);
        this.element = element;
        updateLocations();
    }

    private void updateLocations() {
        x = element.getX();
        y = element.getY();
        Width = element.getWidth();
        Height = element.getHeight();
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        updateLocations();
        ScaledResolution sr = new ScaledResolution(mc);

        int screenWidth = sr.getScaledWidth();
        int screenHeight = sr.getScaledHeight();
        float actualX = (screenWidth*x)-2;
        float actualY = (screenHeight*y)-2;
        float xWidth = actualX+element.getWidth()+4;
        float yHeight = actualY+element.getHeight()+4;

        hovered = mouseX >= actualX && mouseY >= actualY && mouseX < xWidth && mouseY < yHeight;
        GuiUtils.drawGraySquare(-2, -2, element.getWidth() + 4, element.getHeight() + 4, hovered);

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