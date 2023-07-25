package mrfast.skyblockfeatures.features.actionBar;

import net.minecraft.client.Minecraft;
import mrfast.skyblockfeatures.SkyblockFeatures;

import mrfast.skyblockfeatures.gui.components.UIElement;
import mrfast.skyblockfeatures.utils.Utils;
import mrfast.skyblockfeatures.gui.components.Point;


public class HealthDisplay {

    private static final Minecraft mc = Minecraft.getMinecraft();

    static {
        new JerryTimerGUI();
    }

    static String display = Utils.Health+"/"+Utils.maxHealth;
    public static class JerryTimerGUI extends UIElement {
        public JerryTimerGUI() {
            super("Health Display", new Point(0.40520838f, 0.9134259f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            if(mc.thePlayer == null || !Utils.inSkyblock) return;
            display = Utils.Health+"/"+Utils.maxHealth;
            if (this.getToggled() && Minecraft.getMinecraft().thePlayer != null && mc.theWorld != null) {
                Utils.drawTextWithStyle(display, 0, 0, 0xFF5555);
            }
        }
        @Override
        public void drawElementExample() {
            if(mc.thePlayer == null || !Utils.inSkyblock) return;
            display = Utils.Health+"/"+Utils.maxHealth;
            Utils.drawTextWithStyle(display, 0, 0, 0xFF5555);
        }

        @Override
        public boolean getToggled() {
            return Utils.inSkyblock && SkyblockFeatures.config.HealthDisplay;
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
