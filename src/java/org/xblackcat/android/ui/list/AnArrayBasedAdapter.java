package org.xblackcat.android.ui.list;

import android.view.View;

/**
 * 23.07.12 16:52
 *
 * @author xBlackCat
 */
public abstract class AnArrayBasedAdapter<T, V extends View & IItemSettable<T>> extends ABaseAdapter<T,V> {
    protected final T[] items;

    @SuppressWarnings("unchecked")
    protected AnArrayBasedAdapter(T... items) {
        this(items, null, null);
    }

    @SuppressWarnings("unchecked")
    protected AnArrayBasedAdapter(int rowBackground, T... items) {
        this(items, rowBackground, null);
    }

    @SuppressWarnings("unchecked")
    protected AnArrayBasedAdapter(int rowBackground, int evenRowBackground, T... items) {
        this(items, rowBackground, evenRowBackground);
    }

    private AnArrayBasedAdapter(T[] items, Integer cell_bg_light, Integer cell_bg_dark) {
        super(cell_bg_dark, cell_bg_light);
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public T getItem(int i) {
        return items[i];
    }

    public T[] getItems() {
        return items;
    }

}
