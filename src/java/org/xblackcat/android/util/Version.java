package org.xblackcat.android.util;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 23.10.12 17:59
 *
 * @author xBlackCat
 */
public class Version implements Comparable<Version>, Serializable {
    private static final Pattern EXTRACT_DIGITS = Pattern.compile(".*?(\\d+).*");

    private final int[] version;

    public Version(String verString) {
        final String[] parts = verString.split("\\.");
        version = new int[parts.length];

        for (int i = 0; i < version.length; i++) {
            Matcher m = EXTRACT_DIGITS.matcher(parts[i]);
            if (m.find()) {
                version[i] = Integer.parseInt(m.group(1));
            }
        }
    }

    @Override
    public int compareTo(Version version) {
        int i = 0;
        while (i < this.version.length && i < version.version.length) {
            final int c = this.version[i] - version.version[i];
            if (c != 0) {
                return c > 0 ? 1 : -1;
            }

            i++;
        }

        final int c = this.version.length - version.version.length;
        if (c == 0) {
            return 0;
        } else {
            return c > 0 ? 1 : -1;
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("v");
        int i = 0;
        while (i < version.length) {
            sb.append(i == 0 ? "" : ".");
            sb.append(version[i]);
            ++i;
        }
        return sb.toString();
    }
}
