package mrfast.sbf.gui.SideMenu;

import mrfast.sbf.events.SlotClickedEvent;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.HashMap;
import java.util.Map;

public class MenuOverlayManager {

    private static Map<String, Overlay> overlayRegistry = new HashMap<>();

    public static class Overlay {
        public MenuOverlay content;
        public String name;
        public GuiScreen associatedGui;

        public Overlay(String overlayName, MenuOverlay overlayContent, GuiScreen associatedGui) {
            this.associatedGui = associatedGui;
            this.name = overlayName;
            this.content = overlayContent;
        }
    }

    public static Map<String, Overlay> getOverlays() {
        return overlayRegistry;
    }

    public static Overlay getOrCreateMenuOverlay(String overlayName, GuiScreen gui) {
        return overlayRegistry.computeIfAbsent(overlayName, name ->
                new Overlay(name, new MenuOverlay(), gui)
        );
    }


    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event) {
        if (buttonHovered) {
            event.toolTip.clear();
        }
    }

    @SubscribeEvent
    public void onSlotClick(SlotClickedEvent event) {
        if (buttonHovered) {
            event.setCanceled(true);
        }
    }

    boolean buttonHovered = false;
    static GuiScreen mouseWentDownOn;
    @SubscribeEvent
    public void onGuiScreenDrawn(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (event.gui instanceof GuiContainer) {
            buttonHovered = false;
            for (Overlay overlay : MenuOverlayManager.getOverlays().values()) {
                buttonHovered = overlay.content.handleMouseMovement(event.mouseX, event.mouseY, event.gui);
            }
        }
        if (Mouse.isButtonDown(0)) {
            if (mouseWentDownOn == null) {
                mouseWentDownOn = event.gui;
            }
        } else {
            mouseWentDownOn = null;
        }

        CustomElement.lastMouseState = Mouse.isButtonDown(0);
    }
    @SubscribeEvent
    public void onMenuOpen(GuiScreenEvent.InitGuiEvent event) {
        overlayRegistry.clear();
    }

    @SubscribeEvent
    public void onKeyTyped(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (event.gui instanceof GuiContainer) {
            for (Overlay overlay : MenuOverlayManager.getOverlays().values()) {
                int key = Keyboard.getEventKey();
                char character = Keyboard.getEventCharacter();
                if (!Keyboard.isKeyDown(key)) return;

                overlay.content.handleKeyInput(character, key);
            }
        }
    }
}
