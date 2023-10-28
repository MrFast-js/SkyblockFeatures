package mrfast.sbf.features.statDisplays;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;


public class HealthDisplay {

    private static final Minecraft mc = Minecraft.getMinecraft();

    static {
        new HealthDisplayGUI();
    }

    static String display = Utils.Health+"/"+Utils.maxHealth;
    public static class HealthDisplayGUI extends UIElement {
        public HealthDisplayGUI() {
            super("Health Display", new Point(0f,0f));
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
