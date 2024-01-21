package mrfast.sbf.features.overlays;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.gui.components.Point;
import mrfast.sbf.gui.components.UIElement;
import mrfast.sbf.utils.GuiUtils;
import mrfast.sbf.utils.ScoreboardUtil;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;


public class SlayerKillsDisplay {
    private static final Minecraft mc = Minecraft.getMinecraft();

    static {
        new SlayerKillsDisplayGui();
    }

    public static class SlayerKillsDisplayGui extends UIElement {
        public SlayerKillsDisplayGui() {
            super("Slayer Kills Display", new Point(0.0f, 0.24930556f));
            SkyblockFeatures.GUIMANAGER.registerElement(this);
        }

        @Override
        public void drawElement() {
            drawSlayerKills();
        }

        @Override
        public void drawElementExample() {
            drawSlayerKills();
        }

        @Override
        public boolean getToggled() {
            return SkyblockFeatures.config.slayerKillDisplay;
        }

        @Override
        public boolean getRequirement() {
            return Utils.inSkyblock && shouldRender();
        }

        @Override
        public int getHeight() {
            return Utils.GetMC().fontRendererObj.FONT_HEIGHT*2;
        }

        @Override
        public int getWidth() {
            return Utils.GetMC().fontRendererObj.getStringWidth("24/667 Kills")*2;
        }
    }

    private static boolean shouldRender() {
        boolean slayerQuestActive = false;
        for (String sidebarLine : ScoreboardUtil.getSidebarLines(false)) {
            if (sidebarLine.equals("Slayer Quest")) {
                slayerQuestActive = true;
                break;
            }
        }
        return slayerQuestActive;
    }

    private static void drawSlayerKills() {
        String killString = "";
        boolean slayerQuestActive = false;
        if(shouldRender()) {
            for (String sidebarLine : ScoreboardUtil.getSidebarLines(false)) {
                if (sidebarLine.equals("Slayer Quest")) slayerQuestActive = true;
                if (sidebarLine.endsWith("Kills") && slayerQuestActive) {
                    killString = sidebarLine.replace("Kills", "§7Kills").trim();
                }
            }
        } else {
            killString = "§e1§7/§c600 §7Kills";
        }

        float scale = 2f;
        GlStateManager.scale(scale, scale, 0);
        GuiUtils.drawText(killString, 0, 0, GuiUtils.TextStyle.DROP_SHADOW);
        GlStateManager.scale(1/scale, 1/scale, 0);
    }
}
