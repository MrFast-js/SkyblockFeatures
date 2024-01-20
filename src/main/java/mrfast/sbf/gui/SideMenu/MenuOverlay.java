package mrfast.sbf.gui.SideMenu;

import mrfast.sbf.mixins.transformers.GuiContainerAccessor;
import mrfast.sbf.utils.GuiUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

public class MenuOverlay {

    private Map<String,CustomElement> elements;
    int x,y = 0;

    public Map<String,CustomElement> getElements() {
        return elements;
    }
    public boolean handleMouseMovement(int mouseX, int mouseY, GuiScreen gui) {
        for (CustomElement element : this.getElements().values()) {
            boolean result = element.doHoverRender(mouseX, mouseY, gui);
            if (result) {
                return true;
            }
        }
        return false;
    }
    public boolean handleKeyInput(char character, int key) {
        for (CustomElement element : this.getElements().values()) {
            if (element.isFocused()) {
                element.onKeyTyped(character, key);
            }
        }
        return false;
    }

    public MenuOverlay() {
        this.elements = new HashMap<>();
    }

    public void addOrUpdateElement(String name,CustomElement element) {
        element.parent = this;
        elements.put(name,element);
    }

    public void render(int x, int y, boolean drawSquare, int height, GuiContainerAccessor gui) {
        this.x=x;
        this.y=y;

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int maxWidth = 0;
        for (CustomElement element : elements.values()) {
            if(element.width>maxWidth) maxWidth=element.width;
        }

        GlStateManager.translate(x,y,340f);
        if(drawSquare) GuiUtils.drawGraySquareWithBorder(0,0,maxWidth+8,height);
        for (CustomElement element : elements.values()) {
            element.render();
        }
        GlStateManager.translate(-x,-y,-340f);
    }
}

