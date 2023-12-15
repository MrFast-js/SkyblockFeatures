package mrfast.sbf.features.statDisplays;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.GuiUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;


public class DefenseDisplay {

    private static final Minecraft mc = Minecraft.getMinecraft();

    static {
        new DefenseDisplayGUI();
    }

    static String display = Utils.Defense+"";
    public static class DefenseDisplayGUI extends UIElement {
        public DefenseDisplayGUI() {
            super("Defense Display", new Point(0.2f, 0.0f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            if(mc.thePlayer == null || !Utils.inSkyblock) return;
            display = Utils.Defense+"";
            if (this.getToggled() && Minecraft.getMinecraft().thePlayer != null && mc.theWorld != null) {
                GuiUtils.drawText("§a"+display, 0, 0, GuiUtils.TextStyle.BLACK_OUTLINE);
            }
        }
        @Override
        public void drawElementExample() {
            if(mc.thePlayer == null || !Utils.inSkyblock) return;
            display = Utils.Defense+"";
            GuiUtils.drawText("§a"+display, 0, 0, GuiUtils.TextStyle.BLACK_OUTLINE);

        }

        @Override
        public boolean getToggled() {
            return Utils.inSkyblock && SkyblockFeatures.config.DefenseDisplay;
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
