package mrfast.sbf.features.mining;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mojang.realmsclient.gui.ChatFormatting;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.SkyblockInfo;
import mrfast.sbf.features.overlays.maps.CrystalHollowsMap;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.GuiUtils;
import mrfast.sbf.utils.TabListUtils;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;

public class CommisionsTracker {
    private static final Minecraft mc = Minecraft.getMinecraft();

    static {
        new CommissionsTrackerGui();
    }

    public static class CommissionsTrackerGui extends UIElement {

        public CommissionsTrackerGui() {
            super("Commissions Tracker", new Point(0.6484375f, 0.0037037036f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            ArrayList<String> text = new ArrayList<>();
            try {
                text.add(ChatFormatting.BLUE + "Commissions");
                List<String> commissions = new ArrayList<String>();
                commissions.add(mc.ingameGUI.getTabList().getPlayerName(TabListUtils.getTabEntries().get(50)));
                commissions.add(mc.ingameGUI.getTabList().getPlayerName(TabListUtils.getTabEntries().get(51)));

                if (!Utils.cleanColor(mc.ingameGUI.getTabList().getPlayerName(TabListUtils.getTabEntries().get(52))).isEmpty()) {
                    commissions.add(mc.ingameGUI.getTabList().getPlayerName(TabListUtils.getTabEntries().get(52)));
                }
                if (!Utils.cleanColor(mc.ingameGUI.getTabList().getPlayerName(TabListUtils.getTabEntries().get(53))).isEmpty()) {
                    commissions.add(mc.ingameGUI.getTabList().getPlayerName(TabListUtils.getTabEntries().get(53)));
                }
                for (String commission : commissions) {
                    commission = Utils.cleanColor(commission);
                    if (commission.contains("Forges")) continue;

                    Pattern regex = Pattern.compile("(\\d+(?:\\.\\d+)?)");
                    Matcher matcher = regex.matcher(commission);

                    // Stop the 2x from being accounted in the percentage
                    commission = commission.replace("2x", "");

                    if (matcher.find()) {
                        String[] a = commission.split(" ");
                        String amount = Math.round(getCommissionTotal(commission) * (Double.valueOf(matcher.group(1)) / 100)) + "";
                        String mid = ChatFormatting.LIGHT_PURPLE + "[" +
                                ChatFormatting.GREEN + amount +
                                ChatFormatting.GOLD + "/" +
                                ChatFormatting.GREEN + getCommissionTotal(commission) +
                                ChatFormatting.LIGHT_PURPLE + "]";
                        commission = commission.replace(a[a.length - 1], mid);
                    } else if (commission.contains("DONE")) {
                        commission = commission.replace("DONE", ChatFormatting.GREEN + "DONE");
                    }
                    text.add(ChatFormatting.AQUA + commission);
                }
            } catch (Exception ignored) {
            }

            GuiUtils.drawTextLines(text, 0, 0, GuiUtils.TextStyle.BLACK_OUTLINE);
        }

        @Override
        public void drawElementExample() {
            ArrayList<String> text = new ArrayList<>();
            text.add(ChatFormatting.BLUE + "Commissions");
            text.add(ChatFormatting.AQUA + " Upper Mines Titanium: " + ChatFormatting.LIGHT_PURPLE + "[" + ChatFormatting.GREEN + "7" + ChatFormatting.GOLD + "/" + ChatFormatting.GREEN + "10" + ChatFormatting.LIGHT_PURPLE + "]");
            text.add(ChatFormatting.AQUA + " Goblin Raid: " + ChatFormatting.LIGHT_PURPLE + "[" + ChatFormatting.GREEN + "0" + ChatFormatting.GOLD + "/" + ChatFormatting.GREEN + "1" + ChatFormatting.LIGHT_PURPLE + "]");

            GuiUtils.drawTextLines(text, 0, 0, GuiUtils.TextStyle.BLACK_OUTLINE);
        }

        @Override
        public boolean getToggled() {
            return SkyblockFeatures.config.CommisionsTracker;
        }

        @Override
        public boolean getRequirement() {
            return Utils.inSkyblock && (SkyblockInfo.map.equals("Dwarven Mines") || CrystalHollowsMap.inCrystalHollows);
        }

        @Override
        public int getHeight() {
            return Utils.GetMC().fontRendererObj.FONT_HEIGHT * 3;
        }

        @Override
        public int getWidth() {
            return Utils.GetMC().fontRendererObj.getStringWidth("2x Mithril Powder Collector [350/500] ");
        }
    }


    public static int getCommissionTotal(String str) {
        if (CrystalHollowsMap.inCrystalHollows) {
            if (str.contains("Hard Stone Miner") || str.contains("Gemstone Collector")) return 1000;
            if (str.contains("Chest Looter")) return 3;
            if (str.contains("Treasurite")) return 13;
            if (str.contains("Sludge")) return 25;
            if (str.contains("Yog") || str.contains("Automaton") || str.contains("Goblin Slayer")) return 13;
            if (str.contains("Thyst")) return 5;
            if (str.contains("Crystal Hunter")) return 1;
            if (str.contains("Corleone")) return 1;
        }
        if (str.contains("Ice Walker")) return 50;
        if (str.contains("Golden Goblin Slayer")) return 1;
        if (str.contains("Goblin Slayer")) return 100;
        if (str.contains("Powder Ghast Puncher")) return 5;
        if (str.contains("Star Sentry Puncher")) return 10;
        if (str.contains("2x Mithril Powder Collector")) return 500;

        if (str.contains("Raffle")) {
            if (str.contains("Lucky")) return 20;
            return 1;
        }
        if (str.contains("Goblin Raid")) {
            if (str.contains("Slayer")) return 20;
            return 1;
        }
        if (str.contains("Mithril")) {
            if (str.contains("Miner")) return 350;
            return 250;
        }
        if (str.contains("Titanium")) {
            if (str.contains("Miner")) return 15;
            return 10;
        }
        return -1;
    }

}
