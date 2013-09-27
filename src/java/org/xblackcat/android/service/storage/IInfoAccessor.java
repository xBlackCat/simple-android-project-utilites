package org.xblackcat.android.service.storage;

import android.content.ContextWrapper;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

/**
* 28.08.12 16:46
*
* @author xBlackCat
*/
public interface IInfoAccessor<T extends Serializable> {
    T load(ContextWrapper ctx) throws IOException;

    Date store(ContextWrapper ctx, T data) throws IOException;

    Date getLastUpdateDate(ContextWrapper ctx) throws IOException;
}
