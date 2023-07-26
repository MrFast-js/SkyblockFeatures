package mrfast.sbf.features.exoticAuctions.colors;

import com.google.common.collect.ImmutableSet;
/**
 * Taken from iTEM under GNU General Public License v2.0
 * https://github.com/TGWaffles/iTEM/blob/master/LICENSE
 *
 * @author TGWaffles
 */
public class CrystalColors {
    public static final ImmutableSet<String> crystalColorsConstants = ImmutableSet.of(
            "1F0030", "46085E", "54146E", "5D1C78", "63237D", "6A2C82", "7E4196", "8E51A6", "9C64B3", "A875BD",
            "B88BC9", "C6A3D4", "D9C1E3", "E5D1ED", "EFE1F5", "FCF3FF"
    );

    public static boolean isCrystalColor(String hex) {
        return crystalColorsConstants.contains(hex.toUpperCase());
    }
}