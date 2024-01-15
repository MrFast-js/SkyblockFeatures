package mrfast.sbf.features.overlays;

import com.mojang.realmsclient.gui.ChatFormatting;
import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.SkyblockInfo;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.GuiUtils;
import mrfast.sbf.utils.ItemUtils;
import mrfast.sbf.utils.RenderUtil;
import mrfast.sbf.utils.Utils;
import net.minecraft.item.ItemStack;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FinalDestinationOverlay {
    static {
        new FinalDestinationOverlayGui();
    }
    public static class FinalDestinationOverlayGui extends UIElement {
        public FinalDestinationOverlayGui() {
            super("Final Destination Armor Display", new Point(0.0f, 0.26018518f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }
        @Override
        public void drawElement() {
            drawArmor(false);
        }

        @Override
        public void drawElementExample() {
            drawArmor(true);
        }

        @Override
        public boolean getToggled() {
            return SkyblockFeatures.config.finalDestinationArmorDisplay;
        }

        @Override
        public boolean getRequirement() {
            return Utils.inSkyblock && SkyblockInfo.map.equals("The End");
        }

        @Override
        public int getHeight() {
            return 68;
        }

        @Override
        public int getWidth() {
            return Utils.GetMC().fontRendererObj.getStringWidth("Equip Final Destination Piece");
        }
    }

    public static void drawArmor(boolean example) {
        int drawnPieces = 0;
        for (int i = 0; i < 4; i++) {
            ItemStack stack = Utils.GetMC().thePlayer.getCurrentArmor(3-i);

            if(stack!=null && ItemUtils.getSkyBlockItemID(stack).startsWith("FINAL_DESTINATION")) {
                String currentBonus = "";
                int currentKills = 0;
                int nextUpgradeKills = 0;
                for (String line : ItemUtils.getItemLore(stack)) {
                    line = Utils.cleanColor(line);
                    if(line.startsWith("Next Upgrade: ")) {
                        String regex = "\\((\\d{1,3}(?:,\\d{3})*?)/(\\d{1,3}(?:,\\d{3})*?)\\)";
                        Pattern pattern = Pattern.compile(regex);
                        Matcher matcher = pattern.matcher(line);
                        if (matcher.find()) {
                            currentKills = Integer.parseInt(matcher.group(1).replaceAll(",",""));
                            nextUpgradeKills = Integer.parseInt(matcher.group(2).replaceAll(",",""));
                        }
                    }
                    if(line.startsWith("Piece Bonus: ")) {
                        String[] parts = line.split("\\+");
                        if (parts.length > 1) {
                            currentBonus = parts[1].trim();
                        }
                    }
                }
                RenderUtil.renderItemStackOnScreen(stack,0,i*16,16,16);
                String percentage = Utils.round(((double) currentKills / nextUpgradeKills)*100,2)+"%";
                if(SkyblockFeatures.config.finalDestinationArmorDisplayKills) {
                    percentage = "§8(§a"+Utils.nf.format(currentKills)+"§7/§c"+Utils.nf.format(nextUpgradeKills)+"§8)";
                }
                String display = ChatFormatting.AQUA+" "+percentage+" "+ChatFormatting.GREEN+currentBonus;
                GuiUtils.drawText(display,18,(i*16)+4, GuiUtils.TextStyle.DROP_SHADOW);
                drawnPieces++;
            }
        }
        if(drawnPieces==0 && example) {
            GuiUtils.drawText(ChatFormatting.RED+"Equip Final Destination Piece!",0,0, GuiUtils.TextStyle.DROP_SHADOW);
        }
    }
}
