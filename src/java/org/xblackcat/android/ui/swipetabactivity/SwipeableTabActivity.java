package org.xblackcat.android.ui.swipetabactivity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabWidget;
import org.xblackcat.android.R;

import java.util.HashMap;
import java.util.Map;

/**
 * 27.09.13 15:23
 *
 * @author xBlackCat
 */
public class SwipeableTabActivity extends FragmentActivity {
    protected final Map<String, TabHost.OnTabChangeListener> tabListeners = new HashMap<>();
    protected EnablableViewPager mViewPager;
    protected TabHost mTabHost;
    protected TabsAdapter mTabsAdapter;
    private TabHost.OnTabChangeListener globalTabChangeListener;

    @Override
    public void onContentChanged() {
        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup();

        mViewPager = (EnablableViewPager) findViewById(R.id.realtabcontent);
        mViewPager.setOffscreenPageLimit(3);

        mTabsAdapter = new TabsAdapter(getSupportFragmentManager());

        mViewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        // Unfortunately when TabHost changes the current tab, it kindly
                        // also takes care of putting focus on it when not in touch mode.
                        // The jerk.
                        // This hack tries to prevent this from pulling focus out of our
                        // ViewPager.
                        TabWidget widget = mTabHost.getTabWidget();
                        assert widget != null;
                        int oldFocusability = widget.getDescendantFocusability();
                        widget.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
                        mTabHost.setCurrentTab(mTabsAdapter.visibleToAbsPosition(position));
                        widget.setDescendantFocusability(oldFocusability);
                    }
                }
        );
        mViewPager.setAdapter(mTabsAdapter);

        mTabHost.setOnTabChangedListener(
                new TabHost.OnTabChangeListener() {
                    @Override
                    public void onTabChanged(String tabId) {
                        int position = mTabsAdapter.visiblePositionForTag(tabId);
                        mViewPager.setCurrentItem(position);
                        mTabsAdapter.fireOnShow(tabId);

                        if (globalTabChangeListener != null) {
                            globalTabChangeListener.onTabChanged(tabId);
                        }

                        TabHost.OnTabChangeListener l = tabListeners.get(tabId);
                        if (l != null) {
                            l.onTabChanged(tabId);
                        }
                    }
                }
        );
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        int cur = state.getInt("currentTab", -1);
        if (cur != -1) {
            mTabHost.setCurrentTab(cur);
        }
    }

    @Override
    protected void onPostCreate(Bundle icicle) {
        super.onPostCreate(icicle);

        if (mTabHost.getCurrentTab() == -1) {
            mTabHost.setCurrentTab(0);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int currentTabTag = mTabHost.getCurrentTab();
        if (currentTabTag != -1) {
            outState.putInt("currentTab", currentTabTag);
        }
    }

    public void setOnTabChangedListener(String tag, TabHost.OnTabChangeListener listener) {
        if (listener != null) {
            tabListeners.put(tag, listener);
        } else {
            tabListeners.remove(tag);
        }
    }

    public void setOnTabChangedListener(TabHost.OnTabChangeListener globalListener) {
        this.globalTabChangeListener = globalListener;
    }

    public void setSwipeEnabled(boolean swipeEnabled) {
        this.mViewPager.setSwipeEnabled(swipeEnabled);
    }

    public void setCurrentTabByTag(String tag) {
        mTabHost.setCurrentTabByTag(tag);
    }

    public void setTabEnabled(String tag, boolean enabled) {
        TabWidget widget = mTabHost.getTabWidget();
        assert widget != null;
        View childTabViewAt = widget.getChildTabViewAt(mTabsAdapter.positionForTag(tag));
        assert childTabViewAt != null;
        childTabViewAt.setEnabled(enabled);
        mTabsAdapter.setEnabled(tag, enabled);
    }

    public void setTabEnabled(int pos, boolean enabled) {
        TabWidget widget = mTabHost.getTabWidget();
        assert widget != null;
        View childTabViewAt = widget.getChildTabViewAt(pos);
        assert childTabViewAt != null;
        mTabsAdapter.setEnabled(pos, enabled);
    }

    protected void setCurrentTab(int currentTab) {
        mTabHost.setCurrentTab(currentTab);
        int vp = mTabsAdapter.visiblePosition(currentTab);
        if (vp != -1) {
            mViewPager.setCurrentItem(vp, false);
        }
    }

    protected int clearAllTabs() {
        int currentTab = getCurrentTab();
        mTabHost.setCurrentTab(0);
        mTabHost.clearAllTabs();
        mTabsAdapter.clearAllTabs();
        return currentTab;
    }

    public int getCurrentTab() {
        return mTabHost.getCurrentTab();
    }

    public void addTab(String tag, View label, TabHost.TabContentFactory view, Runnable onShow) {
        mTabsAdapter.addTab(tag, view, onShow, true);

        fillModel(tag, label);
    }

    public void addTab(String tag, View label, View view, Runnable onShow) {
        mTabsAdapter.addTab(tag, view, onShow, true);

        fillModel(tag, label);
    }

    private void fillModel(String tag, View label) {
        TabHost.TabSpec tab = mTabHost.newTabSpec(tag);
        tab.setIndicator(label);
        tab.setContent(new DummyTabFactory(this));
        mTabHost.addTab(tab);
    }

    private static class DummyTabFactory implements TabHost.TabContentFactory {
        private final Context mContext;

        public DummyTabFactory(Context context) {
            mContext = context;
        }

        @Override
        public View createTabContent(String tag) {
            View v = new View(mContext);
            v.setMinimumWidth(0);
            v.setMinimumHeight(0);
            return v;
        }
    }
}
