package org.xblackcat.android.service.storage;

import android.util.Log;
import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * 08.10.13 14:43
 *
 * @author xBlackCat
 */
public abstract class XMLStreamParser<T> implements IStreamParser<T> {
    @Override
    public InputStream filter(InputStream is) throws IOException {
        return new DeBOMFilterInputStream(is);
    }

    @Override
    public T parse(Reader reader) throws IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();

            parser.setInput(reader);

            return parse(parser);
        } catch (Exception e) {
            Log.e("XMLReader", "Can't read XML stream.", e);
            throw new IOException("Can't read XML stream");
        }
    }

    protected abstract T parse(XmlPullParser parser) throws XmlPullParserException, IOException;
}
