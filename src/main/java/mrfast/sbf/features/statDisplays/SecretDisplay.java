package mrfast.sbf.features.statDisplays;


import mrfast.sbf.utils.GuiUtils;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.Utils;
import org.jetbrains.annotations.NotNull;


public class SecretDisplay {

    private static final Minecraft mc = Minecraft.getMinecraft();

    static {
        new SecretDisplayGUI();
    }

    public static class SecretDisplayGUI extends UIElement {

        public SecretDisplayGUI() {
            super("Secret Display", new Point(0.2f, 0.0f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            int secrets = ActionBarListener.secrets;
            int maxSecrets = ActionBarListener.maxSecrets;

            List<String> text = getSecrets(secrets, maxSecrets);

            for (int i = 0; i < text.size(); i++) {
                GuiUtils.drawText(text.get(i), ((float) getWidth() /3)*i, i * Utils.GetMC().fontRendererObj.FONT_HEIGHT, GuiUtils.TextStyle.BLACK_OUTLINE);
            }
        }
        @Override
        public void drawElementExample() {
            ArrayList<String> text = new ArrayList<>();

            text.add("§7Secrets");
            text.add("§c1§7/§c9");

            for (int i = 0; i < text.size(); i++) {
                GuiUtils.drawText(text.get(i), (getWidth()/3)*i, i * Utils.GetMC().fontRendererObj.FONT_HEIGHT);
            }
        }

        @Override
        public boolean getToggled() {
            return SkyblockFeatures.config.SecretsDisplay;
        }

        @Override
        public boolean getRequirement() {
            return Utils.inDungeons && Utils.inSkyblock;
        }

        @Override
        public int getHeight() {
            return Utils.GetMC().fontRendererObj.FONT_HEIGHT*2;
        }

        @Override
        public int getWidth() {
            return Utils.GetMC().fontRendererObj.getStringWidth("§7Secrets");
        }
    }

    @NotNull
    private static List<String> getSecrets(int secrets, int maxSecrets) {
        List<String> text = new ArrayList<>();

        String color;

        if(secrets == maxSecrets) {
            color = "§a";
        } else if(secrets > maxSecrets /2) {
            color = "§e";
        } else {
            color = "§c";
        }

        text.add("§7Secrets");

        if(secrets == -1) {
            text.add("§7None");
        } else {
            text.add(color+ secrets +"§7/"+color+ maxSecrets);
        }
        return text;
    }
}
