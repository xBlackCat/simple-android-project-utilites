package org.xblackcat.android.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 28.09.13 14:30
 *
 * @author xBlackCat
 */
public class TextUtils {
    private static final Pattern TAG = Pattern.compile("<([^\"'>]|\"[^\"]*\"|'[^']*')+>");
    private static final Pattern SPACES = Pattern.compile("\\s+");

    public static String stripHtmlTags(String text) {
        if (text == null || text.length() == 0) {
            return "";
        }

        StringBuffer builder = new StringBuffer();

        Matcher matcher = TAG.matcher(text);
        while (matcher.find()) {
            matcher.appendReplacement(builder, " ");
        }
        matcher.appendTail(builder);

        String result = SPACES.matcher(builder.toString()).replaceAll(" ").trim();
        if (" ".equals(result)) {
            return "";
        } else {
            return result;
        }
    }
}
