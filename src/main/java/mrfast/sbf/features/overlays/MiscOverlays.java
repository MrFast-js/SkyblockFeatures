package mrfast.sbf.features.overlays;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.SkyblockInfo;
import mrfast.sbf.features.overlays.maps.CrystalHollowsMap;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
public class MiscOverlays {
    public static Minecraft mc = Utils.GetMC();
    static {
        new timeOverlay();
        new dayCounter();
    }   
    public static String getTime() {
        return new SimpleDateFormat("hh:mm:ss").format(new Date());
    }
    public static class timeOverlay extends UIElement {
        public timeOverlay() {
            super("timeOverlay", new Point(0f,0f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            if(mc.thePlayer == null || !Utils.inSkyblock) return;
            if (this.getToggled() && Minecraft.getMinecraft().thePlayer != null && mc.theWorld != null) {
                Utils.drawTextWithStyle3("["+getTime()+"]", 0, 0);
            }
        }

        @Override
        public void drawElementExample() {
            if(mc.thePlayer == null || !Utils.inSkyblock) return;
            Utils.drawTextWithStyle3("["+getTime()+"]", 0, 0);
        }

        @Override
        public boolean getToggled() {
            return SkyblockFeatures.config.clock;
        }

        @Override
        public int getHeight() {
            return Utils.GetMC().fontRendererObj.FONT_HEIGHT;
        }

        @Override
        public int getWidth() {
            return Utils.GetMC().fontRendererObj.getStringWidth("["+getTime()+"]");
        }
    }

    public static class dayCounter extends UIElement {
        public dayCounter() {
            super("dayCounter", new Point(0.2f, 0.0f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            if(mc.thePlayer == null || !Utils.inSkyblock || Utils.GetMC().theWorld==null || SkyblockInfo.getInstance().getLocation()==null) return;
            if (CrystalHollowsMap.inCrystalHollows && SkyblockFeatures.config.dayTracker) {
                Long time = Utils.GetMC().theWorld.getWorldTime();
                Double timeDouble = time.doubleValue()/20/60/20;
                Double day = (Math.round(timeDouble*100.0))/100.0;
                Utils.drawTextWithStyle3(ChatFormatting.GREEN+"Day "+day, 0, 0);
            }
        }

        @Override
        public void drawElementExample() {
            if(mc.thePlayer == null || !Utils.inSkyblock) return;
            Utils.drawTextWithStyle3(ChatFormatting.GREEN+"Day 2.12", 0, 0);
        }

        @Override
        public boolean getToggled() {
            return SkyblockFeatures.config.dayTracker;
        }

        @Override
        public int getHeight() {
            return Utils.GetMC().fontRendererObj.FONT_HEIGHT;
        }

        @Override
        public int getWidth() {
            return Utils.GetMC().fontRendererObj.getStringWidth("["+getTime()+"]");
        }
    }
}
