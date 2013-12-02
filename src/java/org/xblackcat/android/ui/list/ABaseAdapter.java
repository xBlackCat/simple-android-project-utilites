package org.xblackcat.android.ui.list;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * 06.11.13 11:12
 *
 * @author xBlackCat
 */
public abstract class ABaseAdapter<T, V extends View & IItemSettable<T>> extends BaseAdapter {
    protected final Integer rowBackground;
    protected final Integer oddRowBackground;

    protected ABaseAdapter(
            Integer cell_bg_dark,
            Integer cell_bg_light
    ) {
        oddRowBackground = cell_bg_dark;
        rowBackground = cell_bg_light;
    }

    @Override
    public abstract T getItem(int i);

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressWarnings("unchecked")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
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
