package mrfast.sbf.gui.SideMenu;

import mrfast.sbf.utils.Utils;

public class CustomTextElement extends CustomElement {
    private String text;
    private boolean shadow = false;

    public CustomTextElement(int x, int y, String text, String hoverText, Runnable onClickAction,boolean shadow) {
        super(x, y, Utils.GetMC().fontRendererObj.getStringWidth(text), Utils.GetMC().fontRendererObj.FONT_HEIGHT, hoverText, onClickAction);
        this.text = text;
        this.shadow=shadow;
    }

    @Override
    public void render() {
        // Render the text
        Utils.GetMC().fontRendererObj.drawString(text, this.x + 2, this.y + 2, 0xFFFFFF,shadow);
    }
}

