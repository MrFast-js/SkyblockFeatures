package mrfast.sbf.features.trackers;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.SkyblockInfo;
import mrfast.sbf.events.SecondPassedEvent;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EnderNodeTracker {
    private static final Minecraft mc = Minecraft.getMinecraft();

    static int enderNodesMined = 0;
    static int nests = 0;
    static int enchantedEndstone = 0;
    static int enchantedObsidian = 0;
    static int grand = 0;
    static int titanic = 0;
    static boolean hidden = true;
    static int seconds = 0;
    static int totalSeconds = 0;

    @SubscribeEvent
    public void onload(WorldEvent.Load event) {
        seconds = 0;
        hidden = true;
        enderNodesMined = 0;
        nests = 0;
        enchantedEndstone = 0;
        enchantedObsidian = 0;
        grand = 0;
        titanic = 0;
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if(!Utils.inSkyblock || !SkyblockFeatures.config.EnderNodeTracker || !SkyblockInfo.map.contains("The End")) return;

        String raw = event.message.getUnformattedText();
        if(raw.contains("ENDER NODE")) {
            seconds = 300;
            hidden = false;
            if(raw.contains("Endermite Nest")) nests++;
            if(raw.contains("Enchanted End Stone")) enchantedEndstone++;
            if(raw.contains("Enchanted Obsidian")) enchantedObsidian++;

            if(raw.contains("5x Grand Experience Bottle")) grand+=5;
            else if(raw.contains("Grand Experience Bottle")) grand++;

            if(raw.contains("Grand Titanic Bottle")) titanic++;
            enderNodesMined++;
        }
    }

    @SubscribeEvent
    public void onSecond(SecondPassedEvent event) {
        if(Utils.inSkyblock && SkyblockFeatures.config.EnderNodeTracker && SkyblockInfo.map.contains("The End")) {
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

    static {
        new EnderNodeGui();
    }

    public static class EnderNodeGui extends UIElement {
        public EnderNodeGui() {
            super("Ender Node Tracker", new Point(0.2f, 0.0f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            if (this.getToggled() && Minecraft.getMinecraft().thePlayer != null && mc.theWorld != null && !hidden) {
                String[] lines = {
                    ChatFormatting.LIGHT_PURPLE+""+ChatFormatting.BOLD+"Ender Node Tracker",
                    ChatFormatting.DARK_PURPLE+"  Time Elapsed: §r"+Utils.secondsToTime(totalSeconds),
                    ChatFormatting.DARK_PURPLE+"  Nodes Mined: §r"+Utils.nf.format(enderNodesMined),
                    ChatFormatting.YELLOW+""+ChatFormatting.BOLD+" Drops",
                    ChatFormatting.RED+"  • Endermite Nest: §r"+nests,
                    ChatFormatting.BLUE+"  • Titanic Exp: §r"+titanic,
                    ChatFormatting.GREEN+"  • Grand Exp: §r"+grand,
                    ChatFormatting.GREEN+"  • Ench. Endestone: §r"+ enchantedEndstone,
                    ChatFormatting.GREEN+"  • Ench. Obsidian: §r"+ enchantedObsidian
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
                ChatFormatting.LIGHT_PURPLE+""+ChatFormatting.BOLD+"Ender Node Tracker",
                ChatFormatting.DARK_PURPLE+"  Time Elapsed: §r5m 4s",
                ChatFormatting.DARK_PURPLE+"  Nodes Mined: §r107",
                ChatFormatting.YELLOW+""+ChatFormatting.BOLD+" Drops",
                ChatFormatting.RED+"  • Endermite Nest: §r3",
                ChatFormatting.BLUE+"  • Titanic Exp: §r2",
                ChatFormatting.GREEN+"  • Grand Exp: §r9",
                ChatFormatting.GREEN+"  • Ench. Endestone: §r4",
                ChatFormatting.GREEN+"  • Ench. Obsidian: §r2"
            };
            int lineCount = 0;
            for(String line:lines) {
                Utils.GetMC().fontRendererObj.drawStringWithShadow(line, 0, lineCount*(mc.fontRendererObj.FONT_HEIGHT),0xFFFFFF);
                lineCount++;
            }
        }

        @Override
        public boolean getToggled() {
            return Utils.inSkyblock && SkyblockFeatures.config.EnderNodeTracker && SkyblockInfo.map.contains("The End");
        }

        @Override
        public int getHeight() {
            return Utils.GetMC().fontRendererObj.FONT_HEIGHT*9;
        }

        @Override
        public int getWidth() {
            return Utils.GetMC().fontRendererObj.getStringWidth("Ender   Node   Tracker");
        }
    }
}
