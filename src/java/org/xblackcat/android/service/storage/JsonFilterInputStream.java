package org.xblackcat.android.service.storage;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Skips all extra symbols before first of { or [ appearance.
 *
* 29.09.13 16:52
*
* @author xBlackCat
*/
public class JsonFilterInputStream extends FilterInputStream {
    private boolean jsonStarted = false;

    public JsonFilterInputStream(InputStream is) {
        super(is);
    }

    @Override
    public int read() throws IOException {
        if (jsonStarted) {
            return super.read();
        } else {
            return skipUntilJSONStarted();
        }
    }

    private int skipUntilJSONStarted() throws IOException {
        int b;
        do {
            b = super.read();
        } while (b != '{' && b != '[' && b != -1);

        jsonStarted = true;
        return b;
    }

    @Override
    public int read(byte[] buffer) throws IOException {
        if (jsonStarted) {
            return super.read(buffer);
        } else {
            return read(buffer, 0, buffer.length);
        }
    }

    @Override
    public int read(byte[] buffer, int offset, int count) throws IOException {
        if (jsonStarted) {
            return super.read(buffer, offset, count);
        } else {
            int c = skipUntilJSONStarted();

            if (c == -1) {
                return -1;
            }

            buffer[offset] = (byte)c;

            int i = 1;
            try {
                for (; i < count ; i++) {
                    c = read();
                    if (c == -1) {
                        break;
                    }
                    buffer[offset + i] = (byte)c;
                }
            } catch (IOException ee) {
            }

            return i;
        }
    }
}
