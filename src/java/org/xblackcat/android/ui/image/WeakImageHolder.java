package org.xblackcat.android.ui.image;

import android.graphics.Bitmap;
import android.widget.BaseAdapter;
import org.xblackcat.android.service.image.ImageCache;

import java.lang.ref.WeakReference;

/**
 * 05.04.13 17:56
 *
 * @author xBlackCat
 */
public abstract class WeakImageHolder<T> extends RefImageHolder<WeakReference<Bitmap>, T> {
    protected WeakImageHolder(T data, BaseAdapter adapter, ImageCache imageCache) {
        super(data, adapter, imageCache);
    }

    @Override
    protected WeakReference<Bitmap> coverWithReference(Bitmap image) {
        return new WeakReference<>(image);
    }
}
