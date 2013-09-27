package org.xblackcat.android.util;

/**
 * 11.08.11 17:39
 *
 * @author xBlackCat
 */
public enum ProgressBarStyle {
    Small(android.R.attr.progressBarStyleSmall),
    SmallInverse(android.R.attr.progressBarStyleSmallInverse),
    SmallTitle(android.R.attr.progressBarStyleSmallTitle),
    Normal(android.R.attr.progressBarStyle),
    NormalInverse(android.R.attr.progressBarStyleInverse),
    Large(android.R.attr.progressBarStyleLarge),
    LargeInverse(android.R.attr.progressBarStyleLargeInverse),
    //    ---
    ;

    protected final int styleResId;

    private ProgressBarStyle(int styleResId) {
        this.styleResId = styleResId;
    }
}
