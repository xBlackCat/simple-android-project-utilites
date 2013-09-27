package org.xblackcat.android.service.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import org.xblackcat.android.util.IOUtils;
import org.xblackcat.android.util.UIUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 06.09.12 15:38
 *
 * @author xBlackCat
 */
public class ImageCache {
    public static final int THUMB_SIZE = 150;

    private static final String TAG = "ImageCache";
    private static final String TAG_LOADER = "ImageCache_Loader";
    private final Bitmap invalidImage;

    public static final String SUFFIX_THUMB = ".thumb";
    public static final String SUFFIX_REGULAR = "";
    private final Context ctx;
    private final ImageCacheDB readDB;

    private final ImageLoader loadProcessor = new ImageLoader();

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final LruCache<String, Bitmap> cacheImages;
    private final LruCache<String, Bitmap> cacheThumbs;

    public ImageCache(Context ctx, Bitmap invalidImage) {
        this.ctx = ctx;
        readDB = new ImageCacheDB(this.ctx);
        cacheImages = new ImageMemoryCache(ctx);
        cacheThumbs = new ImageMemoryCache(ctx);

        this.invalidImage = invalidImage;

        Thread loadingThread = new Thread(loadProcessor, TAG_LOADER);
        loadingThread.setDaemon(true);
        loadingThread.start();
    }

    public void getImage(boolean thumb, String url, final OnImageLoad onLoad) {
        loadProcessor.addToLoad(thumb, url, onLoad);
    }

    /**
     * Try to get an image from local cache only.
     *
     * @param thumb flag should be loaded a thumb or real image
     * @param url   image source url
     * @return cached bitmap or null if image is not cached
     */
    public Bitmap getImage(boolean thumb, String url) {
        Bitmap bitmap;
        if (thumb) {
            bitmap = getBitmapFromCache(url, cacheThumbs, SUFFIX_THUMB);
        } else {
            bitmap = getBitmapFromCache(url, cacheImages, SUFFIX_REGULAR);
        }

        return bitmap;
    }

    private Bitmap getBitmapFromCache(String url, LruCache<String, Bitmap> cache, String suffix) {
        // First - check a memory cache
        Bitmap bitmap;
        try {
            lock.readLock().lock();
            bitmap = cache.get(url);
        } finally {
            lock.readLock().unlock();
        }

        if (bitmap == null) {
            Log.d(TAG, "Image is not found in in-memory cache. Url: " + url);

            String fileName = readDB.getImageFileName(url);
            if (fileName != null) {
                Log.d(TAG, "Load image from file " + fileName + suffix + ". Url " + url);
                try {
                    bitmap = loadFromFile(fileName + suffix);
                } catch (IOException e) {
                    bitmap = null;
                    Log.d(TAG, "Failed to load image from file " + fileName + suffix + ". Url: " + url, e);
                }
            }
        }
        return bitmap;
    }

