package org.xblackcat.android.service.image;

import android.graphics.Bitmap;

/**
 * 06.09.12 17:15
 *
 * @author xBlackCat
 */
public interface OnImageLoad {
    Bitmap postProcessor(Bitmap image);

    void loaded(Bitmap image);
}
