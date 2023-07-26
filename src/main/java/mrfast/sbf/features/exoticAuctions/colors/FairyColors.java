package mrfast.sbf.features.exoticAuctions.colors;

import com.google.common.collect.ImmutableSet;
/**
 * Taken from iTEM under GNU General Public License v2.0
 * https://github.com/TGWaffles/iTEM/blob/master/LICENSE
 *
 * @author TGWaffles
 */
public class FairyColors {
    public static final ImmutableSet<String> fairyColorConstants = ImmutableSet.of(
            "330066", "4C0099", "660033", "660066", "6600CC", "7F00FF", "99004C", "990099", "9933FF", "B266FF",
            "CC0066", "CC00CC", "CC99FF", "E5CCFF", "FF007F", "FF00FF", "FF3399", "FF33FF", "FF66B2", "FF66FF",
            "FF99CC", "FF99FF", "FFCCE5", "FFCCFF"
    );

    public static final ImmutableSet<String> ogFairyColorConstants = ImmutableSet.of(
            "FF99FF", "FFCCFF", "E5CCFF", "CC99FF", "CC00CC", "FF00FF", "FF33FF", "FF66FF",
            "B266FF", "9933FF", "7F00FF", "660066", "6600CC", "4C0099", "330066", "990099"
    );

    public static final ImmutableSet<String> ogFairyColorBootsExtras = ImmutableSet.of(
            "660033", "99004C", "CC0066"
    );

    public static final ImmutableSet<String> ogFairyColorLeggingsExtras = ImmutableSet.of(
            "660033", "99004C", "FFCCE5"
    );

    public static final ImmutableSet<String> ogFairyColorChestplateExtras = ImmutableSet.of(
            "660033", "FFCCE5", "FF99CC"
    );

    public static final ImmutableSet<String> ogFairyColorHelmetExtras = ImmutableSet.of(
            "FFCCE5", "FF99CC", "FF66B2"
    );

    public static boolean isFairyColor(String hex) {
        return fairyColorConstants.contains(hex.toUpperCase());
    }

    public static boolean isOGFairyColor(String itemId, String hex) {
        hex = hex.toUpperCase();
        if (ogFairyColorConstants.contains(hex)) {
            return true;
        }

        if (itemId.contains("BOOTS")) {
            return ogFairyColorBootsExtras.contains(hex);
        }

        if (itemId.contains("LEGGINGS")) {
            return ogFairyColorLeggingsExtras.contains(hex);
        }

        if (itemId.contains("CHESTPLATE")) {
            return ogFairyColorChestplateExtras.contains(hex);
        }

        if (itemId.contains("HELMET")) {
            return ogFairyColorHelmetExtras.contains(hex);
        }

        return false;
    }
}