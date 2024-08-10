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
    
    public static String capitalizeAllWords(String string) {
        String resultString = "";
        boolean spaceBefore = true;
        for (int i = 0; i < string.length(); i++) {
            char charAt = string.charAt(i);
            if (Character.isWhitespace(charAt)) {
                spaceBefore = true;
            } else if (spaceBefore) {
                charAt = Character.toUpperCase(charAt);
                spaceBefore = false;
            }
            resultString += charAt;
        }
        return resultString;
    }

}
