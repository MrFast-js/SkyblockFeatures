package mrfast.skyblockfeatures.features.overlays;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.skyblockfeatures.SkyblockFeatures;

import mrfast.skyblockfeatures.gui.components.UIElement;
import mrfast.skyblockfeatures.events.SecondPassedEvent;

import mrfast.skyblockfeatures.core.PricingData;
import mrfast.skyblockfeatures.core.SkyblockInfo;
import mrfast.skyblockfeatures.utils.Utils;
import mrfast.skyblockfeatures.gui.components.Point;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class GemstoneMiningOverlay {
    private static final Minecraft mc = Minecraft.getMinecraft();
    static int seconds = 0;
    static boolean start = false;

    public static List<Gemstone> gemstones = new ArrayList<>();
    @SubscribeEvent
    public void onload(WorldEvent.Load event) {
        try {
            seconds = 0;
            start = false;
            gemstones.clear();
        } catch(Exception e) {

        }
    }

    public class Gemstone {
        public Long time;
        public String item_name;
        public Integer amount;

        public Gemstone(Long t,String i,Integer a) {
            time = t;
            item_name = i;
            amount = a;
        }
    }

    @SubscribeEvent
    public void onSecond(SecondPassedEvent event) {
        if(Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().theWorld != null && SkyblockFeatures.config.gemstoneTracker) {
            List<Gemstone> gemstonesToRemove = new ArrayList<>();

            for(Gemstone gemstone:gemstones) {
                if((new Date()).getTime()-gemstone.time > 5*60*1000) gemstonesToRemove.add(gemstone);
            }
            for(Gemstone gemstone:gemstonesToRemove) {
                gemstones.remove(gemstone);
            }
            if(start) {
                seconds++;
            }
        }
    }
    @SubscribeEvent
    public void onDrawContainerTitle(ClientChatReceivedEvent event) {
        String message = event.message.getUnformattedText();
        if (message.contains("PRISTINE!") && SkyblockFeatures.config.gemstoneTracker) {
            start = true;
            message = message.toUpperCase();
            String itemName = message.split(" ")[4]+"_"+message.split(" ")[5]+"_GEM";
            Utils.SendMessage(itemName);
            gemstones.add(new Gemstone((new Date()).getTime(), itemName,Integer.parseInt(message.replaceAll("[^0-9]", ""))));
        }
    }

    static {
        new GemstoneMiningGUI();
    }

    public static class GemstoneMiningGUI extends UIElement {
        public GemstoneMiningGUI() {
            super("Gemstone GUI", new Point(0.45052084f, 0.86944443f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            try {
                if(mc.thePlayer == null || !Utils.inSkyblock || !getToggled() || !SkyblockInfo.getInstance().getMap().equals("Crystal Hollows")) return;
                int total = 0;
                for(Gemstone gemstone:gemstones) {
                    if(PricingData.bazaarPrices.get(gemstone.item_name) != null) {
                        total += PricingData.bazaarPrices.get(gemstone.item_name)*gemstone.amount;
                    }
                }
                String[] lines = {
                    ChatFormatting.LIGHT_PURPLE+""+ChatFormatting.BOLD+"Gemstone Mining Info",
                    ChatFormatting.LIGHT_PURPLE+" Time Spent Mining: "+ChatFormatting.GREEN+Utils.secondsToTime(seconds),
                    ChatFormatting.LIGHT_PURPLE+" Gemstone Coins Per hour: §6"+Utils.nf.format(total*12),
                    ChatFormatting.LIGHT_PURPLE+" Pristine Count: §a"+gemstones.size()
                };
                int lineCount = 0;
                for(String line:lines) {
                    Utils.GetMC().fontRendererObj.drawStringWithShadow(line, 0, lineCount*(mc.fontRendererObj.FONT_HEIGHT),0xFFFFFF);
                    lineCount++;
                }
            } catch (Exception e) {
                //TODO: handle exception
            }
        }
        @Override
        public void drawElementExample() {
            if(mc.thePlayer == null || !Utils.inSkyblock) return;
            String[] lines = {
                ChatFormatting.LIGHT_PURPLE+""+ChatFormatting.BOLD+"Gemstone Mining Info",
                ChatFormatting.LIGHT_PURPLE+"Time Spent Mining: 19m 27s",
                ChatFormatting.LIGHT_PURPLE+"Gemstone Coins Per hour: §6123,456",
                ChatFormatting.LIGHT_PURPLE+"Pristine Count: §a3"
            };
            int lineCount = 0;
            for(String line:lines) {
                Utils.GetMC().fontRendererObj.drawStringWithShadow(line, 0, lineCount*(mc.fontRendererObj.FONT_HEIGHT),0xFFFFFF);
                lineCount++;
            }
        }

        @Override
        public boolean getToggled() {
            return Utils.inSkyblock && SkyblockFeatures.config.gemstoneTracker;
        }

        @Override
        public int getHeight() {
            return Utils.GetMC().fontRendererObj.FONT_HEIGHT*4;
        }

        @Override
        public int getWidth() {
            return Utils.GetMC().fontRendererObj.getStringWidth("Gemstone Coins Per hour: §6123,456");
        }
    }
}
