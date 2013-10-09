package org.xblackcat.android.service.storage;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * 08.10.13 12:06
 *
 * @author xBlackCat
 */
public interface IStreamParser<T> {
    /**
     * Add a stream for filtering data before pass it into reader
     */
    InputStream filter(InputStream is) throws IOException;

    T parse(Reader reader) throws IOException;
}
