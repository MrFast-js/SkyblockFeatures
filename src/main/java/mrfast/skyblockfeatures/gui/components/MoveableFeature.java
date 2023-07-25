package mrfast.skyblockfeatures.gui.components;
import java.awt.Color;

import mrfast.skyblockfeatures.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

public class MoveableFeature extends GuiButton {

    public UIElement element;
    public float x;
    public float y;
    public float xWidth;
    public float yHeight;

    public MoveableFeature(UIElement element) {
        super(-1, 0, 0, null);
        this.element = element;
        updateLocations();
    }

    public MoveableFeature(int buttonId, UIElement element) {
        super(-1, 0, 0, null);
        this.element = element;
    }

    private void updateLocations() {
        x = element.getX();
        y = element.getY();
        xWidth = x + element.getWidth();
        yHeight = y + element.getHeight();
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        updateLocations();
        hovered = mouseX >= x && mouseY >= y && mouseX < xWidth && mouseY < yHeight;
        Color c = new Color(255, 255, 255, hovered ? 100 : 40);
        Utils.drawGraySquare(0, 0, element.getWidth() + 4, element.getHeight() + 4, 3, c);

        GlStateManager.pushMatrix();
        GlStateManager.translate(2, 2, 0);
        element.drawElementExample();
        GlStateManager.translate(-2, -2, 0);
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