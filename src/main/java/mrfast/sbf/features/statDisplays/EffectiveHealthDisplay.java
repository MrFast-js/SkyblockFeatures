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
        return Math.round(Utils.health * (1f+ (Utils.Defense / 100f) ));
    }
    public static class EffectiveHealthDisplayGUI extends UIElement {
        public EffectiveHealthDisplayGUI() {
            super("Effective Health Display", new Point(0.3703125f, 0.9539931f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            GuiUtils.drawText("§2"+Utils.nf.format(getEffectiveHealth()), 0, 0, GuiUtils.TextStyle.BLACK_OUTLINE);
        }
        @Override
        public void drawElementExample() {
            GuiUtils.drawText("§2"+Utils.nf.format(getEffectiveHealth()), 0, 0, GuiUtils.TextStyle.BLACK_OUTLINE);
        }

        @Override
        public boolean getToggled() {
            return SkyblockFeatures.config.EffectiveHealthDisplay;
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
            return Utils.GetMC().fontRendererObj.getStringWidth(getEffectiveHealth()+"");
        }
    }
}
