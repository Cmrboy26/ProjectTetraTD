package net.cmr.util;

import java.text.DecimalFormat;

public class StringUtils {
    
    public static String truncateFloatingPoint(float value, int places) {
        String format = "#.";
        for (int i = 0; i < places; i++) {
            format += "#";
        }
        return new DecimalFormat(format).format(value);
    }
    
}
