package mrfast.sbf.features.statDisplays;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.GuiUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;


public class SpeedDisplay {

    static {
        new SpeedDisplayGUI();
    }

    static String display = "123%";

    public static String getSpeed() {
        String text;
        String walkSpeed = String.valueOf(Minecraft.getMinecraft().thePlayer.capabilities.getWalkSpeed() * 1000);
        text = walkSpeed.substring(0, Math.min(walkSpeed.length(), 3));
        if (text.endsWith(".")) text = text.substring(0, text.indexOf('.')); //remove trailing periods
        return text+"%";
    }
    public static class SpeedDisplayGUI extends UIElement {
        public SpeedDisplayGUI() {
            super("Speed Display", new Point(0.375f, 0.9777778f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            GuiUtils.drawText(getSpeed(), 0, 0, GuiUtils.TextStyle.BLACK_OUTLINE);
        }
        @Override
        public void drawElementExample() {
            GuiUtils.drawText(getSpeed(), 0, 0, GuiUtils.TextStyle.BLACK_OUTLINE);
        }

        @Override
        public boolean getToggled() {
            return SkyblockFeatures.config.SpeedDisplay;
        }

        @Override
        public boolean getRequirement() {
            return Utils.inSkyblock;
        }
        @Override
        public int getHeight() {
            return Utils.GetMC().fontRendererObj.FONT_HEIGHT;
        }

        @Override
        public int getWidth() {
            return Utils.GetMC().fontRendererObj.getStringWidth(display);
        }
    }
}
