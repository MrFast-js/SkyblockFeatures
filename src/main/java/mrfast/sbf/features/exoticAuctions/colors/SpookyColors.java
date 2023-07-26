package mrfast.sbf.features.exoticAuctions.colors;

import com.google.common.collect.ImmutableSet;

public class SpookyColors {
    public static final ImmutableSet<String> spookColorConstants = ImmutableSet.of(
            "000000", "070008", "0E000F", "150017", "1B001F", "220027", "29002E", "300036", "37003E", "3E0046",
            "45004D", "4C0055", "52005D", "590065", "60006C", "670074", "6E007C", "750084", "7C008B", "830093",
            "89009B", "9000A3", "9700AA", "993399", "9E00B2"
    );

    public static boolean isSpookColor(String hex) {
        return spookColorConstants.contains(hex.toUpperCase());
    }
}