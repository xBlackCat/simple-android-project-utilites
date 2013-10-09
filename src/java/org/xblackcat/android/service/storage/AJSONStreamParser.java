package org.xblackcat.android.service.storage;

import com.google.android.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * 08.10.13 12:12
 *
 * @author xBlackCat
 */
public abstract class AJSONStreamParser<T> implements IStreamParser<T> {
    @Override
    public InputStream filter(InputStream is) throws IOException {
        return new JsonFilterInputStream(is);
    }

    @Override
    public T parse(Reader reader) throws IOException {
        JsonReader jsonReader = new JsonReader(reader);
        return parse(jsonReader);
    }

    protected abstract T parse(JsonReader reader) throws IOException;
}
