package org.xblackcat.android.ui.swipetabactivity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;

import java.util.*;

/**
 * This is a helper class that implements the management of tabs and all
 * details of connecting a ViewPager with associated TabHost.  It relies on a
 * trick.  Normally a tab host has a simple API for supplying a View or
 * Intent that each tab will show.  This is not sufficient for switching
 * between pages.  So instead we make the content part of the tab host
 * 0dp high (it is not shown) and the TabsAdapter supplies its own dummy
 * view to show as the tab content.  It listens to changes in tabs, and takes
 * care of switch to the correct paged in the ViewPager whenever the selected
 * tab changes.
 */
class TabsAdapter extends FragmentPagerAdapter {
    private final List<TabInfo> mTabs = new ArrayList<>();
    private final List<TabInfo> enabledTabs = new LinkedList<>();
    private final Set<String> enabledTabTags = new HashSet<>();

    public TabsAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    public void addTab(String tag, View view, Runnable onShow, boolean enable) {
        TabInfo info = new TabInfo(tag, view, onShow);
        addTabInfo(tag, enable, info);
    }

    public void addTab(String tag, TabHost.TabContentFactory factory, Runnable onShow, boolean enable) {
        TabInfo info = new TabInfo(tag, factory, onShow);
        addTabInfo(tag, enable, info);
    }

    private void addTabInfo(String tag, boolean enable, TabInfo info) {
        mTabs.add(info);
        if (enable) {
            enabledTabs.add(info);
            enabledTabTags.add(tag);

            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return enabledTabs.size();
    }

    @Override
    public Fragment getItem(int position) {
        final TabInfo info = enabledTabs.get(position);
        return info.fragment;
    }

    public String tagForPosition(int position) {
        return mTabs.get(position).tag;
    }

    public int visiblePositionForTag(String tabId) {
        int i = 0;
        while (i < enabledTabs.size()) {
            if (tabId.equals(enabledTabs.get(i).tag)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public int positionForTag(String tag) {
        int i = 0;
        while (i < mTabs.size()) {
            if (tag.equals(mTabs.get(i).tag)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public void setEnabled(int position, boolean enable) {
        String tag = mTabs.get(position).tag;
        if (enabledTabTags.contains(tag) == enable) {
            return;
        }

        if (enable) {
            int ie = 0;
            int i = 0;
            while (i < position) {
                if (mTabs.get(i).tag.equals(enabledTabs.get(ie).tag)) {
                    ie++;
                }

                i++;
            }

            enabledTabs.add(ie, mTabs.get(position));
        } else {
            Iterator<TabInfo> iterator = enabledTabs.iterator();
            while (iterator.hasNext()) {
                TabInfo info = iterator.next();
                if (info.tag.equals(tag)) {
                    iterator.remove();
                    break;
                }
            }
        }

        notifyDataSetChanged();
    }

    public void setEnabled(String tag, boolean enable) {
        if (enabledTabTags.contains(tag) == enable) {
            return;
        }

        if (enable) {
            int ie = 0;
            int i = 0;
            while (i < mTabs.size()) {
                String curTag = mTabs.get(i).tag;
                if (curTag.equals(tag)) {
                    break;
                }

                if (curTag.equals(enabledTabs.get(ie).tag)) {
                    ie++;
                }

                i++;
            }

            enabledTabs.add(ie, mTabs.get(i));
        } else {
            Iterator<TabInfo> iterator = enabledTabs.iterator();
            while (iterator.hasNext()) {
                TabInfo info = iterator.next();
                if (info.tag.equals(tag)) {
                    iterator.remove();
                    break;
                }
            }
        }

        notifyDataSetChanged();
    }

    public void clearAllTabs() {
        mTabs.clear();
        notifyDataSetChanged();
    }

    public void fireOnShow(String tag) {
        for (TabInfo tabInfo : mTabs) {
            if (tag.equals(tabInfo.tag)) {
                if (tabInfo.onShow != null) {
                    tabInfo.onShow.run();
                }
                break;
            }
        }
    }

    public void fireOnShow(int position) {
        TabInfo tabInfo = mTabs.get(position);

        if (tabInfo.onShow != null) {
            tabInfo.onShow.run();
        }
    }

    public int visibleToAbsPosition(int position) {
        return positionForTag(enabledTabs.get(position).tag);
    }

    static final class TabInfo {
        private final String tag;
        private final Runnable onShow;
        private final Fragment fragment;

        TabInfo(String tag, final View view, Runnable onShow) {
            this.tag = tag;
            this.onShow = onShow;
            this.fragment = new Fragment() {
                @Override
                public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
                    return view;
                }
            };
        }

        TabInfo(final String tag, final TabHost.TabContentFactory view, Runnable onShow) {
            this.tag = tag;
            this.onShow = onShow;
            this.fragment = new Fragment() {
                @Override
                public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
                    return view.createTabContent(tag);
                }
            };
        }
    }
}
