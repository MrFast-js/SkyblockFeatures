package mrfast.sbf.features.statDisplays;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.GuiUtils;
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
            GuiUtils.drawText("§c"+Utils.Health+"/"+Utils.maxHealth, 0, 0, GuiUtils.TextStyle.BLACK_OUTLINE);
        }
        @Override
        public void drawElementExample() {
            display = "§c"+Utils.Health+"/"+Utils.maxHealth;
            GuiUtils.drawText(display, 0, 0, GuiUtils.TextStyle.BLACK_OUTLINE);
        }

        @Override
        public boolean getToggled() {
            return SkyblockFeatures.config.HealthDisplay;
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
