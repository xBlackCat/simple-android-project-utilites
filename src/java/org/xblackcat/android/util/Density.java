package org.xblackcat.android.util;

/**
 * 27.09.13 15:31
 *
 * @author xBlackCat
 */
public enum Density {
    Unknown("nodpi"),
    Low("ldpi"),
    Medium("mdpi"),
    TV("tvdpi"),
    High("hdpi"),
    XHigh("xhdpi"),
    XXHigh("xxhdpi"),
    XXXHigh("xxxhdpi"),
    //    ----
    ;
    private final String abbr;

    Density(String abbr) {
        this.abbr = abbr;
    }

    public String getAbbr() {
        return abbr;
    }
}
