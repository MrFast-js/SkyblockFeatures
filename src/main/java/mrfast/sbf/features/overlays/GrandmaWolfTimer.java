package mrfast.sbf.features.overlays;
import java.util.List;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.events.GuiContainerEvent;
import mrfast.sbf.events.SkyblockMobEvent;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.GuiUtils;
import mrfast.sbf.utils.ItemUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class GrandmaWolfTimer {
    public static Minecraft mc = Utils.GetMC();
    public static double SecondsRemaining = 0;
    public static double fiveComboSeconds = 0;
    public double TenComboSeconds = 0;
    public double FifteenComboSeconds = 0;
    public double TwentyComboSeconds = 0;
    public double TwentyFiveComboSeconds = 0;
    public double ThirtyComboSeconds = 0;
    public double currentCombo = 0;
    @SubscribeEvent
    public void onEntityDeath(SkyblockMobEvent.Death event) {
        if(!SkyblockFeatures.config.GrandmaWolfTimer) return;

        if(Utils.GetMC().thePlayer.getDistanceToEntity(event.getSbMob().skyblockMob)<10 && Utils.GetMC().thePlayer.canEntityBeSeen(event.getSbMob().skyblockMob)) {
            SecondsRemaining = currentCombo;
        }
    }

    @SubscribeEvent
    public void onDrawSlots(GuiContainerEvent.DrawSlotEvent.Pre event) {
        if(!SkyblockFeatures.config.GrandmaWolfTimer) return;

        if (event.gui instanceof GuiChest ) {
            GuiChest gui = (GuiChest) event.gui;
            ContainerChest chest = (ContainerChest) gui.inventorySlots;
            IInventory inv = chest.getLowerChestInventory();
            String chestName = inv.getDisplayName().getUnformattedText().trim();
            if(chestName.contains("Pets")) {
                if(event.slot.getHasStack()) {
                    ItemStack stack = event.slot.getStack();
                    if(stack.getDisplayName().contains("Grandma")) {
                        List<String> lore = ItemUtils.getItemLore(stack);
                        for(String line:lore) {
                            String raw = Utils.cleanColor(line);
                            if(!raw.contains("last")) continue;
                            String delayString = raw.split("last")[1];
                            double secondsDelay = Double.parseDouble(delayString.replaceAll("[^0-9]", ""))/10;
                            if(raw.contains("15 Combo")) {
                                FifteenComboSeconds=secondsDelay;
                            }
                            else if(raw.contains("20 Combo")) {
                                TwentyComboSeconds=secondsDelay;
                            }
                            else if(raw.contains("25 Combo")) {
                                TwentyFiveComboSeconds=secondsDelay;
                            }
                            else if(raw.contains("30 Combo")) {
                                ThirtyComboSeconds=secondsDelay;
                            }
                            else if(raw.contains("10 Combo")) {
                                TenComboSeconds=secondsDelay;
                            }
                            else if(raw.contains("5 Combo")) {
                                fiveComboSeconds=secondsDelay;
                            }
                        }
                        if(((int) (fiveComboSeconds*10))!=SkyblockFeatures.config.gMaWolf5Second) {
                            Utils.sendMessage(ChatFormatting.GREEN+"Updated Grandma Wolf combo times");
                        }
                        SkyblockFeatures.config.gMaWolf5Second=(int) (fiveComboSeconds*10);
                        SkyblockFeatures.config.gMaWolf10Second=(int) (TenComboSeconds*10);
                        SkyblockFeatures.config.gMaWolf15Second=(int) (FifteenComboSeconds*10);
                        SkyblockFeatures.config.gMaWolf20Second=(int) (TwentyComboSeconds*10);
                        SkyblockFeatures.config.gMaWolf25Second=(int) (TwentyFiveComboSeconds*10);
                        SkyblockFeatures.config.gMaWolf30Second=(int) (ThirtyComboSeconds*10);
                    }
                }
            }
        }
    }
    @SubscribeEvent
    public void onTick(ClientTickEvent event) {
        if(!SkyblockFeatures.config.GrandmaWolfTimer || Utils.GetMC().theWorld==null) return;

        if(fiveComboSeconds==0 && SkyblockFeatures.config.gMaWolf5Second!=0) {
            fiveComboSeconds= (double) SkyblockFeatures.config.gMaWolf5Second /10;
            TenComboSeconds= (double) SkyblockFeatures.config.gMaWolf10Second /10;
            FifteenComboSeconds= (double) SkyblockFeatures.config.gMaWolf15Second /10;
            TwentyComboSeconds= (double) SkyblockFeatures.config.gMaWolf20Second /10;
            TwentyFiveComboSeconds= (double) SkyblockFeatures.config.gMaWolf25Second /10;
            ThirtyComboSeconds= (double) SkyblockFeatures.config.gMaWolf30Second /10;
        }
        
        if(SecondsRemaining>0.05) SecondsRemaining-=0.05/2;
    }

    @SubscribeEvent
    public void onChatMessage(ClientChatReceivedEvent event) {
        if(!SkyblockFeatures.config.GrandmaWolfTimer) return;

        String raw = event.message.getUnformattedText();
        if(raw.contains("Your Kill Combo has expired! You reached a ")) {
            SecondsRemaining=0;
            currentCombo=0;
        }
        if(raw.contains("+5 Kill Combo")) {
            SecondsRemaining=fiveComboSeconds;
            currentCombo = fiveComboSeconds;
        }
        if(raw.contains("+10 Kill Combo")) {
            SecondsRemaining=TenComboSeconds;
            currentCombo = TenComboSeconds;
        }
        if(raw.contains("+15 Kill Combo")) {
            SecondsRemaining=FifteenComboSeconds;
            currentCombo = FifteenComboSeconds;
        }
        if(raw.contains("+20 Kill Combo")) {
            SecondsRemaining=TwentyComboSeconds;
            currentCombo = TwentyComboSeconds;
        }
        if(raw.contains("+25 Kill Combo")) {
            SecondsRemaining=TwentyFiveComboSeconds;
            currentCombo = TwentyFiveComboSeconds;
        }
        if(raw.contains("+30 Kill Combo")) {
            SecondsRemaining=ThirtyComboSeconds;
            currentCombo = ThirtyComboSeconds;
        }
    }

    static {
        new gWolfTimer();
    }   

    public static class gWolfTimer extends UIElement {
        public gWolfTimer() {
            super("Grandma Wolf Timer", new Point(0.2f, 0.0f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            if(mc.thePlayer == null || !Utils.inSkyblock || Utils.GetMC().theWorld==null || !SkyblockFeatures.config.GrandmaWolfTimer) return;
            double remaining = Math.floor(SecondsRemaining*100)/100;
            String time = (remaining+"").length()==3?remaining+"0":remaining+"";
            if(SecondsRemaining>0.05) {
                GuiUtils.drawText(ChatFormatting.GREEN+time+"s",0,0, GuiUtils.TextStyle.BLACK_OUTLINE);
            }  else if(fiveComboSeconds==0) {
                GuiUtils.drawText(ChatFormatting.RED+"Open Pets Menu",0,0, GuiUtils.TextStyle.BLACK_OUTLINE);
            }
        }

        @Override
        public void drawElementExample() {
            if(mc.thePlayer == null || !Utils.inSkyblock) return;
            GuiUtils.drawText(ChatFormatting.GREEN+"5.231s",0,0, GuiUtils.TextStyle.BLACK_OUTLINE);
        }

        @Override
        public boolean getToggled() {
            return SkyblockFeatures.config.GrandmaWolfTimer;
        }

        @Override
        public int getHeight() {
            return Utils.GetMC().fontRendererObj.FONT_HEIGHT;
        }

        @Override
        public int getWidth() {
            return Utils.GetMC().fontRendererObj.getStringWidth("5.231s");
        }
    }
}
