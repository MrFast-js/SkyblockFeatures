package mrfast.skyblockfeatures.features.misc;

import java.util.ArrayList;
import java.util.List;

import com.mojang.realmsclient.gui.ChatFormatting;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import mrfast.skyblockfeatures.SkyblockFeatures;

import mrfast.skyblockfeatures.gui.components.UIElement;
import mrfast.skyblockfeatures.events.SecondPassedEvent;
import mrfast.skyblockfeatures.utils.ItemUtil;
import mrfast.skyblockfeatures.utils.Utils;
import mrfast.skyblockfeatures.gui.components.Point;


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
        if(averageCropsValues.size()>0) {
            int total = 0;
            for(int i=0;i<averageCropsValues.size();i++) {
                total+=averageCropsValues.get(i);
            }
            cropsPerSecond = total/averageCropsValues.size();
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if(mc.thePlayer == null||!SkyblockFeatures.config.Counter||!Utils.inSkyblock) return;
        ItemStack item = mc.thePlayer.getHeldItem();
        if(item == null) return;
        if(!item.getDisplayName().contains("Hoe")) return;

        ticks++;
        if(averageCropsValues.size()>0 && ticks >= 10 && averageCropsValues.size()>5) {
            averageCropsValues.remove(0);
            System.out.println("removed index "+(0)+" new size:"+averageCropsValues.size());
        }
        List<String> lore = ItemUtil.getItemLore(item);
        for (int i = 0; i < lore.size(); i++) {
            String line = lore.get(i);
            if(line.contains("Counter: ")) {
                count = line.replace("Counter: ", "");
                int counter = Integer.parseInt(count.replaceAll("[^0-9]",""));
                
                if(ticks >= 10) {
                    ticks = 0;
                    if(oldCount == 0) {
                        oldCount = counter;
                    } else {
                        averageCropsValues.add((counter-oldCount)*2);
                        System.out.println("added "+((counter-oldCount)*2)+" new size:"+averageCropsValues.size());
                        oldCount = counter;
                    }
                }
            }
        }
    }
    
    static {
        new CropCounterGUI();
    }   
    public static class CropCounterGUI extends UIElement {
        public CropCounterGUI() {
            super("CropCounter", new Point(0, 5));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            if(mc.thePlayer == null) return;
            if (this.getToggled() && Minecraft.getMinecraft().thePlayer != null && mc.theWorld != null) {
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
        }
        @Override
        public void drawElementExample() {
            if(mc.thePlayer == null) return;
            mc.fontRendererObj.drawStringWithShadow(ChatFormatting.RED+"Counter: "+ChatFormatting.YELLOW+"19,302", 0, 0, 0xFFFFFF);   
            mc.fontRendererObj.drawStringWithShadow(ChatFormatting.RED+"Crops Per Second: "+ChatFormatting.YELLOW+"0", 0, Utils.GetMC().fontRendererObj.FONT_HEIGHT, 0xFFFFFF);   
        }

        @Override
        public boolean getToggled() {
            return Utils.inSkyblock && SkyblockFeatures.config.Counter;
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
