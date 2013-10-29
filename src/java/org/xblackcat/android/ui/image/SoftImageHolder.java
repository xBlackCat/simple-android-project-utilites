package org.xblackcat.android.ui.image;

import android.graphics.Bitmap;
import android.widget.BaseAdapter;
import org.xblackcat.android.service.image.ImageCache;

import java.lang.ref.SoftReference;

/**
 * 05.04.13 17:56
 *
 * @author xBlackCat
 */
public abstract class SoftImageHolder<T> extends RefImageHolder<SoftReference<Bitmap>, T> {
    protected SoftImageHolder(T data, BaseAdapter adapter, ImageCache imageCache) {
        super(data, adapter, imageCache);
    }

    @Override
    protected SoftReference<Bitmap> coverWithReference(Bitmap image) {
        return new SoftReference<>(image);
    }

}
