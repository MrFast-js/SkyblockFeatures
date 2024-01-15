package mrfast.sbf.features.misc;

import java.util.ArrayList;
import java.util.List;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.events.SecondPassedEvent;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.ItemUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;


public class CropCounter {
    private static final Minecraft mc = Minecraft.getMinecraft();
    static String count = "0";
    static int oldCount = 0;
    static int cropsPerSecond = 0;
    static int ticks = 0;
    static List<Integer> averageCropsValues = new ArrayList<>();
    @SubscribeEvent
    public void onSecond(SecondPassedEvent event) {
        if(mc.thePlayer == null||!SkyblockFeatures.config.Counter||!Utils.inSkyblock) return;
        if(!averageCropsValues.isEmpty()) {
            int total = 0;
            for (Integer averageCropsValue : averageCropsValues) {
                total += averageCropsValue;
            }
            cropsPerSecond = total/averageCropsValues.size();
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if(mc.thePlayer == null||!SkyblockFeatures.config.Counter||!Utils.inSkyblock) return;
        ItemStack item = mc.thePlayer.getHeldItem();

        if(item == null || !item.getDisplayName().contains("Hoe")) return;

        ticks++;
        if(!averageCropsValues.isEmpty() && ticks >= 10 && averageCropsValues.size()>5) {
            averageCropsValues.remove(0);
        }
        List<String> lore = ItemUtils.getItemLore(item);
        for (String line : lore) {
            if (line.contains("Counter: ")) {
                count = line.replace("Counter: ", "");
                int counter = Integer.parseInt(count.replaceAll("[^0-9]", ""));

                if (ticks >= 10) {
                    ticks = 0;
                    if (oldCount != 0) {
                        averageCropsValues.add((counter - oldCount) * 2);
                    }
                    oldCount = counter;
                }
            }
        }
    }
    
    static {
        new CropCounterGui();
    }   
    public static class CropCounterGui extends UIElement {
        public CropCounterGui() {
            super("Crop Counter", new Point(0.43951935f, 0.6869489f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            try {
                ItemStack item = mc.thePlayer.getHeldItem();
                if(item == null) return;
                String hoes = "Euclid Gauss Pythagorean Turing Newton";
                for(String hoe: hoes.split(" ")) {
                    if(Utils.cleanColor(item.getDisplayName()).contains(hoe)) {
                        mc.fontRendererObj.drawStringWithShadow(ChatFormatting.RED+"Counter: "+ChatFormatting.YELLOW+count, 0, 0, 0xFFFFFF);
                        mc.fontRendererObj.drawStringWithShadow(ChatFormatting.RED+"Crops Per Second: "+ChatFormatting.YELLOW+cropsPerSecond, 0, Utils.GetMC().fontRendererObj.FONT_HEIGHT, 0xFFFFFF);
                    }
                }
            } catch (Exception e) {
                //TODO: handle exception
            }
        }
        @Override
        public void drawElementExample() {
            mc.fontRendererObj.drawStringWithShadow(ChatFormatting.RED+"Counter: "+ChatFormatting.YELLOW+"19,302", 0, 0, 0xFFFFFF);
            mc.fontRendererObj.drawStringWithShadow(ChatFormatting.RED+"Crops Per Second: "+ChatFormatting.YELLOW+"0", 0, Utils.GetMC().fontRendererObj.FONT_HEIGHT, 0xFFFFFF);   
        }

        @Override
        public boolean getToggled() {
            return SkyblockFeatures.config.Counter;
        }

        @Override
        public boolean getRequirement() {
            return Utils.inSkyblock;
        }

        @Override
        public int getHeight() {
            return Utils.GetMC().fontRendererObj.FONT_HEIGHT*2;
        }

        @Override
        public int getWidth() {
            return 12 + Utils.GetMC().fontRendererObj.getStringWidth(ChatFormatting.RED+"Crops Per Second: "+ChatFormatting.YELLOW+"102");
        }
    }
}
