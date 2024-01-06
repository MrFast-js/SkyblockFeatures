package mrfast.sbf.features.statDisplays.bars;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.gui.Gui;

import java.awt.*;

public class ManaBar {
    static {
        new ManaBarGUI();
    }

    public static class ManaBarGUI extends UIElement {

        public ManaBarGUI() {
            super("Mana Bar", new Point(0.50865895f, 0.9157407f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            drawHealthBar();
        }
        @Override
        public void drawElementExample() {
            drawHealthBar();
        }

        private void drawHealthBar() {
            int max = Utils.maxMana;
            int mana = Utils.mana;
            int overflow = Utils.overflowMana;
            int total = (max+overflow);
            double manaFillPerc = (double) mana / total;
            double overflowFillPerc = (double) overflow / total;

            Color manaColor = new Color(0x5555FF);
            Color overflowColor = new Color(0x55FFFF);
            Gui.drawRect(0, 0,80, 10, Color.black.getRGB());

            Gui.drawRect(2, 2,(int)(78d*manaFillPerc), 8, manaColor.getRGB());
            if(overflow!=0) {
                int fillPixels = (int)(78d*overflowFillPerc)+3;
                Gui.drawRect(Math.min(76,2+(78-fillPixels)), 2, 78, 8, overflowColor.getRGB());
            }
        }

        @Override
        public boolean getToggled() {
            return SkyblockFeatures.config.ManaBar;
        }

        @Override
        public boolean getRequirement() {
            return Utils.inSkyblock;
        }

        @Override
        public int getHeight() {
            return 11;
        }

        @Override
        public int getWidth() {
            return 81;
        }
    }
}
