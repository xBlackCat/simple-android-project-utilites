package org.xblackcat.android.ui.list;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 23.07.12 16:52
 *
 * @author xBlackCat
 */
public abstract class AMutableArrayBasedAdapter<T, V extends View & IItemSettable<T>> extends BaseAdapter {
    protected final List<T> items;

    @SuppressWarnings("unchecked")
    public AMutableArrayBasedAdapter(T... items) {
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

    @SuppressWarnings("unchecked")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        V view = null;
        if (convertView instanceof IItemSettable) {
            try {
                view = (V) convertView;
            } catch (ClassCastException e) {
                view = null;
            }
        }

        if (view == null) {
            view = buildView(position, parent);

            assert view != null;
        }

        view.setItem(position, getItem(position));
        view.setEnabled(isEnabled(position));
        return view;
    }

    protected abstract V buildView(int position, ViewGroup parent);
}
