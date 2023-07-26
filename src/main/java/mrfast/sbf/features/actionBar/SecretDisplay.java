package mrfast.sbf.features.actionBar;


import net.minecraft.client.Minecraft;

import java.util.ArrayList;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.Utils;



public class SecretDisplay {

    private static final Minecraft mc = Minecraft.getMinecraft();

    static {
        new JerryTimerGUI();
    }
    static String display = "Secrets";
    public static class JerryTimerGUI extends UIElement {

        public JerryTimerGUI() {
            super("Secret Display", new Point(0.45052084f, 0.86944443f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            if(mc.thePlayer == null || !Utils.inDungeons) return;
            int secrets = ActionBarListener.secrets;
            int maxSecrets = ActionBarListener.maxSecrets;

            if (this.getToggled() && Minecraft.getMinecraft().thePlayer != null && mc.theWorld != null) {
                // Utils.drawTextWithStyle(String.valueOf(secrets) + "/"+maxSecrets+" Secrets", 0, 0, secretsColor);
                // GlStateManager.color(1, 1, 1, 1);
                ArrayList<String> text = new ArrayList<>();

                String color = "§c";

                if(secrets == maxSecrets) {
                    color = "§a";
                } else if(secrets > maxSecrets/2) {
                    color = "§e";
                } else {
                    color = "§c";
                }

                text.add("§7Secrets");

                if(secrets == -1) {
                    text.add("§7None");
                } else {
                    text.add(color+secrets+"§7/"+color+maxSecrets);
                }
                

                for (int i = 0; i < text.size(); i++) {
                    Utils.drawTextWithStyle3(text.get(i), (getWidth()/3)*i, i * Utils.GetMC().fontRendererObj.FONT_HEIGHT);
                }
            }
        }
        @Override
        public void drawElementExample() {
            ArrayList<String> text = new ArrayList<>();

            String color = "§c";
            
            text.add("§7Secrets");
            text.add(color+"1"+"§7/"+color+"9");

            for (int i = 0; i < text.size(); i++) {
                Utils.drawText(text.get(i), (getWidth()/3)*i, i * Utils.GetMC().fontRendererObj.FONT_HEIGHT);
            }
        }

        @Override
        public boolean getToggled() {
            return SkyblockFeatures.config.SecretsDisplay;
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
}