    private Bitmap loadFromFile(String fileName) throws IOException {
        BufferedInputStream in = new BufferedInputStream(ctx.openFileInput(fileName));
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPurgeable = true;
            return BitmapFactory.decodeStream(in, null, options);
        } finally {
            in.close();
        }
    }

    public void killPending() {
        loadProcessor.clearQueue();
    }

    public void purgeCache(String url) {
        String fileName = readDB.getImageFileName(url);

        if (fileName != null) {
            // Remove file
            readDB.removeImage(url);
            ctx.deleteFile(fileName + SUFFIX_REGULAR);
            ctx.deleteFile(fileName + SUFFIX_THUMB);
        }
    }

    protected final Bitmap checkCacheOrLoad(LruCache<String, Bitmap> cache, String suffix, String url) {
        Log.d(TAG, "Get an image by url " + url);
        Bitmap bitmap = getBitmapFromCache(url, cache, suffix);

        if (bitmap == null) {
            // Not found in cache
            bitmap = loadBitmap(url);
        }
        return bitmap;
    }

    private Bitmap loadBitmap(String url) {
        WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        int targetSize = Math.max(display.getHeight(), display.getWidth());
        Log.d(TAG, "Load image by url: " + url);

        Bitmap bitmap = null;
        String fileName;
        try {
            int sampleSize = 1;
            do {
                try {
                    bitmap = IOUtils.loadImage(url, sampleSize);

                    if (bitmap == null) {
                        Log.w(TAG, "Can't decode image by url [" + url + "].");
                        bitmap = invalidImage;
                        break;
                    }
                } catch (OutOfMemoryError e) {
                    Log.w(TAG, "Can't load large image by url [" + url + "] Try to load reduced one");
                    // Reduce image size by two and try to load again
                    sampleSize <<= 1;
                }
            } while (bitmap == null && sampleSize < 10);

            if (bitmap != null) {
                do {
                    fileName = generateFileName();
                } while (new File(fileName).exists());

                Log.d(TAG, "Save image to file " + fileName + ". Url: " + url);

                int height = bitmap.getHeight();
                int width = bitmap.getWidth();

                if (height > targetSize && width > targetSize) {
                    bitmap = UIUtils.scaleBitmap(bitmap, targetSize);
                }

                storeToFile(fileName + SUFFIX_REGULAR, bitmap);

                Bitmap thumbBitmap = UIUtils.scaleBitmap(bitmap, THUMB_SIZE);
                storeToFile(fileName + SUFFIX_THUMB, thumbBitmap);

                readDB.storeInCache(url, fileName);

                try {
                    lock.writeLock().lock();
                    cacheImages.put(url, bitmap);
                    cacheThumbs.put(url, thumbBitmap);
                } finally {
                    lock.writeLock().unlock();
                }
            }
        } catch (IOException e) {
            Log.w(TAG, "Can't load and image from internet. Url: " + url, e);
        } catch (Throwable e) {
            Log.e(TAG, "Can't load and image from internet. Url: " + url, e);
        }

        return bitmap;
    }

    private String generateFileName() {
        return UUID.randomUUID().toString().replaceAll("-", SUFFIX_REGULAR) + ".png";
    }

    private void storeToFile(String fileName, Bitmap bitmap) throws IOException {
        BufferedOutputStream out = new BufferedOutputStream(ctx.openFileOutput(fileName, Context.MODE_PRIVATE));
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } finally {
            out.close();
        }
    }

    public void clearInMemoryCache() {
        lock.writeLock().lock();
        try {
            cacheImages.evictAll();
            cacheThumbs.evictAll();
        } finally {
            lock.writeLock().unlock();
        }

    }

    private class ImageLoader implements Runnable {
        private final Map<String, LoadInfo> queue = new LinkedHashMap<>();
        private final Object lock = new Object();

        @Override
        public void run() {
            while (true) {
                try {
                    LoadInfo info;
                    synchronized (lock) {
                        while (queue.isEmpty()) {
                            lock.wait();
                        }

                        Iterator<LoadInfo> iterator = queue.values().iterator();
                        info = iterator.next();
                        iterator.remove();
                    }

                    Bitmap bitmap;
                    if (info.returnTumb) {
                        bitmap = checkCacheOrLoad(cacheThumbs, SUFFIX_THUMB, info.url);
                    } else {
                        bitmap = checkCacheOrLoad(cacheImages, SUFFIX_REGULAR, info.url);
                    }
                    info.postProcessor.loaded(bitmap);
                } catch (Exception e) {
                    if (e instanceof InterruptedException) {
                        Log.e(TAG_LOADER, "Loader thread interrupted", e);
                        break;
                    } else {
                        Log.w(TAG_LOADER, "Unexpected exception", e);
                    }
                }
            }
        }

        public boolean addToLoad(boolean thumb, String url, OnImageLoad onLoad) {
            synchronized (lock) {
                if (queue.containsKey(url)) {
                    return false;
                } else {
                    queue.put(url, new LoadInfo(url, thumb, onLoad));
                    lock.notifyAll();
                    return true;
                }
            }
        }

        public void clearQueue() {
            synchronized (lock) {
                queue.clear();
                lock.notifyAll();
            }
        }

        private class LoadInfo {
            private final String url;
            private final boolean returnTumb;
            private final OnImageLoad postProcessor;

            private LoadInfo(String url, boolean returnTumb, OnImageLoad postProcessor) {
                this.url = url;
                this.returnTumb = returnTumb;
                this.postProcessor = postProcessor;
            }
        }
    }
}
