package mrfast.sbf.features.trackers;

import java.util.ArrayList;
import java.util.List;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.PricingData;
import mrfast.sbf.core.SkyblockInfo;
import mrfast.sbf.events.SecondPassedEvent;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class IceTreasureTracker {
    private static final Minecraft mc = Minecraft.getMinecraft();

    static int iceTreasuresMined = 0;
    static int redGift = 0;
    static int whiteGift = 0;
    static int greenGift = 0;
    static int talisman = 0;
    static int fragment = 0;
    static int hoodie = 0;

    static double totalMoney = 0;
    static double redGiftTotal = 0;
    static double whiteGiftTotal = 0;
    static double greenGiftTotal = 0;
    static double talismanTotal = 0;
    static double fragmentTotal = 0;
    static double hoodieTotal = 0;

    static boolean hidden = true;
    static int seconds = 0;
    static int totalSeconds = 0;
    @SubscribeEvent
    public void onload(WorldEvent.Load event) {
        seconds = 0;
        hidden = true;
        iceTreasuresMined = 0;
        redGift = 0;
        whiteGift = 0;
        greenGift = 0;
        talisman = 0;
        fragment = 0;
        hoodie = 0;
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        String raw = event.message.getUnformattedText();
        if(raw.contains("FROZEN TREASURE")) {
            seconds = 300;
            hidden = false;
            if(raw.contains("Green Gift")) greenGift++;
            if(raw.contains("Red Gift")) redGift++;
            if(raw.contains("White Gift")) whiteGift++;
            if(raw.contains("Fragment")) fragment++;
            if(raw.contains("Talisman")) talisman++;
            if(raw.contains("Einary's Red Hoodie")) hoodie++;
            iceTreasuresMined++;
        }
    }

    @SubscribeEvent
    public void onSecond(SecondPassedEvent event) {
        if(Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().theWorld != null) {
            if(!hidden) {
                seconds--;
            }
            if(seconds <= 0) {
                hidden = true;
            } else {
                totalSeconds++;
            }
        }
    }

    @SubscribeEvent
    public void onSecond2(SecondPassedEvent event) {
        if(Utils.GetMC().thePlayer == null || !Utils.inSkyblock) return;
        try {
            double redGiftValue = Math.floor(PricingData.bazaarPrices.get("RED_GIFT"));
            double greenGiftValue = Math.floor(PricingData.bazaarPrices.get("GREEN_GIFT"));
            double whiteGiftValue = Math.floor(PricingData.bazaarPrices.get("WHITE_GIFT"));
            double fragmentValue = Math.floor(PricingData.bazaarPrices.get("GLACIAL_FRAGMENT"));
            double talismanValue = Math.floor(PricingData.lowestBINs.get("GLACIAL_TALISMAN"));
            double hoodieValue = Math.floor(PricingData.lowestBINs.get("EINARY_RED_HOODIE"));
            
            if(redGiftValue != 0) redGiftTotal = redGift*redGiftValue;
            if(greenGiftValue != 0) greenGiftTotal = greenGift*greenGiftValue;
            if(whiteGiftValue != 0) whiteGiftTotal = whiteGift*whiteGiftValue;
            if(fragmentValue != 0) fragmentTotal = fragment*fragmentValue;
            if(talismanValue != 0) talismanTotal = talisman*talismanValue;
            if(hoodieValue != 0) hoodieTotal = hoodie*hoodieValue;

            totalMoney = (redGiftTotal+greenGiftTotal+whiteGiftTotal+fragmentTotal+talismanTotal+hoodieTotal);
        } catch (Exception e) {
            //TODO: handle exception
        }
    }
    static {
        new IceTreasureTrackerGui();
    }

    public static class IceTreasureTrackerGui extends UIElement {
        public IceTreasureTrackerGui() {
            super("Glacial Cave Treasure Tracker", new Point(0.0f, 0.52037036f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            if (this.getToggled() && Minecraft.getMinecraft().thePlayer != null && mc.theWorld != null && !hidden) {
                String[] lines = {
                    ChatFormatting.YELLOW+""+ChatFormatting.BOLD+"Ice Treasure Tracker",
                    ChatFormatting.GREEN+"  Time Elapsed: §r"+Utils.secondsToTime(totalSeconds),
                    ChatFormatting.GREEN+"  Treasures Mined: §r"+Utils.nf.format(iceTreasuresMined),
                    ChatFormatting.GREEN+"  Total Value: §6"+Utils.nf.format(totalMoney),
                    ChatFormatting.AQUA+""+ChatFormatting.BOLD+" Drops",
                    ChatFormatting.RED+"  • Red Gift: §r"+redGift+" §7("+Utils.nf.format(redGiftTotal)+")",
                    ChatFormatting.GREEN+"  • Green Gift: §r"+greenGift+" §7("+Utils.nf.format(greenGiftTotal)+")",
                    ChatFormatting.WHITE+"  • White Gift: §r"+whiteGift+" §7("+Utils.nf.format(whiteGiftTotal)+")",
                    ChatFormatting.LIGHT_PURPLE+"  • Fragment: §r"+fragment+" §7("+Utils.nf.format(fragmentTotal)+")",
                    ChatFormatting.GOLD+"  • Talisman: §r"+talisman+" §7("+Utils.nf.format(talismanTotal)+")",
                    ChatFormatting.DARK_RED+"  • Einary's Red Hoodie: §r"+hoodie+" §7("+Utils.nf.format(hoodieTotal)+")"
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
            String[] lines = {
                ChatFormatting.YELLOW+""+ChatFormatting.BOLD+"Ice Treasure Tracker",
                ChatFormatting.GREEN+"  Time Elapsed: §r"+Utils.secondsToTime(576),
                ChatFormatting.GREEN+"  Treasures Mined: §r"+Utils.nf.format(233),
                ChatFormatting.GREEN+"  Total Value: §6"+Utils.nf.format(654323),
                ChatFormatting.AQUA+""+ChatFormatting.BOLD+" Drops",
                ChatFormatting.RED+"  • Red Gift: §r"+1,
                ChatFormatting.GREEN+"  • Green Gift: §r"+19,
                ChatFormatting.WHITE+"  • White Gift: §r"+67,
                ChatFormatting.LIGHT_PURPLE+"  • Fragment: §r"+4,
                ChatFormatting.GOLD+"  • Talisman: §r"+1,
                ChatFormatting.DARK_RED+"  • Einary's Red Hoodie: §r"+0
            };
            int lineCount = 0;
            for(String line:lines) {
                Utils.GetMC().fontRendererObj.drawStringWithShadow(line, 0, lineCount*(mc.fontRendererObj.FONT_HEIGHT),0xFFFFFF);
                lineCount++;
            }
        }
        
        @Override
        public boolean getToggled() {
            return SkyblockFeatures.config.IceTreasureTracker;
        }

        @Override
        public boolean getRequirement() {
            return SkyblockInfo.localLocation.contains("Glacial") && Utils.inSkyblock;
        }

        @Override
        public int getHeight() {
            return (Utils.GetMC().fontRendererObj.FONT_HEIGHT+1)*11;
        }

        @Override
        public int getWidth() {
            return Utils.GetMC().fontRendererObj.getStringWidth("Electron Transmitter: 10");
        }
    }
}
