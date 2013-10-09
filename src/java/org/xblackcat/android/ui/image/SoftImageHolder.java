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
public abstract class SoftImageHolder<T> extends ImageHolder<T> {
    private SoftReference<Bitmap> image;

    protected SoftImageHolder(T data, BaseAdapter adapter, ImageCache imageCache) {
        super(data, adapter, imageCache);
    }

    @Override
    protected void setImage(Bitmap image) {
        if (image == null) {
            image = cache.getInvalidImage();
        }

        this.image = new SoftReference<>(image);
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
