package mrfast.sbf.features.statDisplays;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.GuiUtils;
import mrfast.sbf.utils.Utils;


public class ManaDisplay {

    static {
        new ManaDisplayGUI();
    }

    static String display = Utils.mana +"/"+Utils.maxMana;
    public static class ManaDisplayGUI extends UIElement {
        public ManaDisplayGUI() {
            super("Mana Display", new Point(0.47864583f, 0.80324066f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            GuiUtils.drawText("§9"+Utils.nf.format(Utils.mana) +"/"+Utils.nf.format(Utils.maxMana), 0, 0, GuiUtils.TextStyle.BLACK_OUTLINE);
        }
        @Override
        public void drawElementExample() {
            display="§9"+Utils.nf.format(Utils.mana) +"/"+Utils.nf.format(Utils.maxMana);
            GuiUtils.drawText(display, 0, 0, GuiUtils.TextStyle.BLACK_OUTLINE);
        }

        @Override
        public boolean getToggled() {
            return SkyblockFeatures.config.ManaDisplay;
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
