package mrfast.sbf.mixins.transformers;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mojang.realmsclient.gui.ChatFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;

@Mixin(GuiIngame.class)
public class MixinGuiIngame {

    @Inject(method = "renderScoreboard", at = @At("HEAD"), cancellable = true)
    protected void renderScoreboard(ScoreObjective objective, ScaledResolution scaledRes, CallbackInfo ci) {
        if (!SkyblockFeatures.config.customSidebar) return;
        ci.cancel();
        Minecraft mc = Utils.GetMC();
        Scoreboard scoreboard = objective.getScoreboard();
        Collection<Score> collection = scoreboard.getSortedScores(objective);
        List<Score> list = Lists.newArrayList(Iterables.filter(collection, p_apply_1_ -> p_apply_1_.getPlayerName() != null && !p_apply_1_.getPlayerName().startsWith("#")));

        if (list.size() > 15) {
            collection = Lists.newArrayList(Iterables.skip(list, collection.size() - 15));
        } else {
            collection = list;
        }

        int i = mc.fontRendererObj.getStringWidth(objective.getDisplayName());

        for (Score score : collection) {
            ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(score.getPlayerName());
            String s = ScorePlayerTeam.formatPlayerName(scoreplayerteam, score.getPlayerName()) + ": " + EnumChatFormatting.RED + score.getScorePoints();
            i = Math.max(i, mc.fontRendererObj.getStringWidth(s));
        }

        int i1 = collection.size() * mc.fontRendererObj.FONT_HEIGHT;
        int j1 = scaledRes.getScaledHeight() / 2 + i1 / 3;
        int k1 = 3;
        int l1 = scaledRes.getScaledWidth() - i - k1;
        int j = 0;
        Score hypixelLine = null;


        for (Score score : collection) {
            ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(score.getPlayerName());
            String lineText = ScorePlayerTeam.formatPlayerName(scoreplayerteam, score.getPlayerName());
            if (SkyblockFeatures.config.hideHypixelFromSidebar && lineText.contains("www.hypixel") && score.getScorePoints() == 1) {
                hypixelLine = score;
            }
        }
        if (hypixelLine != null) {
            collection.remove(hypixelLine);
        }
        String serverIdRegex = "\\b([0-9]{2}/[0-9]{2}/[0-9]{2})\\s*[Mm]([0-9]+[A-Za-z0-9]*)\\b";
        Pattern serverIdPattern = Pattern.compile(serverIdRegex);

        for (Score score : collection) {
            ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(score.getPlayerName());
            String lineText = ScorePlayerTeam.formatPlayerName(scoreplayerteam, score.getPlayerName());
            ++j;
            String s2 = EnumChatFormatting.RED + "" + score.getScorePoints();
            int k = j1 - j * mc.fontRendererObj.FONT_HEIGHT;
            int l = scaledRes.getScaledWidth() - k1 + 2;

            if (!SkyblockFeatures.config.removeSidebarBackground) {
                Gui.drawRect(l1 - 2, k, l, k + mc.fontRendererObj.FONT_HEIGHT, 1342177280);
            }
            if(SkyblockFeatures.config.hideServerFromSidebar) {
                Matcher serverIdMatcher = serverIdPattern.matcher(Utils.cleanColor(lineText));
                if (serverIdMatcher.find()) {
                    lineText = ChatFormatting.GRAY + serverIdMatcher.group(1);
                }
            }

            // Handling normal lines
            if (SkyblockFeatures.config.useShadowOnSidebar) {
                mc.fontRendererObj.drawStringWithShadow(lineText, l1, k, 553648127);
            } else {
                mc.fontRendererObj.drawString(lineText, l1, k, 553648127);
            }

            // Stop the red numbers from the side
            if (!SkyblockFeatures.config.removeSidebarRedNumbers) {
                mc.fontRendererObj.drawString(s2, l - mc.fontRendererObj.getStringWidth(s2), k, 553648127);
            }

            if (j == collection.size()) {
                String s3 = objective.getDisplayName();
                if (!SkyblockFeatures.config.removeSidebarBackground) {
                    Gui.drawRect(l1 - 2, k - mc.fontRendererObj.FONT_HEIGHT - 1, l, k - 1, 1610612736);
                    Gui.drawRect(l1 - 2, k - 1, l, k, 1342177280);
                }
                if (SkyblockFeatures.config.useShadowOnSidebar)
                    mc.fontRendererObj.drawStringWithShadow(s3, l1 + (float) i / 2 - (float) mc.fontRendererObj.getStringWidth(s3) / 2, k - mc.fontRendererObj.FONT_HEIGHT, 553648127);
                else
                    mc.fontRendererObj.drawString(s3, l1 + i / 2 - mc.fontRendererObj.getStringWidth(s3) / 2, k - mc.fontRendererObj.FONT_HEIGHT, 553648127);
            }
        }
    }
}
