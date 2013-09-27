package org.xblackcat.android.service.storage;

import android.content.ContextWrapper;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 17.05.13 11:21
 *
 * @author xBlackCat
 */
public class SaverTask<T extends Serializable> extends AsyncTask<T, Void, Void> {
    private static final String TAG = "SaverTask";
    private static final ReentrantLock saverLock = new ReentrantLock(true);

    private final IInfoAccessor<T> infoAccessor;
    private final ContextWrapper ctx;

    public SaverTask(ContextWrapper ctx, IInfoAccessor<T> infoAccessor) {
        this.infoAccessor = infoAccessor;
        this.ctx = ctx;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Void doInBackground(T... objects) {
        try {
            saverLock.lock();
            try {
                for (T o : objects) {
                    infoAccessor.store(ctx, o);
                }
            } finally {
                saverLock.unlock();
            }
        } catch (IOException e) {
            Log.e(TAG, "Can't store user favorites", e);
        }

        return null;
    }
}
