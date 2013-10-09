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
public abstract class WeakImageHolder<T> extends ImageHolder<T> {
    private WeakReference<Bitmap> image;

    protected WeakImageHolder(T data, BaseAdapter adapter, ImageCache imageCache) {
        super(data, adapter, imageCache);
    }

    @Override
    protected void setImage(Bitmap image) {
        if (image == null) {
            image = cache.getInvalidImage();
        }

        this.image = new WeakReference<>(image);
    }

    @Override
    public Bitmap getImage() {
        if (image != null && image.get() != null) {
            return image.get();
        }

        if (getUrl() == null) {
            setImage(null);
            if (image != null && image.get() != null) {
                return image.get();
            }
        }

        loadImage();
        return null;
    }

}
