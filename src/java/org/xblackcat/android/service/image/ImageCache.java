package org.xblackcat.android.service.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import org.xblackcat.android.util.Density;
import org.xblackcat.android.util.IOUtils;
import org.xblackcat.android.util.ImageUrl;
import org.xblackcat.android.util.UIUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 06.09.12 15:38
 *
 * @author xBlackCat
 */
public class ImageCache {
    private static final String TAG = "ImageCache";
    private static final String TAG_LOADER = "ImageCache_Loader";
    private final Density systemDensity;
    private Bitmap invalidImage;

    private final Context ctx;
    private final ImageCacheDB readDB;

    private final ImageLoader loadProcessor = new ImageLoader();

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final LruCache<String, Bitmap> cacheImages;

    public ImageCache(Context ctx) {
        this.ctx = ctx;

        readDB = new ImageCacheDB(this.ctx);
        cacheImages = new ImageMemoryCache(ctx);

        systemDensity = UIUtils.getSystemDensity(ctx);

        Thread loadingThread = new Thread(loadProcessor, TAG_LOADER);
        loadingThread.setDaemon(true);
        loadingThread.start();
    }

    public static void cleanImages(Context ctx) {
        ImageCacheDB db = new ImageCacheDB(ctx);

        for (String s : db.getImageFileNames()) {
            ctx.deleteFile(s);
        }

        ImageCacheDB.drop(ctx);
    }

    /**
     * Loads an image by URL and stores it in cache
     */
    public void getImage(ImageUrl url, final OnImageLoad onLoad) {
        loadProcessor.addToLoad(url, onLoad);
    }

    /**
     * Loads an image by URL and stores it in cache
     */
    public void getImage(String url, final OnImageLoad onLoad) {
        getImage(new ImageUrl(url), onLoad);
        }

    public Bitmap getInvalidImage() {
        return invalidImage;
    }

    public Bitmap getBitmapFromCache(String url) {
        if (url == null) {
            return null;
        }

        // First - check a memory cache
        Bitmap bitmap;
        try {
            lock.readLock().lock();
            bitmap = cacheImages.get(url);
        } finally {
            lock.readLock().unlock();
        }

        if (bitmap == null) {
            Log.d(TAG, "Image is not found in in-memory cache. Url: " + url);

            String fileName = readDB.getImageFileName(url);
            if (fileName != null) {
                Log.d(TAG, "Load image from file " + fileName + ". Url " + url);
                try {
                    bitmap = loadFromFile(fileName);
                } catch (IOException e) {
                    bitmap = null;
                    Log.d(TAG, "Failed to load image from file " + fileName + ". Url: " + url, e);
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
            ctx.deleteFile(fileName);
        }
    }

    public final Bitmap checkCacheOrLoad(ImageUrl url) {
        Log.d(TAG, "Get an image by url " + url);
        if (url == null) {
            return null;
        }

        Bitmap bitmap = getBitmapFromCache(url.getUrl());

        if (bitmap == null) {
            // Not found in cache
            bitmap = loadBitmap(url);
        }
        return bitmap;
    }

    public Bitmap loadBitmap(ImageUrl url) {
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
                    bitmap = IOUtils.loadImage(url.getUrl(), sampleSize);

                    if (bitmap == null) {
                        Log.w(TAG, "Can't decode image by url [" + url + "].");
                        bitmap = null;
                        break;
                    }
                } catch (OutOfMemoryError e) {
                    Log.w(TAG, "Can't load large image by url [" + url + "] Try to load reduced one");
                    // Reduce image size by two and try to load again
                    sampleSize <<= 1;
                }
            } while (bitmap == null && sampleSize < 10);

            if (bitmap != null) {
                Density sourceDensity = url.getDensity();
                if (sourceDensity != null && sourceDensity != systemDensity) {
                    // Rescale image

                    int newWidth = bitmap.getWidth() * systemDensity.getDensity() / sourceDensity.getDensity();
                    int newHeight = bitmap.getHeight() * systemDensity.getDensity() / sourceDensity.getDensity();

                    bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
                }


                do {
                    fileName = generateFileName();
                } while (new File(fileName).exists());

                Log.d(TAG, "Save image to file " + fileName + ". Url: " + url);

                int height = bitmap.getHeight();
                int width = bitmap.getWidth();

                if (height > targetSize && width > targetSize) {
                    bitmap = UIUtils.scaleBitmap(bitmap, targetSize);
                }

                storeToFile(fileName, bitmap);

                readDB.storeInCache(url.getUrl(), fileName);

                try {
                    lock.writeLock().lock();
                    cacheImages.put(url.getUrl(), bitmap);
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
        return UUID.randomUUID().toString().replaceAll("-", "") + ".png";
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
        } finally {
            lock.writeLock().unlock();
        }

    }

    public void setInvalidImage(Bitmap noImageBitmap) {
        this.invalidImage = noImageBitmap;
    }

    private class ImageLoader implements Runnable {
        private final Queue<LoadInfo> queue = new PriorityQueue<>(
                5,
                new Comparator<LoadInfo>() {
                    @Override
                    public int compare(LoadInfo loadInfo, LoadInfo loadInfo2) {
                        int c = loadInfo.postProcessor.size() - loadInfo2.postProcessor.size();
                        if (c != 0) {
                            return 0;
                        }

                        if (loadInfo.timeline > loadInfo2.timeline) {
                            return 1;
                        } else if (loadInfo.timeline < loadInfo2.timeline) {
                            return -1;
                        } else {
                            return 0;
                        }
                    }
                }
        );
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

                        info = queue.poll();
                    }

                    Bitmap bitmap = checkCacheOrLoad(info.url);
                    for (OnImageLoad pp : info.postProcessor) {
                        pp.loaded(bitmap);
                    }
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

        public boolean addToLoad(ImageUrl url, OnImageLoad onLoad) {
            synchronized (lock) {
                for (LoadInfo i : queue) {
                    if (i.url.equals(url)) {
                        i.postProcessor.add(onLoad);
                        return false;
                    }
                }

                queue.add(new LoadInfo(url, onLoad));
                lock.notifyAll();
                return true;
            }
        }

        public void clearQueue() {
            synchronized (lock) {
                queue.clear();
                lock.notifyAll();
            }
        }

        private class LoadInfo {
            private final long timeline = System.currentTimeMillis();
            private final ImageUrl url;
             private final List<OnImageLoad> postProcessor;

            private LoadInfo(ImageUrl url, OnImageLoad postProcessor) {
                this.url = url;
                this.postProcessor = new ArrayList<>();
                this.postProcessor.add(postProcessor);
            }
        }
    }
}
