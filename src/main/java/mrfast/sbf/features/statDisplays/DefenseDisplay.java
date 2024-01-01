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
            super("Defense Display", new Point(0.5651042f, 0.8037037f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            GuiUtils.drawText("§a"+Utils.Defense, 0, 0, GuiUtils.TextStyle.BLACK_OUTLINE);
        }
        @Override
        public void drawElementExample() {
            display="§a"+Utils.Defense;
            GuiUtils.drawText(display, 0, 0, GuiUtils.TextStyle.BLACK_OUTLINE);
        }

        @Override
        public boolean getToggled() {
            return SkyblockFeatures.config.DefenseDisplay;
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
