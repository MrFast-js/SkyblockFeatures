package mrfast.sbf.utils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;

import java.util.*;
import java.util.stream.Collectors;

public class ScoreboardUtil {

    /**
     * Data from Skyhanni
     * Source: https://github.com/hannibal002/SkyHanni
     * @author CalMWolfs
     */
    private static final List<String> hypixelsWeirdIcons = Arrays.asList(
            "\uD83C\uDF6B", "\uD83D\uDCA3", "\uD83D\uDC7D", "\uD83D\uDD2E", "\uD83D\uDC0D",
            "\uD83D\uDC7E", "\uD83C\uDF20", "\uD83C\uDF6D", "⚽", "\uD83C\uDFC0", "\uD83D\uDC79",
            "\uD83C\uDF81", "\uD83C\uDF89", "\uD83C\uDF82", "\uD83D\uDD2B"
    );
    public static String fixFormatting(String input,Boolean cleanColor) {
        for (String weirdIcon : hypixelsWeirdIcons) {
            if (input.contains(weirdIcon)) {
                String[] parts = input.split(weirdIcon, 2);
                input = parts[0] + (parts.length > 1 ? parts[1].length()>2?parts[1].substring(2):"" : "");
            }
        }
        if(cleanColor) input = Utils.cleanColor(input);
        return input;
    }

    /**
     * Taken from Danker's Skyblock Mod and modified under GPL 3.0 license
     * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
     * @author bowser0000
     */
    public static List<String> getSidebarLines() {
        return getSidebarLines(true);
    }
    public static List<String> getSidebarLines(boolean clean) {
        List<String> lines = new ArrayList<>();
        if (Minecraft.getMinecraft().theWorld == null) return lines;
        Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();
        if (scoreboard == null) return lines;

        ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);

        if (objective == null) return lines;

        Collection<Score> scores = scoreboard.getSortedScores(objective);
        List<Score> list = scores.stream()
                .filter(input -> input != null && input.getPlayerName() != null && !input.getPlayerName()
                        .startsWith("#"))
                .collect(Collectors.toList());

        if (list.size() > 15) {
            scores = Lists.newArrayList(Iterables.skip(list, scores.size() - 15));
        } else {
            scores = list;
        }

        for (Score score : scores) {
            ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
            lines.add(fixFormatting(ScorePlayerTeam.formatPlayerName(team, score.getPlayerName()),clean));
        }
        // Reverse it so its from top to bottom
        lines.add(objective.getDisplayName());
        Collections.reverse(lines);
        return lines;
    }
}
