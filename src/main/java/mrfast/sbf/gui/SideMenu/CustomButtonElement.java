package mrfast.sbf.gui.SideMenu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class CustomButtonElement extends CustomElement {
    private final ResourceLocation buttonTexture = new ResourceLocation("skyblockfeatures","gui/default_button.png");
    private String text;
    private int width,height = 0;
    public CustomButtonElement(int x, int y,String text, int width, int height, String hoverText, Runnable onClickAction) {
        super(x, y, width, height, hoverText, onClickAction);
        this.text = text;
        this.width=width;
        this.height=height;
    }

    @Override
    public void render() {
        GL11.glColor4f(1f,1f,1f,1f);

        // Draw button background (customize as needed)
        Minecraft.getMinecraft().getTextureManager().bindTexture(buttonTexture);

        int buttonWidth = width;  // Get button width
        int buttonHeight = height; // Get button height

        // Draw the button background
        Gui.drawModalRectWithCustomSizedTexture(this.x, this.y, 0, 0, buttonWidth, buttonHeight, buttonWidth, buttonHeight);

        // Calculate text position to center it in the button
        int textX = this.x + (buttonWidth - Minecraft.getMinecraft().fontRendererObj.getStringWidth(text)) / 2;
        int textY = this.y + (buttonHeight - Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT) / 2;

        // Draw the text in the middle of the button
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(text, textX, textY, Color.WHITE.getRGB());
    }
}
