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
        Log.d(TAG, "Load data from url " + sourceUrl);

        O result;
        try {
            InputStream is = IOUtils.getInputStream(sourceUrl, checkGZippedUrl);
            try {
                InputStream filter = parser.filter(is);
                Reader streamReader = new InputStreamReader(filter, Charset.forName("utf8"));

                result = parser.parse(streamReader);
            } finally {
                is.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Can't load data", e);
            result = null;
        }

        try {
            return postProcess(result);
        } catch (Exception e) {
            Log.e(TAG, "Can't process data", e);
            return null;
        }
    }

    protected abstract T postProcess(O o);
}
