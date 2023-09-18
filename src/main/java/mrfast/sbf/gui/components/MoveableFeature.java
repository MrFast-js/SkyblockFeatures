package mrfast.sbf.gui.components;
import java.awt.Color;

import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
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

    public MoveableFeature(int buttonId, UIElement element) {
        super(-1, 0, 0, null);
        this.element = element;
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
        int screenWidth = Utils.GetMC().displayWidth;
        int screenHeight = Utils.GetMC().displayHeight;
        Float actualX =screenWidth*x;
        Float actualY =screenHeight*y;
        Float xWidth = actualX+element.getWidth();
        Float yHeight = actualY+element.getHeight();

        hovered = mouseX >= actualX && mouseY >= actualY && mouseX < xWidth && mouseY < yHeight;
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