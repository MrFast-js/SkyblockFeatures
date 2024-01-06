package mrfast.sbf.features.statDisplays.bars;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.gui.Gui;

import java.awt.*;

public class HealthBar {
    static {
        new HealthBarGUI();
    }

    public static class HealthBarGUI extends UIElement {

        public HealthBarGUI() {
            super("Health Bar", new Point(0.40605482f, 0.9166667f));
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
            int max = Utils.maxHealth;
            int health = Utils.health;
            int absorbtion = 0;
            if(health>max) {
                absorbtion=health-max;
                health=max;
            }
            int total = (max+absorbtion);
            double healthFillPerc = (double) health /total;
            double absorbFillPerc = (double) absorbtion /total;

            Color HealthColor = Color.RED;
            Color AbsorbColor = new Color(0xFFAA00);

            Gui.drawRect(0, 0,80, 10, Color.black.getRGB());

            Gui.drawRect(2, 2,(int)(78d*healthFillPerc), 8, HealthColor.getRGB());
            if(absorbtion!=0) {
                int fillPixels = (int)(78d*absorbFillPerc)+3;
                Gui.drawRect(Math.min(76,2+(78-fillPixels)), 2, 78, 8, AbsorbColor.getRGB());
            }
        }

        @Override
        public boolean getToggled() {
            return SkyblockFeatures.config.HealthBar;
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
