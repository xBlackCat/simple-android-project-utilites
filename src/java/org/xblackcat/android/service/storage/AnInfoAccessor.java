package org.xblackcat.android.service.storage;

import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;

import java.io.*;
import java.util.Date;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 28.08.12 16:48
 *
 * @author xBlackCat
 */
public abstract class AnInfoAccessor<T extends Serializable> implements IInfoAccessor<T> {
    private static final String DATE_SUFFIX = ".date";
    private final String TAG = getClass().getName();
    private final String fileName;

    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    protected AnInfoAccessor(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public T load(ContextWrapper ctx) throws IOException {
        try {
            lock.readLock().lock();
            try {
                ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(ctx.openFileInput(fileName)));

                try {
                    return readStream(ctx, is);
                } finally {
                    is.close();
                }
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "Can't load tweets from cache", e);
                throw new IOException("Class is not found");
            }
        } finally {
            lock.readLock().unlock();
        }

    }

    @Override
    public Date store(ContextWrapper ctx, T data) throws IOException {
        try {
            lock.writeLock().lock();
            ObjectOutputStream os = new ObjectOutputStream(
                    new BufferedOutputStream(
                            ctx.openFileOutput(
                                    fileName,
                                    Context.MODE_PRIVATE
                            )
                    )
            );

            try {
                writeToStream(os, data);
            } finally {
                os.close();
            }

            os = new ObjectOutputStream(
                    new BufferedOutputStream(
                            ctx.openFileOutput(
                                    fileName + DATE_SUFFIX,
                                    Context.MODE_PRIVATE
                            )
                    )
            );

            Date storeDate;
            try {
                storeDate = new Date();
                os.writeObject(storeDate);
            } finally {
                os.close();
            }

            return storeDate;
        } finally {
            lock.writeLock().unlock();
        }

    }

    @Override
    public Date getLastUpdateDate(ContextWrapper ctx) throws IOException {
        try {
            lock.readLock().lock();
            try {
                ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(ctx.openFileInput(fileName + DATE_SUFFIX)));

                try {
                    return (Date) is.readObject();
                } finally {
                    is.close();
                }
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "Can't load tweets from cache", e);
                throw new IOException("Class is not found");
            } catch (FileNotFoundException e) {
                // No info: return null
                return null;
            }
        } finally {
            lock.readLock().unlock();
        }

    }

    protected abstract void writeToStream(ObjectOutputStream os, T data) throws IOException;

    protected abstract T readStream(ContextWrapper ctx, ObjectInputStream is) throws ClassNotFoundException, IOException;
}
