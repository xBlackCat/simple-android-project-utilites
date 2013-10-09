package org.xblackcat.android.service.storage;

import android.content.ContextWrapper;
import android.util.Log;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

/**
 * 08.10.13 11:32
 *
 * @author xBlackCat
 */
public class StorableDataLoader<T extends Serializable> extends ADataLoader<StorableDataLoader.DataPack<T>, T> {
    protected final IInfoAccessor<T> infoAccessor;
    protected final ContextWrapper ctx;

    public StorableDataLoader(String sourceUrl, IStreamParser<T> parser, ContextWrapper ctx, IInfoAccessor<T> infoAccessor) {
        this(sourceUrl, parser, ctx, infoAccessor, true);
    }

    public StorableDataLoader(
            String sourceUrl,
            IStreamParser<T> parser,
            ContextWrapper ctx,
            IInfoAccessor<T> infoAccessor,
            boolean checkGZippedUrl
    ) {
        super(sourceUrl, parser, checkGZippedUrl);
        this.ctx = ctx;
        this.infoAccessor = infoAccessor;
    }

    @Override
    protected DataPack<T> postProcess(T data) {
        Date storeDate;
        try {
            Log.d(TAG, "Saving data to internal storage");
            storeDate = infoAccessor.store(ctx, data);
        } catch (IOException e) {
            Log.e(TAG, "Can't store data", e);
            storeDate = new Date();
        }

        return new DataPack<>(data, storeDate);
    }

    /**
     * 08.10.13 11:13
     *
     * @author xBlackCat
     */
    public static class DataPack<T extends Serializable> {
        public final T data;
        public final Date date;

        public DataPack(T data, Date date) {
            this.data = data;
            this.date = date;
        }
    }
}
