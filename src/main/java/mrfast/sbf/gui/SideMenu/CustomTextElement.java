package mrfast.sbf.gui.SideMenu;

import mrfast.sbf.utils.Utils;

public class CustomTextElement extends CustomElement {
    private String text;

    public CustomTextElement(int x, int y, String text, String hoverText, Runnable onClickAction) {
        super(x, y, Utils.GetMC().fontRendererObj.getStringWidth(text), Utils.GetMC().fontRendererObj.FONT_HEIGHT, hoverText, onClickAction);
        this.text = text;
    }

    @Override
    public void render() {
        // Render the text
        Utils.GetMC().fontRendererObj.drawStringWithShadow(text, this.x + 2, this.y + 2, 0xFFFFFF);
    }
}

