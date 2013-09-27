package org.xblackcat.android.ui.list;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.LinearLayout;
import org.xblackcat.android.util.UIUtils;

/**
 * 31.07.12 17:47
 *
 * @author xBlackCat
 */
public class WaitingAdapter<T extends ViewGroup.LayoutParams> extends BaseAdapter {
    public static WaitingAdapter<AbsListView.LayoutParams> forList() {
        return new WaitingAdapter<>(
                new AbsListView.LayoutParams(
                        ViewGroup.LayoutParams.FILL_PARENT,
                        ViewGroup.LayoutParams.FILL_PARENT
                )
        );
    }

    public static WaitingAdapter<Gallery.LayoutParams> forGallery() {
        return new WaitingAdapter<>(
                new Gallery.LayoutParams(
                        ViewGroup.LayoutParams.FILL_PARENT,
                        ViewGroup.LayoutParams.FILL_PARENT
                )
        );
    }

    public static WaitingAdapter<ViewGroup.LayoutParams> forGroup() {
        return new WaitingAdapter<>(
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.FILL_PARENT,
                        ViewGroup.LayoutParams.FILL_PARENT
                )
        );
    }


    private final T layoutParams;

    private WaitingAdapter(T layoutParams) {
        this.layoutParams = layoutParams;
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public Object getItem(int position) {
        return Void.class;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout linearLayout = UIUtils.buildWaitingComponent(parent.getContext());
        linearLayout.setLayoutParams(layoutParams);
        return linearLayout;
    }
}
