package mrfast.sbf.gui.SideMenu;

import net.minecraft.client.gui.inventory.GuiContainer;

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

    public static void removeSidebar(GuiContainer gui) {
        sidebarMap.remove(gui);
    }
}
