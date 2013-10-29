package org.xblackcat.android.service.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import org.xblackcat.android.util.IOUtils;
import org.xblackcat.android.util.UIUtils;

import java.io.*;
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
    private final int maximumSize;

    public ImageCache(Context ctx) {
        this.ctx = ctx;

        readDB = new ImageCacheDB(this.ctx);
        cacheImages = new ImageMemoryCache(ctx);

        systemDensity = UIUtils.getSystemDensity(ctx);
        WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        maximumSize = Math.max(display.getHeight(), display.getWidth());

        loadProcessor.execute();
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
    public void getImage(String url, final OnImageLoad onLoad) {
        getImage(url == null ? null : new ImageUrl(url), onLoad);
    }

    /**
     * Loads an image by URL and stores it in cache. After image is loaded a {@linkplain org.xblackcat.android.service.image.OnImageLoad} handler will be invoked.
     * {@linkplain org.xblackcat.android.service.image.OnImageLoad#loaded(android.graphics.Bitmap)} is invoked in UI thread so there is no need for additional synchronization.
     */
    public void getImage(ImageUrl url, final OnImageLoad onLoad) {
        if (url == null || url.getUrl() == null) {
            // Force to inform about 'no image'
            onLoad.loaded(null);
            return;
        }

        Bitmap bitmap = getBitmapFromMemory(url.getUrl());
        if (bitmap != null) {
            onLoad.loaded(onLoad.postProcessor(bitmap));
            return;
        }

        loadProcessor.addToLoad(url, onLoad);
    }

    public Bitmap getInvalidImage() {
        return invalidImage;
    }

    public Bitmap getBitmapFromCache(String url) {
        if (url == null) {
            return null;
        }
        Bitmap bitmap = getBitmapFromMemory(url);

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

    private Bitmap getBitmapFromMemory(String url) {
        // First - check a memory cache
        Bitmap bitmap;
        try {
            lock.readLock().lock();
            bitmap = cacheImages.get(url);
        } finally {
            lock.readLock().unlock();
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

    public final void preLoadImageAsIs(String urlString) throws IOException {
        String fileName;

        do {
            fileName = generateFileName();
        } while (new File(fileName).exists());

        Log.d(TAG, "Load image to file " + fileName + " from URL " + urlString);

        InputStream is = new BufferedInputStream(IOUtils.getInputStream(urlString, false));
        try {
            OutputStream out = new BufferedOutputStream(ctx.openFileOutput(fileName, Context.MODE_PRIVATE));
            try {
                IOUtils.copy(is, out);
            } finally {
                out.close();
            }
        } finally {
            is.close();
        }

        readDB.storeInCache(urlString, fileName);
    }

    public Bitmap loadBitmap(ImageUrl url) {
        if (url == null || url.getUrl() == null) {
            return null;
        }

        String urlString = url.getUrl();
        Log.d(TAG, "Load image by url: " + url);

        Bitmap bitmap = null;
        String fileName;
        try {
            int sampleSize = 1;
            do {
                try {
                    bitmap = IOUtils.loadImage(urlString, sampleSize);

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

                int height = bitmap.getHeight();
                int width = bitmap.getWidth();

                if (height > maximumSize && width > maximumSize) {
                    bitmap = UIUtils.scaleBitmap(bitmap, maximumSize);
                }

                do {
                    fileName = generateFileName();
                } while (new File(fileName).exists());

                Log.d(TAG, "Save image to file " + fileName + ". Url: " + urlString);

                storeToFile(fileName, bitmap);

                readDB.storeInCache(urlString, fileName);

                try {
                    lock.writeLock().lock();
                    cacheImages.put(urlString, bitmap);
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

    private static String generateFileName() {
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

    private class ImageLoader extends AsyncTask<Void, Runnable, Void> {
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
        protected Void doInBackground(Void... params) {
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
                    for (final OnImageLoad pp : info.postProcessor) {
                        // Run each onLoad processor in separate task. Just in case.
                        final Bitmap image;
                        if (bitmap != null) {
                            image = pp.postProcessor(bitmap);
                        } else {
                            image = null;
                        }
                        publishProgress(
                                new Runnable() {
                                    public void run() {
                                        pp.loaded(image);
                                    }
                                }
                        );
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

            return null;
        }

        @Override
        protected void onProgressUpdate(Runnable... values) {
            for (Runnable r : values) {
                try {
                    r.run();
                } catch (Exception e) {
                    Log.e(TAG, "Unexpected exception while loading an image", e);
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
