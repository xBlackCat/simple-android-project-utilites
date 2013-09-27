package org.xblackcat.android.ui.pulltorefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.*;

public class PullToRefreshGridView extends APullToRefreshComponent<GridView> {
    private AdapterView.OnItemClickListener onItemClickListener;
    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================

    // ===========================================================
    // Constructors
    // ===========================================================

    public PullToRefreshGridView(Context context) {
        this(context, null);
    }

    public PullToRefreshGridView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs);
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    public void setAdapter(BaseAdapter adapter) {
        view.setAdapter(adapter);

        resetHeader();
    }

    public void setSelection(int position) {
        if (state == REFRESHING) {
            position++;
        }

        view.setSelection(position);
    }

    public View getListViewChildAt(int index) {
        if (state == REFRESHING) {
            index++;
        }

        return view.getChildAt(index);
    }

    public ListAdapter getAdapter() {
        return view.getAdapter();
    }

    public void setOnScrollListener(AbsListView.OnScrollListener listener) {
        view.setOnScrollListener(listener);
    }

    protected GridView buildComponent(final Context context, final AttributeSet attrs) {
        return new GridView(context, attrs);
    }

    public void onDestroy() {
        super.onDestroy();
        onItemClickListener = null;
    }

    protected void setRefreshingImpl() {
    }

    protected void resetHeaderImpl() {
    }

    protected boolean isAbleToPushToRefresh() {
        return true;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        onItemClickListener = listener;

        view.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        if (onItemClickListener == null) {
                            return;
                        }
                        int offset = 0;
                        if (state == REFRESHING) {
                            offset = 1;
                        }
                        onItemClickListener.onItemClick(arg0, arg1, arg2 - offset, arg3);
                    }
                }
        );
    }

    public int getFirstVisiblePosition() {
        return view.getFirstVisiblePosition();
    }

    public int getLastVisiblePosition() {
        if (state == REFRESHING) {
            return view.getLastVisiblePosition() - 1;
        } else {
            return view.getLastVisiblePosition();
        }
    }

    public void setEmptyView(View emptyView) {
        view.setEmptyView(emptyView);
    }

    private boolean isFirstVisible() {
        if (this.view.getCount() == 0) {
            return true;
        } else if (view.getFirstVisiblePosition() == 0) {
            return view.getChildAt(0).getTop() >= view.getTop();
        } else {
            return false;
        }
    }

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================


    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

}