package org.xblackcat.android.ui.pulltorefresh;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.xblackcat.android.R;

public class PullToRefreshList extends APullToRefreshComponent<ListView> {
    protected PullToRefreshAdapter adapter;
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

    public PullToRefreshList(Context context) {
        this(context, null);
    }

    public PullToRefreshList(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs);
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    public void setAdapter(BaseAdapter adapter) {
        this.adapter = new PullToRefreshAdapter(adapter);
        if (adapter == null) {
            view.setAdapter(null);
        } else {
            view.setAdapter(this.adapter);
        }

        resetHeader();
    }

    public void setSelection(int position) {
        if (state == REFRESHING) {
            position++;
        }

        view.setSelection(position);
    }

    public void setSelectionFromTop(int position, int y) {
        if (state == REFRESHING) {
            position++;
        }

        view.setSelectionFromTop(position, y);
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

    protected ListView buildComponent(final Context context, final AttributeSet attrs) {
        return new ListView(context, attrs) {
            @Override
            public BaseAdapter getAdapter() {
                if (adapter != null) {
                    return adapter.getInnerAdapter();
                } else {
                    return null;
                }
            }

        };
    }

    public void onDestroy() {
        super.onDestroy();
        onItemClickListener = null;

        if (adapter != null) {
            adapter.onDestroy();
            adapter = null;
        }
    }

    protected void setRefreshingImpl() {
        if (adapter != null) {
            adapter.setRefreshed(true);
        }
    }

    protected void resetHeaderImpl() {
        if (adapter != null) {
            adapter.setRefreshed(false);
        }
    }

    protected boolean isAbleToPushToRefresh() {
        return isFirstVisible() && adapter != null
                && adapter.isAbleToPullToRefresh();
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

    protected class PullToRefreshAdapter extends BaseAdapter {

        // ===========================================================
        // Constants
        // ===========================================================

        // ===========================================================
        // Fields
        // ===========================================================

        private BaseAdapter adapter;
        private int offset = 0;
        private AdapterSetObserver adapterSetObserver;

        // ===========================================================
        // Constructors
        // ===========================================================

        public PullToRefreshAdapter(BaseAdapter adapter) {
            onDestroy();
            if (adapter == null) {
                return;
            }
            adapterSetObserver = new AdapterSetObserver();
            adapter.registerDataSetObserver(adapterSetObserver);
            this.adapter = adapter;
        }

        // ===========================================================
        // Getter & Setter
        // ===========================================================

        public boolean isAbleToPullToRefresh() {
            if (adapter.getCount() == 0) {
                return false;
            } else {
                return true;
            }
        }

        public void setRefreshed(boolean value) {
            if (value) {
                offset = 1;
            } else {
                offset = 0;
            }
            notifyDataSetChanged();
        }

        public BaseAdapter getInnerAdapter() {
            return adapter;
        }

        public void onDestroy() {
            if (adapter != null) {
                adapter.unregisterDataSetObserver(adapterSetObserver);
                adapter = null;
            }
        }

        // ===========================================================
        // Methods for/from SuperClass/Interfaces
        // ===========================================================

        @Override
        public boolean isEnabled(int position) {
            if ((position == 0 && offset == 1) || adapter == null) {
                return false;
            } else {
                return adapter.isEnabled(position - offset);
            }
        }

        @Override
        public int getCount() {
            if (adapter != null) {
                return adapter.getCount() + offset;
            } else {
                return 0;
            }
        }

        @Override
        public int getViewTypeCount() {
            if (adapter != null) {
                return adapter.getViewTypeCount() + 1;
            } else {
                return 1;
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (adapter == null) {
                return 0;
            }
            if (position == 0 && offset == 1) {
                return adapter.getViewTypeCount();
            }
            return adapter.getItemViewType(position - offset);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if ((position == 0 && offset == 1) || adapter == null) {
                if (convertView == null) {
                    Context context = parent.getContext();
                    assert context != null;
                    convertView = LayoutInflater.from(context)
                            .inflate(R.layout.pull_to_refresh_header, null);
                    assert convertView != null;
                    convertView.findViewById(R.id.pull_to_refresh_image)
                            .setVisibility(GONE);
                    convertView.findViewById(R.id.pull_to_refresh_progress)
                            .setVisibility(VISIBLE);
                    TextView tv = (TextView) convertView
                            .findViewById(R.id.pull_to_refresh_text);
                    tv.setText(loadingText);
                }
                return convertView;
            }

            return adapter.getView(position - offset, convertView, parent);
        }

        @Override
        public Object getItem(int position) {
            if ((position == 0 && offset == 1) || adapter == null) {
                return null;
            } else {
                return adapter.getItem(position - offset);
            }
        }

        @Override
        public long getItemId(int position) {
            if ((position == 0 && offset == 1) || adapter == null) {
                return 0;
            } else {
                return adapter.getItemId(position - offset);
            }
        }

        // ===========================================================
        // Methods
        // ===========================================================

        // ===========================================================
        // Inner and Anonymous Classes
        // ===========================================================

        private class AdapterSetObserver extends DataSetObserver {

            @Override
            public void onChanged() {
                PullToRefreshAdapter.this.notifyDataSetChanged();
            }

            @Override
            public void onInvalidated() {
                PullToRefreshAdapter.this.notifyDataSetInvalidated();
            }
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