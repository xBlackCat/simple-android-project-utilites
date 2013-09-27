package org.xblackcat.android.service.image;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

/**
 * 16.11.12 17:16
 *
 * @author xBlackCat
 */
public class ImageMemoryCache extends LruCache<String, Bitmap> {
    public ImageMemoryCache(Context context) {
        super(1024 * 1024 * (((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass() >> 2));
    }

    @Override
    protected int sizeOf(String key, Bitmap value) {
        return value.getRowBytes() * value.getHeight();
    }
}
