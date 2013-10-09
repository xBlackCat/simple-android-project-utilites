package org.xblackcat.android.service.storage;

/**
 * 08.10.13 12:18
 *
 * @author xBlackCat
 */
public class SimpleDataLoader<T> extends ADataLoader<T, T> {
    public SimpleDataLoader(String sourceUrl, IStreamParser<T> parser) {
        this(sourceUrl, parser, true);
    }

    public SimpleDataLoader(String sourceUrl, IStreamParser<T> parser, boolean checkGZippedUrl) {
        super(sourceUrl, parser, checkGZippedUrl);
    }

    @Override
    protected T postProcess(T o) {
        return o;
    }
}
