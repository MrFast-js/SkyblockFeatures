package mrfast.sbf.features.statDisplays;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.GuiUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;


public class EffectiveHealthDisplay {

    private static final Minecraft mc = Minecraft.getMinecraft();

    static {
        new EffectiveHealthDisplayGUI();
    }

    public static int getEffectiveHealth() {
        return Math.round(Utils.Health * (1f+ (Utils.Defence / 100f) ));
    }
    public static class EffectiveHealthDisplayGUI extends UIElement {
        public EffectiveHealthDisplayGUI() {
            super("Effective Health Display", new Point(0f,0f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            if(mc.thePlayer == null || !Utils.inSkyblock) return;
            if (this.getToggled() && Minecraft.getMinecraft().thePlayer != null && mc.theWorld != null) {
                GuiUtils.drawText("§2"+getEffectiveHealth(), 0, 0, GuiUtils.TextStyle.BLACK_OUTLINE);
            }
        }
        @Override
        public void drawElementExample() {
            if(mc.thePlayer == null || !Utils.inSkyblock) return;
            GuiUtils.drawText("§2"+getEffectiveHealth(), 0, 0, GuiUtils.TextStyle.BLACK_OUTLINE);
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
            return Utils.GetMC().fontRendererObj.getStringWidth(getEffectiveHealth()+"");
        }
    }
}
