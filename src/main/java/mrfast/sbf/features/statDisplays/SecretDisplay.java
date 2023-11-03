package mrfast.sbf.features.statDisplays;


import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.Utils;



public class SecretDisplay {

    private static final Minecraft mc = Minecraft.getMinecraft();

    static {
        new SecretDisplayGUI();
    }
    static String display = "Secrets";
    public static class SecretDisplayGUI extends UIElement {

        public SecretDisplayGUI() {
            super("Secret Display", new Point(0.2f, 0.0f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            if(mc.thePlayer == null || !Utils.inDungeons) return;
            int secrets = ActionBarListener.secrets;
            int maxSecrets = ActionBarListener.maxSecrets;

            if (this.getToggled() && Minecraft.getMinecraft().thePlayer != null && mc.theWorld != null) {
                List<String> text = new ArrayList<>();

                String color;

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
                    Utils.drawTextWithStyle3(text.get(i), ((float) getWidth() /3)*i, i * Utils.GetMC().fontRendererObj.FONT_HEIGHT);
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
            return SkyblockFeatures.config.SecretsDisplay && Utils.inSkyblock && Utils.inDungeons;
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
