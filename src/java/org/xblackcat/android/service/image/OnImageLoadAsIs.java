package org.xblackcat.android.service.image;

import android.graphics.Bitmap;

/**
 * 09.10.13 16:26
 *
 * @author xBlackCat
 */
public abstract class OnImageLoadAsIs implements OnImageLoad {
    @Override
    public Bitmap postProcessor(Bitmap image) {
        return image;
    }
}
