package org.xblackcat.android.service.storage;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Removes BOM from begin of the stream
 * <p/>
 * 09.10.13 10:15
 *
 * @author xBlackCat
 */
public class DeBOMFilterInputStream extends FilterInputStream {
    private final static char[] BOM = {0xEF, 0xBB, 0xBF};

    private int idx;


    public DeBOMFilterInputStream(InputStream is) {
        super(is);
    }

    @Override
    public int read() throws IOException {
        if (idx > 2) {
            return super.read();
        } else {
            return skipBOM();
        }
    }

    private int skipBOM() throws IOException {
        int b;
        do {
            b = super.read();
        } while (idx < BOM.length && b == BOM[idx++]);

        return b;
    }

    @Override
    public int read(byte[] buffer) throws IOException {
        if (idx > 2) {
            return super.read(buffer);
        } else {
            return read(buffer, 0, buffer.length);
        }
    }

    @Override
    public int read(byte[] buffer, int offset, int count) throws IOException {
        if (idx > 2) {
            return super.read(buffer, offset, count);
        } else {
            int c = skipBOM();

            if (c == -1) {
                return -1;
            }

            buffer[offset] = (byte) c;

            int i = 1;
            try {
                for (; i < count; i++) {
                    c = read();
                    if (c == -1) {
                        break;
                    }
                    buffer[offset + i] = (byte) c;
                }
            } catch (IOException ee) {
            }

            return i;
        }
    }

}
