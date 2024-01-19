package mrfast.sbf.gui.SideMenu;

import mrfast.sbf.mixins.transformers.GuiContainerAccessor;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.Map;

public class SideMenuManager {

    private static Map<GuiContainer, SideMenu> sidebarMap = new HashMap<>();

    public static SideMenu getOrCreateSidebar(GuiContainer gui) {
        if (!sidebarMap.containsKey(gui)) {
            sidebarMap.put(gui, new SideMenu());
        }
        return sidebarMap.get(gui);
    }

    @SubscribeEvent
    public void onGuiScreenDrawn(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (event.gui instanceof GuiContainer) {
            SideMenu sidebar = SideMenuManager.getOrCreateSidebar((GuiContainer) event.gui);
            // Draw sidebar hovering text
            for (CustomElement element : sidebar.getElements().values()) {
                element.doHoverRender(event.mouseX, event.mouseY, (GuiContainerAccessor) event.gui);
            }
        }
    }

    @SubscribeEvent
    public void onKeyTyped(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (event.gui instanceof GuiContainer) {
            SideMenu sidebar = SideMenuManager.getOrCreateSidebar((GuiContainer) event.gui);
            int key = Keyboard.getEventKey();
            char character = Keyboard.getEventCharacter();
            if(!Keyboard.isKeyDown(key)) return;
            // Draw sidebar hovering text
            for (CustomElement element : sidebar.getElements().values()) {
                if(element.isFocused()) {
                    element.onKeyTyped(character,key);
                }
            }
        }
    }

    public static void removeSidebar(GuiContainer gui) {
        sidebarMap.remove(gui);
    }
}
