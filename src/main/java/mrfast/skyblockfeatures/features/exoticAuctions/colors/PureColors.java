package mrfast.skyblockfeatures.features.exoticAuctions.colors;

import java.util.HashMap;
import java.util.Map;
/**
 * Taken from iTEM under GNU General Public License v2.0
 * https://github.com/TGWaffles/iTEM/blob/master/LICENSE
 *
 * @author TGWaffles
 */
public class PureColors {
    public static final Map<String, String> pureColorsToName = createPureColors();

    private static HashMap<String, String> createPureColors() {
        HashMap<String, String> pureColors = new HashMap<>();
        pureColors.put("993333", "RED");
        pureColors.put("D87F33", "ORANGE");
        pureColors.put("E5E533", "YELLOW");
        pureColors.put("7FCC19", "GREEN");
        pureColors.put("667F33", "DARK GREEN");
        pureColors.put("6699D8", "LIGHT BLUE");
        pureColors.put("4C7F99", "CYAN");
        pureColors.put("334CB2", "BLUE");
        pureColors.put("F27FA5", "PINK");
        pureColors.put("7F3FB2", "PURPLE");
        pureColors.put("B24CD8", "MAGENTA");
        pureColors.put("664C33", "BROWN");
        pureColors.put("FFFFFF", "WHITE");
        pureColors.put("999999", "LIGHT GRAY");
        pureColors.put("4C4C4C", "DARK GRAY");
        pureColors.put("191919", "BLACK");
        return pureColors;
    }

    public static boolean isPureColor(String hex) {
        return pureColorsToName.containsKey(hex.toUpperCase());
    }

    public static String getPureColor(String hex) {
        return pureColorsToName.get(hex.toUpperCase());
    }
}