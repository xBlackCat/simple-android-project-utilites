package org.xblackcat.android.service.storage;

import android.os.AsyncTask;
import android.util.Log;
import org.xblackcat.android.util.IOUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

/**
 * 08.10.13 11:21
 *
 * @author xBlackCat
 */
public abstract class ADataLoader<T, O> extends AsyncTask<Void, Void, T> {
    protected final String TAG = getClass().getName();
    protected final String sourceUrl;
    protected final IStreamParser<O> parser;
    private final boolean checkGZippedUrl;

    public ADataLoader(String sourceUrl, IStreamParser<O> parser) {
        this(sourceUrl, parser, true);
    }

    public ADataLoader(String sourceUrl, IStreamParser<O> parser, boolean checkGZippedUrl) {
        if (parser == null) {
            throw new NullPointerException("Stream parser is null");
        }

        this.sourceUrl = sourceUrl;
        this.parser = parser;
        this.checkGZippedUrl = checkGZippedUrl;
    }

    @Override
    protected T doInBackground(Void... voids) {
        long startOverall = System.currentTimeMillis();
        Log.d(TAG, "Load data from url " + sourceUrl);

        T data;
        try {
            InputStream is = IOUtils.getInputStream(sourceUrl, checkGZippedUrl);
            try {
                InputStream filter = parser.filter(is);
                Reader streamReader = new InputStreamReader(filter, Charset.forName("utf8"));

                long start;
                start = System.currentTimeMillis();
                O result = parser.parse(streamReader);
                Log.e("Performance", "[" + sourceUrl + "] Document parsed in " + (System.currentTimeMillis() - start) + " ms");
                start = System.currentTimeMillis();

                data = postProcess(result);
                Log.e("Performance", "[" + sourceUrl + "] Document postprocessed in " + (System.currentTimeMillis() - start) + " ms");
            } finally {
                is.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Can't parse data", e);
            return null;
        }

        Log.e("Performance", "[" + sourceUrl + "] Total: " + (System.currentTimeMillis() - startOverall) + " ms");
        return data;
    }

    protected abstract T postProcess(O o);
}
