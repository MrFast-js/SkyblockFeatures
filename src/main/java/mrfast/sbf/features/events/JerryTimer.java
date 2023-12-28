package mrfast.sbf.features.events;

import com.google.gson.JsonObject;
import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.events.SecondPassedEvent;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.APIUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


public class JerryTimer {
    public static int seconds = 360;
    public static  String display = EnumChatFormatting.LIGHT_PURPLE + "Jerry: " + "6:00";
    private static final Minecraft mc = Minecraft.getMinecraft();
    static boolean checkedMayor = false;
    static boolean isJerryMayor = false;

    @SubscribeEvent
    public void onSeconds(SecondPassedEvent event) {
        if (!SkyblockFeatures.config.jerryTimer || !Utils.inSkyblock) return;

        if(!checkedMayor) {
            checkedMayor = true;
            new Thread(()->{
                JsonObject mayorResponse = APIUtils.getJSONResponse("https://api.hypixel.net/resources/skyblock/election");
                String currentMayor = mayorResponse.get("mayor").getAsJsonObject().get("name").getAsString();
                if(currentMayor.equals("Jerry")) {
                    isJerryMayor = true;
                }
            }).start();
        }
        if(!isJerryMayor) return;

        if (seconds < 361 && seconds > 0) {
            seconds--;
        }
        if (seconds == 0) {
            display = EnumChatFormatting.LIGHT_PURPLE + "Jerry: " + EnumChatFormatting.GREEN + "Ready!";
            return;
        }
        int secondsDisplay = seconds % 60;
        if (("" + seconds % 60).length() == 1) {
            display = EnumChatFormatting.LIGHT_PURPLE + "Jerry: " + EnumChatFormatting.RED + seconds / 60 + ":0" + secondsDisplay;
        } else {
            display = EnumChatFormatting.LIGHT_PURPLE + "Jerry: " + EnumChatFormatting.RED + seconds / 60 + ":" + secondsDisplay;
        }
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (!SkyblockFeatures.config.jerryTimer || !isJerryMayor) return;

        String unformatted = event.message.getUnformattedText();
        if (unformatted.contains("☺") && unformatted.contains("Jerry") && !unformatted.contains("Jerry Box")) {
            seconds = 359;
        }
    }
    static {
        new JerryTimerGUI();
    }   
    public static class JerryTimerGUI extends UIElement {
        public JerryTimerGUI() {
            super("Jerry Timer", new Point(0.2f, 0.0f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            if(mc.thePlayer == null || !Utils.inSkyblock) return;
            if (this.getToggled() && Minecraft.getMinecraft().thePlayer != null && mc.theWorld != null) {
                mc.fontRendererObj.drawString(display, 0, 0, 0xFFFFFF, true);
            }
        }
        @Override
        public void drawElementExample() {
            if(mc.thePlayer == null || !Utils.inSkyblock) return;
            mc.fontRendererObj.drawString(display, 0, 0, 0xFFFFFF, true);
        }

        @Override
        public boolean getToggled() {
            return SkyblockFeatures.config.jerryTimer;
        }

        @Override
        public boolean getRequirement() {
            return Utils.inSkyblock && isJerryMayor;
        }

        @Override
        public int getHeight() {
            return Utils.GetMC().fontRendererObj.FONT_HEIGHT;
        }

        @Override
        public int getWidth() {
            return 12 + Utils.GetMC().fontRendererObj.getStringWidth(display);
        }
    }
}
