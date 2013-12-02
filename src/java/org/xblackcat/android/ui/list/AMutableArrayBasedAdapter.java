package org.xblackcat.android.ui.list;

import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 23.07.12 16:52
 *
 * @author xBlackCat
 */
public abstract class AMutableArrayBasedAdapter<T, V extends View & IItemSettable<T>> extends ABaseAdapter<T, V> {
    protected final List<T> items;

    @SuppressWarnings("unchecked")
    public AMutableArrayBasedAdapter(T... items) {
        super(null, null);
        this.items = new ArrayList<>(Arrays.asList(items));
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public T getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public List<T> getItems() {
        return Collections.unmodifiableList(items);
    }
}
