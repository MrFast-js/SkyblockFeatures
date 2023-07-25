package mrfast.skyblockfeatures.features.trackers;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.skyblockfeatures.SkyblockFeatures;

import mrfast.skyblockfeatures.gui.components.UIElement;
import mrfast.skyblockfeatures.events.SecondPassedEvent;
import mrfast.skyblockfeatures.utils.NumberUtil;
import mrfast.skyblockfeatures.utils.Utils;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import mrfast.skyblockfeatures.gui.components.Point;

public class PowderTracker {
    private static final Minecraft mc = Minecraft.getMinecraft();

    static int treasureChestsFound = 0;
    static int mithrilPowderGained = 0;
    static int gemstonePowderGained = 0;

    static boolean hidden = true;
    static int seconds = 0;
    static int totalSeconds = 0;
    static double coinsPerHour = 0;
    @SubscribeEvent
    public void onload(WorldEvent.Load event) {
        if(!SkyblockFeatures.config.PowderTracker) return;
        try {
            seconds = 0;
            hidden = true;
            treasureChestsFound = 0;
            mithrilPowderGained = 0;
            gemstonePowderGained = 0;
        } catch(Exception e) {

        }
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if(!SkyblockFeatures.config.PowderTracker) return;
        String raw = event.message.getUnformattedText();
        if(raw.contains("You uncovered a treasure chest!")) {
            seconds = 300;
            hidden = false;
            treasureChestsFound++;
        }
        if(raw.contains("You received") && raw.contains("Gemstone Powder.")) {
            gemstonePowderGained+= Double.parseDouble(raw.replaceAll("[^0-9]", ""));
        }
        if(raw.contains("You received") && raw.contains("Mithril Powder.")) {
            mithrilPowderGained+= Double.parseDouble(raw.replaceAll("[^0-9]", ""));
        }
    }

    @SubscribeEvent
    public void onSecond(SecondPassedEvent event) {
        if(Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().theWorld != null && SkyblockFeatures.config.PowderTracker) {
            if(!hidden) {
                seconds--;
            }
            if(seconds <= 0) {
                hidden = true;
                treasureChestsFound = 0;
            } else {
                totalSeconds++;
            }
        }
    }

    static {
        new EnderNodeGui();
    }

    static String display = "";
    public static class EnderNodeGui extends UIElement {
        public EnderNodeGui() {
            super("Powder Tracker", new Point(0.004166f, 0.41111112f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            if (this.getToggled() && Minecraft.getMinecraft().thePlayer != null && mc.theWorld != null && !hidden && totalSeconds>0) {
                String gemPowderPerHour = NumberUtil.nf.format(Math.floor((3600/totalSeconds)*gemstonePowderGained));
                String mithrilPowderPerHour = NumberUtil.nf.format(Math.floor((3600/totalSeconds)*mithrilPowderGained));

                String[] lines = {
                    ChatFormatting.BLUE+"Time Elapsed: §r"+Utils.secondsToTime(totalSeconds),
                    ChatFormatting.AQUA+"Treasure Chest Found: §r"+NumberUtil.nf.format(treasureChestsFound),
                    ChatFormatting.LIGHT_PURPLE+"Gemstone Powder: §r"+NumberUtil.nf.format(gemstonePowderGained),
                    ChatFormatting.LIGHT_PURPLE+"Gemstone Powder Per Hour: §r"+gemPowderPerHour,
                    ChatFormatting.DARK_GREEN+"Mithril Powder: §r"+NumberUtil.nf.format(mithrilPowderGained),
                    ChatFormatting.DARK_GREEN+"Gemstone Powder Per Hour: §r"+mithrilPowderPerHour,
                };
                int lineCount = 0;
                for(String line:lines) {
                    Utils.GetMC().fontRendererObj.drawStringWithShadow(line, 0, lineCount*(mc.fontRendererObj.FONT_HEIGHT),0xFFFFFF);
                    lineCount++;
                }
            }
        }
        @Override
        public void drawElementExample() {
            if(mc.thePlayer == null || !Utils.inSkyblock) return;
            String gemPowderPerHour = NumberUtil.nf.format(Math.floor((3600/345)*10203));
            String mithrilPowderPerHour = NumberUtil.nf.format(Math.floor((3600/345)*12932));
            String[] lines = {
                ChatFormatting.BLUE+"Time Elapsed: §r"+Utils.secondsToTime(378),
                ChatFormatting.AQUA+"Treasure Chest Found: §r"+NumberUtil.nf.format(20),
                ChatFormatting.LIGHT_PURPLE+"Gemstone Powder: §r"+NumberUtil.nf.format(10203),
                ChatFormatting.LIGHT_PURPLE+"Gemstone Powder/Hour: §r"+gemPowderPerHour,
                ChatFormatting.DARK_GREEN+"Mithril Powder: §r"+NumberUtil.nf.format(12932),
                ChatFormatting.DARK_GREEN+"Mithril Powder/Hour: §r"+mithrilPowderPerHour,
            };
            int lineCount = 0;
            for(String line:lines) {
                Utils.GetMC().fontRendererObj.drawStringWithShadow(line, 0, lineCount*(mc.fontRendererObj.FONT_HEIGHT),0xFFFFFF);
                lineCount++;
            }
        }

        @Override
        public boolean getToggled() {
            return Utils.inSkyblock && SkyblockFeatures.config.PowderTracker;
        }

        @Override
        public int getHeight() {
            return Utils.GetMC().fontRendererObj.FONT_HEIGHT*6;
        }

        @Override
        public int getWidth() {
            return Utils.GetMC().fontRendererObj.getStringWidth("Treasure Chest Found: §r1234123");
        }
    }
}
