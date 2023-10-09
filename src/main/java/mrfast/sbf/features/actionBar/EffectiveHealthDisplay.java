package mrfast.sbf.features.actionBar;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;


public class EffectiveHealthDisplay {

    private static final Minecraft mc = Minecraft.getMinecraft();

    static {
        new JerryTimerGUI();
    }

    static String display = "1234";

    public static int getSpeed() {
        return Math.round(Utils.Health * (1f+ (Utils.Defence / 100f) ));
    }
    public static class JerryTimerGUI extends UIElement {
        public JerryTimerGUI() {
            super("Effective Health Display", new Point(0f,0f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            if(mc.thePlayer == null || !Utils.inSkyblock) return;
            if (this.getToggled() && Minecraft.getMinecraft().thePlayer != null && mc.theWorld != null) {
                Utils.drawTextWithStyle(getSpeed()+"", 0, 0, 0x00AA00);
            }
        }
        @Override
        public void drawElementExample() {
            if(mc.thePlayer == null || !Utils.inSkyblock) return;
            Utils.drawTextWithStyle("1234", 0, 0, 0x00AA00);
        }

        @Override
        public boolean getToggled() {
            return Utils.inSkyblock && SkyblockFeatures.config.EffectiveHealthDisplay;
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
