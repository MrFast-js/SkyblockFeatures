package mrfast.sbf.features.overlays;

import java.util.List;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.DataManager;
import mrfast.sbf.events.GuiContainerEvent;
import mrfast.sbf.events.ProfileSwapEvent;
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
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class GrandmaWolfTimer {
    public static Minecraft mc = Utils.GetMC();
    public static double SecondsRemaining = 0;
    public static double FiveComboSeconds = 0;
    public static double TenComboSeconds = 0;
    public static double FifteenComboSeconds = 0;
    public static double TwentyComboSeconds = 0;
    public static double TwentyFiveComboSeconds = 0;
    public static double ThirtyComboSeconds = 0;
    public static double currentCombo = 0;

    @SubscribeEvent
    public void onProfileSwap(ProfileSwapEvent event) {
        FiveComboSeconds = (double) DataManager.getProfileDataDefault("grandmaWolfTimes.FiveComboSeconds", 0d) - 2;
        TenComboSeconds = (double) DataManager.getProfileDataDefault("grandmaWolfTimes.TenComboSeconds", 0d) - 2;
        FifteenComboSeconds = (double) DataManager.getProfileDataDefault("grandmaWolfTimes.FifteenComboSeconds", 0d) - 2;
        TwentyComboSeconds = (double) DataManager.getProfileDataDefault("grandmaWolfTimes.TwentyComboSeconds", 0d) - 2;
        TwentyFiveComboSeconds = (double) DataManager.getProfileDataDefault("grandmaWolfTimes.TwentyFiveComboSeconds", 0d) - 2;
        ThirtyComboSeconds = (double) DataManager.getProfileDataDefault("grandmaWolfTimes.ThirtyComboSeconds", 0d) - 2;
    }


    @SubscribeEvent
    public void onEntityDeath(SkyblockMobEvent.Death event) {
        if (!SkyblockFeatures.config.GrandmaWolfTimer) return;

        if (Utils.GetMC().thePlayer.getDistanceToEntity(event.getSbMob().skyblockMob) < 10 && Utils.GetMC().thePlayer.canEntityBeSeen(event.getSbMob().skyblockMob)) {
            SecondsRemaining = currentCombo;
        }
    }

    @SubscribeEvent
    public void onDrawSlots(GuiContainerEvent.DrawSlotEvent.Pre event) {
        if (!SkyblockFeatures.config.GrandmaWolfTimer) return;

        if (event.gui instanceof GuiChest) {
            GuiChest gui = (GuiChest) event.gui;
            ContainerChest chest = (ContainerChest) gui.inventorySlots;
            IInventory inv = chest.getLowerChestInventory();
            String chestName = inv.getDisplayName().getUnformattedText().trim();
            if (chestName.contains("Pets")) {
                if (event.slot.getHasStack()) {
                    ItemStack stack = event.slot.getStack();
                    if (stack.getDisplayName().contains("Grandma")) {
                        List<String> lore = ItemUtils.getItemLore(stack);
                        for (String line : lore) {
                            String raw = Utils.cleanColor(line);
                            if (!raw.contains("last")) continue;
                            String delayString = raw.split("last")[1];
                            double secondsDelay = Double.parseDouble(delayString.replaceAll("[^0-9]", "")) / 10;
                            if (raw.contains("15 Combo")) {
                                FifteenComboSeconds = secondsDelay;
                            } else if (raw.contains("20 Combo")) {
                                TwentyComboSeconds = secondsDelay;
                            } else if (raw.contains("25 Combo")) {
                                TwentyFiveComboSeconds = secondsDelay;
                            } else if (raw.contains("30 Combo")) {
                                ThirtyComboSeconds = secondsDelay;
                            } else if (raw.contains("10 Combo")) {
                                TenComboSeconds = secondsDelay;
                            } else if (raw.contains("5 Combo")) {
                                FiveComboSeconds = secondsDelay;
                            }
                        }

                        DataManager.saveProfileData("grandmaWolfTimes.FiveComboSeconds", FiveComboSeconds);
                        DataManager.saveProfileData("grandmaWolfTimes.TenComboSeconds", TenComboSeconds);
                        DataManager.saveProfileData("grandmaWolfTimes.FifteenComboSeconds", FifteenComboSeconds);
                        DataManager.saveProfileData("grandmaWolfTimes.TwentyComboSeconds", TwentyComboSeconds);
                        DataManager.saveProfileData("grandmaWolfTimes.TwentyFiveComboSeconds", TwentyFiveComboSeconds);
                        DataManager.saveProfileData("grandmaWolfTimes.ThirtyComboSeconds", ThirtyComboSeconds);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent event) {
        if (!SkyblockFeatures.config.GrandmaWolfTimer || Utils.GetMC().theWorld == null) return;
        if (event.phase != TickEvent.Phase.START) return;

        if (SecondsRemaining > 0.05) SecondsRemaining -= 0.05;
    }

    @SubscribeEvent
    public void onChatMessage(ClientChatReceivedEvent event) {
        if (!SkyblockFeatures.config.GrandmaWolfTimer) return;

        String raw = event.message.getUnformattedText();
        if (raw.contains("Your Kill Combo has expired! You reached a ")) {
            SecondsRemaining = 0;
            currentCombo = 0;
        }
        if (raw.contains("+5 Kill Combo")) {
            SecondsRemaining = FiveComboSeconds;
            currentCombo = FiveComboSeconds;
        }
        if (raw.contains("+10 Kill Combo")) {
            SecondsRemaining = TenComboSeconds;
            currentCombo = TenComboSeconds;
        }
        if (raw.contains("+15 Kill Combo")) {
            SecondsRemaining = FifteenComboSeconds;
            currentCombo = FifteenComboSeconds;
        }
        if (raw.contains("+20 Kill Combo")) {
            SecondsRemaining = TwentyComboSeconds;
            currentCombo = TwentyComboSeconds;
        }
        if (raw.contains("+25 Kill Combo")) {
            SecondsRemaining = TwentyFiveComboSeconds;
            currentCombo = TwentyFiveComboSeconds;
        }
        if (raw.contains("+30 Kill Combo")) {
            SecondsRemaining = ThirtyComboSeconds;
            currentCombo = ThirtyComboSeconds;
        }
    }

    static {
        new GrandmaWolfTimerGui();
    }

    public static class GrandmaWolfTimerGui extends UIElement {
        public GrandmaWolfTimerGui() {
            super("Grandma Wolf Pet Combo Timer", new Point(0.2828125f, 0.67777777f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            String seconds = Utils.msToSeconds((long) (SecondsRemaining * 1000d),2);
            String time = seconds.length() == 3 ? seconds + "0" : seconds;
            if (SecondsRemaining > 0.05) {
                GuiUtils.drawText(ChatFormatting.GREEN + time, 0, 0, GuiUtils.TextStyle.BLACK_OUTLINE);
            } else if (FiveComboSeconds == 0) {
                GuiUtils.drawText(ChatFormatting.RED + "Open Pets Menu", 0, 0, GuiUtils.TextStyle.BLACK_OUTLINE);
            }
        }

        @Override
        public void drawElementExample() {
            GuiUtils.drawText(ChatFormatting.GREEN + "5.23s", 0, 0, GuiUtils.TextStyle.BLACK_OUTLINE);
        }

        @Override
        public boolean getToggled() {
            return SkyblockFeatures.config.GrandmaWolfTimer;
        }

        @Override
        public boolean getRequirement() {
            return Utils.inSkyblock;
        }

        @Override
        public int getHeight() {
            return Utils.GetMC().fontRendererObj.FONT_HEIGHT;
        }

        @Override
        public int getWidth() {
            return Utils.GetMC().fontRendererObj.getStringWidth("5.23s");
        }
    }
}
