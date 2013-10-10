package org.xblackcat.android.ui.image;

import android.graphics.Bitmap;
import android.widget.BaseAdapter;
import org.xblackcat.android.service.image.ImageCache;
import org.xblackcat.android.service.image.ImageUrl;
import org.xblackcat.android.service.image.OnImageLoad;

/**
 * Class for wrapping an data object to allow load images "on demand". Can be used in adapters for lazy image loading.
 *
 * @author xBlackCat
 */
public abstract class ImageHolder<T> {
    protected final T data;
    protected final BaseAdapter adapter;
    protected final ImageCache cache;
    private final OnImageLoad onLoad = new OnImageLoad() {
        @Override
        public void loaded(Bitmap loaded) {
            setImage(loaded);
            markAsSet();
        }

        @Override
        public Bitmap postProcessor(Bitmap image) {
            return adjustImage(image);
        }
    };

    private boolean loadInProgress = false;

    protected ImageHolder(T data, BaseAdapter adapter, ImageCache imageCache) {
        if (data == null) {
            throw new NullPointerException("Data can not be null");
        }

        this.data = data;
        this.cache = imageCache;
        this.adapter = adapter;
    }

    protected void preloadImage() {
        ImageUrl url = getUrl();
        if (url == null || url.getUrl() == null) {
            setImage(null);
        } else {
            Bitmap image = cache.getBitmapFromCache(url.getUrl());

            if (image != null) {
                setImage(adjustImage(image));
            }
        }
    }

    public T get() {
        return data;
    }

    public abstract Bitmap getImage();

    protected Bitmap adjustImage(Bitmap image) {
        return image;
    }

    protected abstract void setImage(Bitmap image);

    private void markAsSet() {
        loadInProgress = false;
        adapter.notifyDataSetChanged();
    }

    /**
     * Clears 'loading' flag for image. Image content will be requested again on demand if not yet loaded.
     */
    protected void clearLoadingFlag() {
        loadInProgress = false;
    }

    /**
     * Loads image by URL returned by {@linkplain #getUrl()}. If URL is null then instantly
     * {@linkplain #setImage(android.graphics.Bitmap)} will be invoked with null as bitmap value -
     * this is could be used as indicator of invalid image.
     */
    protected final void loadImage() {
        final ImageUrl url = getUrl();

        if (url == null) {
            setImage(null);
            markAsSet();
            return;
        }

        // Should be executed in UI thread so we could not use synchronization
        if (!loadInProgress) {
            loadInProgress = true;

            cache.getImage(url, onLoad);
        }
    }

    protected abstract ImageUrl getUrl();

    @Override
    public String toString() {
        return "ImageHolder: for " + data.toString();
    }
}
