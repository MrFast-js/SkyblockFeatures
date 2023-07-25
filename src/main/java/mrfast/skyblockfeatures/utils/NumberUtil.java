package mrfast.skyblockfeatures.utils;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Original code was taken from Skytils under GNU Affero General Public License v3.0
 *
 * @author Skytils Team
 * @link https://github.com/Skytils/SkytilsMod/blob/1.x/LICENSE.md
 */
public class NumberUtil {

    public static final NumberFormat nf = NumberFormat.getInstance(Locale.US);

    private static final String[] KMG = new String[] {"", "K", "M", "B","T","P","E"};

    public static String formatDbl(double d) {
        int i = 0;
        while (d >= 1000) { 
            i++; 
            d /= 1000; 
        }
        
        if (i > 0 && Math.floor(d) == d) {
            return String.valueOf((int) d) + KMG[i];
        } else if (Math.floor(d) == d) {
            return String.valueOf((int) d);
        } else {
            return round(d, 1) + KMG[i];
        }
    }



    /**
     * This code was unmodified and taken under CC BY-SA 3.0 license
     * @link https://stackoverflow.com/a/22186845
     * @author jpdymond
     */
    public static double round(double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

}
