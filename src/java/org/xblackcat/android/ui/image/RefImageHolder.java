package org.xblackcat.android.ui.image;

import android.graphics.Bitmap;
import android.widget.BaseAdapter;
import org.xblackcat.android.service.image.ImageCache;

import java.lang.ref.Reference;

/**
 * 29.10.13 12:56
 *
 * @author xBlackCat
 */
public abstract class RefImageHolder<R extends Reference<Bitmap>, T> extends ImageHolder<T> {
    private R image;

    public RefImageHolder(T data, BaseAdapter adapter, ImageCache imageCache) {
        super(data, adapter, imageCache);
    }

    @Override
    protected void setImage(Bitmap image) {
        if (image == null) {
            image = cache.getInvalidImage();
        }

        this.image = coverWithReference(image);
    }

    protected abstract R coverWithReference(Bitmap image);

    @Override
    public Bitmap getImage() {
        if (image != null && image.get() != null) {
            return image.get();
        }

        if (getUrl() == null || getUrl().getUrl() == null) {
            setImage(null);
            if (image != null && image.get() != null) {
                return image.get();
            }
        }

        loadImage();
        return null;
    }
}
