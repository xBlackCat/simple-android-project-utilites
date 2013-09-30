package org.xblackcat.android.ui.swipetabactivity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
    private final List<TabInfo> allTabs = new ArrayList<>();
    private final List<TabInfo> enabledTabs = new LinkedList<>();

    public TabsAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    public void addTab(String tag, View view, Runnable onShow, boolean enable) {
        TabInfo info = new TabInfo(tag, view, onShow);
        addTabInfo(enable, info);
    }

    public void addTab(String tag, TabHost.TabContentFactory factory, Runnable onShow, boolean enable) {
        TabInfo info = new TabInfo(tag, factory, onShow);
        addTabInfo(enable, info);
    }

    private void addTabInfo(boolean enable, TabInfo info) {
        allTabs.add(info);
        if (enable) {
            enabledTabs.add(info);

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

    public void removeTabByTag(String tag) {
        int i = 0;
        while (i < allTabs.size()) {
            if (tag.equals(allTabs.get(i).tag)) {
                enabledTabs.remove(allTabs.remove(i));
            }
            i++;
        }
    }

    public void removeTabByPosition(int position) {
        enabledTabs.remove(allTabs.remove(position));
    }

    public void removeTabByVisiblePosition(int position) {
        allTabs.remove(enabledTabs.remove(position));
    }

    public String tagForPosition(int position) {
        return allTabs.get(position).tag;
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
        while (i < allTabs.size()) {
            if (tag.equals(allTabs.get(i).tag)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public void setEnabled(int position, boolean enable) {
        TabInfo tab = allTabs.get(position);

        if (enable) {
            int ie = 0;
            int i = 0;
            while (i < position) {
                if (allTabs.get(i).equals(enabledTabs.get(ie))) {
                    ie++;
                }

                i++;
            }

            enabledTabs.add(ie, allTabs.get(position));
        } else {
            Iterator<TabInfo> iterator = enabledTabs.iterator();
            while (iterator.hasNext()) {
                TabInfo info = iterator.next();
                if (info.equals(tab)) {
                    iterator.remove();
                    break;
                }
            }
        }

        notifyDataSetChanged();
    }

    public void setEnabled(String tag, boolean enable) {
        if (enable) {
            int ie = 0;
            int i = 0;
            TabInfo tab = allTabs.get(i);
            while (i < allTabs.size()) {
                if (tab.tag.equals(tag)) {
                    break;
                }

                if (tab.equals(enabledTabs.get(ie))) {
                    ie++;
                }

                i++;
            }

            enabledTabs.add(ie, tab);
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
        allTabs.clear();
        enabledTabs.clear();
        notifyDataSetChanged();
    }

    public void fireOnShow(String tag) {
        for (TabInfo tabInfo : allTabs) {
            if (tag.equals(tabInfo.tag)) {
                if (tabInfo.onShow != null) {
                    tabInfo.onShow.run();
                }
                break;
            }
        }
    }

    public void fireOnShow(int position) {
        TabInfo tabInfo = allTabs.get(position);

        if (tabInfo.onShow != null) {
            tabInfo.onShow.run();
        }
    }

    /**
     * Returns an index of a tab specified by visible position;
     *
     * @param position visible position of a tag
     * @return tab index in model.
     */
    public int visibleToAbsPosition(int position) {
        return allTabs.indexOf(enabledTabs.get(position));
    }

    /**
     * Returns a visible position of a tab specified by its index. It tab is not enabled (not visible) then <code>-1</code> is returned.
     *
     * @param position index of tab in model
     * @return visible position or <code>-1</code> if tab is disabled.
     */
    public int visiblePosition(int position) {
        return enabledTabs.indexOf(allTabs.get(position));
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
