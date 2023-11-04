package mrfast.sbf.features.overlays;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.PricingData;
import mrfast.sbf.core.SkyblockInfo;
import mrfast.sbf.events.SecondPassedEvent;
import mrfast.sbf.features.overlays.maps.CrystalHollowsMap;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.GuiUtils;
import mrfast.sbf.utils.Utils;
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
        seconds = 0;
        start = false;
        gemstones.clear();
    }

    public static class Gemstone {
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
                // Change the 5 to minutes for average
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
    public void onChat(ClientChatReceivedEvent event) {
        String message = event.message.getUnformattedText();
        if (message.contains("PRISTINE!") && SkyblockFeatures.config.gemstoneTracker) {
            start = true;
            message = message.toUpperCase();
            String itemName = message.split(" ")[4]+"_"+message.split(" ")[5]+"_GEM";
            String count  = message.split("X")[1].split("!")[0];
            Integer countInt = Integer.parseInt(count.replaceAll("[^0-9]", ""));
            gemstones.add(new Gemstone((new Date()).getTime(), itemName, countInt));
        }
    }

    static {
        new GemstoneMiningGUI();
    }

    public static class GemstoneMiningGUI extends UIElement {
        public GemstoneMiningGUI() {
            super("Gemstone GUI", new Point(0.2f, 0.0f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            try {
                if(mc.thePlayer == null || !Utils.inSkyblock || !getToggled() || !CrystalHollowsMap.inCrystalHollows) return;
                int total = 0;
                for(Gemstone gemstone:gemstones) {
                    if(PricingData.bazaarPrices.get(gemstone.item_name) != null) {
                        total += (int) (PricingData.bazaarPrices.get(gemstone.item_name)*gemstone.amount);
                    }
                }
                String[] lines = {
                    ChatFormatting.LIGHT_PURPLE+""+ChatFormatting.BOLD+"Gemstone Mining Info",
                    ChatFormatting.GRAY+" Time Spent Mining: "+ChatFormatting.GREEN+Utils.secondsToTime(seconds),
                    ChatFormatting.GRAY+" Coins Per hour: §6"+Utils.nf.format(total* 12L),
                    ChatFormatting.GRAY+" Pristine Count: "+ChatFormatting.AQUA+gemstones.size()
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
                ChatFormatting.GRAY+" Time Spent Mining: §a19m 27s",
                ChatFormatting.GRAY+" Coins Per hour: §6123,456",
                ChatFormatting.GRAY+" Pristine Count: "+ChatFormatting.AQUA+"3"
            };

            GuiUtils.drawTextLines(Arrays.asList(lines),0,0, GuiUtils.TextStyle.DROP_SHADOW);
        }

        @Override
        public boolean getToggled() {
            return Utils.inSkyblock && SkyblockFeatures.config.gemstoneTracker && SkyblockInfo.map.equals("Dwarven Mines");
        }

        @Override
        public int getHeight() {
            return Utils.GetMC().fontRendererObj.FONT_HEIGHT*4;
        }

        @Override
        public int getWidth() {
            return Utils.GetMC().fontRendererObj.getStringWidth("Time Spent Mining: §a19m 27 s");
        }
    }
}
