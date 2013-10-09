package org.xblackcat.android.service.image;

/**
 * 27.09.13 15:31
 *
 * @author xBlackCat
 */
public enum Density {
    Unknown("nodpi", 0),
    Low("ldpi", 120),
    Medium("mdpi", 160),
    TV("tvdpi", 213),
    High("hdpi", 240),
    XHigh("xhdpi", 320),
    XXHigh("xxhdpi", 480),
    XXXHigh("xxxhdpi", 640),
    //    ----
    ;

    public static final Density Default = Medium;

    public static Density find(String density) {
        for (Density d : values()) {
            if (d.abbr.equalsIgnoreCase(density)) {
                return d;
            }
        }

        return null;
    }

    public static Density valueOf(int density) {
        for (Density d : values()) {
            if (d.density == density) {
                return d;
            }
        }

        return Unknown;
    }

    private final String abbr;
    private final int density;

    Density(String abbr, int density) {
        this.abbr = abbr;
        this.density = density;
    }

    public String getAbbr() {
        return abbr;
    }

    public int getDensity() {
        return density;
    }
}
