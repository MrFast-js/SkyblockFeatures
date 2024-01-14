package mrfast.sbf.gui;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.*;
import java.util.List;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.gui.components.MoveableFeature;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

public class EditLocationsGui extends GuiScreen {

    private float lastMouseX, lastMouseY, xOffset, yOffset;
    private final int screenWidth = Utils.GetMC().displayWidth / 2;
    private final int screenHeight = Utils.GetMC().displayHeight / 2;
    private static boolean copyingPos, isMouseMoving = false;
    private static MoveableFeature hoveredFeature;
    private UIElement dragging;
    private final Map<UIElement, MoveableFeature> MoveableFeatures = new HashMap<>();

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void initGui() {
        super.initGui();
        for (Map.Entry<Integer, UIElement> e : SkyblockFeatures.GUIMANAGER.getElements().entrySet()) {
            MoveableFeature lb = new MoveableFeature(e.getValue());
            this.buttonList.add(lb);
            this.MoveableFeatures.put(e.getValue(), lb);
        }
        this.buttonList.add(new GuiButton(6969, this.width / 2 - 60, 0, 120, 20, getButtonLabel()));
        hoveredFeature = null;
    }

    public String getButtonLabel() {
        if (GuiManager.showAllEnabledElements) {
            return "§e§lShow Active Only";
        } else {
            return "§e§lShow All Enabled";
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        onMouseMove();
        this.drawGradientRect(0, 0, this.width, this.height, new Color(0, 0, 0, 50).getRGB(), new Color(0, 0, 0, 200).getRGB());
        for (GuiButton button : this.buttonList) {
            if (button instanceof MoveableFeature) {
                if (!((MoveableFeature) button).element.getToggled()) continue;
                MoveableFeature moveableFeature = (MoveableFeature) button;
                if (moveableFeature.hovered) {
                    // Debug tool to copy elements position to clipboard
                    if (Utils.isDeveloper()) {
                        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && Keyboard.isKeyDown(Keyboard.KEY_C)) {
                            if (copyingPos) continue;

                            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                            String point = "new Point(" + moveableFeature.relativeX + "f, " + moveableFeature.relativeY + "f)";
                            clipboard.setContents(new StringSelection("new Point(" + moveableFeature.relativeX + "f, " + moveableFeature.relativeY + "f)"), null);

                            copyingPos = true;
                            Utils.sendMessage(ChatFormatting.GREEN + "Copied hovered element position: " + ChatFormatting.YELLOW + point);
                        } else {
                            copyingPos = false;
                        }
                    }
                    hoveredFeature = moveableFeature;
                }
                ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
                float guiScaleFactor = (1f / scaledResolution.getScaleFactor()) * 2;

                GlStateManager.pushMatrix();
                GlStateManager.scale(guiScaleFactor, guiScaleFactor, 1.0f); // Apply the GUI scale factor
                GlStateManager.translate(moveableFeature.relativeX * screenWidth, moveableFeature.relativeY * screenHeight, 0);

                button.drawButton(this.mc, mouseX, mouseY);
                GlStateManager.popMatrix();
            } else {
                button.drawButton(this.mc, mouseX, mouseY);
            }
        }

        if (hoveredFeature != null && !isMouseMoving) {
            List<String> renderTooltip = new ArrayList<>(Arrays.asList(
                    "§a§l" + hoveredFeature.getElement().getName(),
                    "§7X: §e" + Math.round(hoveredFeature.actualX) + " §7Y: §e" + Math.round(hoveredFeature.actualY),
                    "§eRClICK to open config"));

            int tooltipWidth = Utils.GetMC().fontRendererObj.getStringWidth(renderTooltip.get(0));
            int tooltipHeight = renderTooltip.size() * Utils.GetMC().fontRendererObj.FONT_HEIGHT;

            int adjustedX = Math.max(0,mouseX - 3);

            if (adjustedX + tooltipWidth > screenWidth) {
                adjustedX = screenWidth - tooltipWidth;
            }

            if (mouseY + tooltipHeight > screenHeight) {
                mouseY = Math.max(screenHeight - tooltipHeight, mouseY - tooltipHeight - 12);
            }

            if(Mouse.isButtonDown(1)) {
                ConfigGui.searchQuery = hoveredFeature.getElement().getName();
                mrfast.sbf.utils.GuiUtils.openGui(new ConfigGui(true));
            }

            GuiUtils.drawHoveringText(renderTooltip, adjustedX, mouseY, screenWidth, screenHeight, -1, Utils.GetMC().fontRendererObj);
        }
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button instanceof MoveableFeature) {
            MoveableFeature currentlyDraggedFeature = (MoveableFeature) button;
            dragging = currentlyDraggedFeature.getElement();

            float floatMouseX = Mouse.getX() * 0.5f;
            float floatMouseY = (mc.displayHeight - Mouse.getY()) * 0.5f;

            xOffset = floatMouseX - dragging.getX() * screenWidth;
            yOffset = floatMouseY - dragging.getY() * screenHeight;
        }
        if (button.id == 6969) {
            GuiManager.showAllEnabledElements = !GuiManager.showAllEnabledElements;
            button.displayString = getButtonLabel();
        }
    }

    private long lastMoveTime = System.currentTimeMillis();

    protected void onMouseMove() {
        float floatMouseX = Mouse.getX() * 0.5f;
        float floatMouseY = (mc.displayHeight - Mouse.getY()) * 0.5f;

        if (dragging != null) {
            MoveableFeature currentlyDraggedFeature = MoveableFeatures.get(dragging);
            if (currentlyDraggedFeature == null) return;
            float x = Math.max(2, Math.min(floatMouseX - xOffset, screenWidth - dragging.getWidth() - 2));
            float y = Math.max(2, Math.min(floatMouseY - yOffset, screenHeight - dragging.getHeight() - 2));

            dragging.setPos(x / screenWidth, y / screenHeight);
        }

        long currentTime = System.currentTimeMillis();
        if (lastMouseX != floatMouseX || lastMouseY != floatMouseY) {
            lastMoveTime = currentTime;
        }
        isMouseMoving = currentTime - lastMoveTime < 500;

        lastMouseX = floatMouseX;
        lastMouseY = floatMouseY;
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        dragging = null;
    }

    @Override
    public void onGuiClosed() {
        GuiManager.saveConfig();
    }
}
