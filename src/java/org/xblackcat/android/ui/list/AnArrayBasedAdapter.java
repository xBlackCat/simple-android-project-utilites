package org.xblackcat.android.ui.list;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * 23.07.12 16:52
 *
 * @author xBlackCat
 */
public abstract class AnArrayBasedAdapter<T, V extends View & IItemSettable<T>> extends BaseAdapter {
    protected final T[] items;
    private final Integer rowBackground;
    private final Integer oddRowBackground;

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
        this.items = items;
        rowBackground = cell_bg_light;
        oddRowBackground = cell_bg_dark;
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public T getItem(int i) {
        return items[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public T[] getItems() {
        return items;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        V view = null;
        if (convertView != null && convertView instanceof IItemSettable) {
            try {
                view = (V) convertView;
            } catch (ClassCastException e) {
                view = null;
            }
        }

        if (view == null) {
            view = buildView(position, parent);
        }

        if (rowBackground != null) {
            if (oddRowBackground == null) {
                view.setBackgroundResource(rowBackground);
            } else {
                view.setBackgroundResource((position % 2 == 0) ? rowBackground : oddRowBackground);
            }
        }

        view.setItem(position, getItem(position));
        view.setEnabled(isEnabled(position));
        return view;
    }

    protected abstract V buildView(int position, ViewGroup parent);
}
