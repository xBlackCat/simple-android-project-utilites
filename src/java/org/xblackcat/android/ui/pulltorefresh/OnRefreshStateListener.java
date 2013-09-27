package org.xblackcat.android.ui.pulltorefresh;

import android.view.View;

/**
 * 31.10.12 15:11
 *
 * @author xBlackCat
 */
public interface OnRefreshStateListener {
    void onRefreshStarted(APullToRefreshComponent<? extends View> comp);

    void onRefreshComplete(APullToRefreshComponent<? extends View> comp);
}
