package org.xblackcat.android.service.storage;

import android.content.ContextWrapper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * 15.05.13 16:12
 *
 * @author xBlackCat
 */
public class SimpleInfoAccessor<O extends Serializable> extends AnInfoAccessor<O> {
    public SimpleInfoAccessor(String fileName) {
        super(fileName);
    }

    @Override
    protected void writeToStream(ObjectOutputStream os, O data) throws IOException {
        os.writeObject(data);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected O readStream(ContextWrapper ctx, ObjectInputStream is) throws ClassNotFoundException, IOException {
        return (O) is.readObject();
    }
}
