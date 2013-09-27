package org.xblackcat.android.ui.list;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

/**
 * 26.09.13 18:17
 *
 * @author xBlackCat
 */
@SuppressWarnings("unchecked")
public abstract class AnArrayBasedExpandableListAdapter<G, C, GI extends IItemGroup<G, C>, GV extends View & IItemSettable<G>, CV extends View & IItemSettable<C>>
        extends BaseExpandableListAdapter {
    private final Integer rowBackground;
    private final Integer oddRowBackground;
    private final GI[] groups;

    protected AnArrayBasedExpandableListAdapter(GI... groups) {
        this(groups, null, null);
    }

    protected AnArrayBasedExpandableListAdapter(int rowBackground, GI... groups) {
        this(groups, rowBackground, null);
    }

    protected AnArrayBasedExpandableListAdapter(int rowBackground, int evenRowBackground, GI... groups) {
        this(groups, rowBackground, evenRowBackground);
    }

    private AnArrayBasedExpandableListAdapter(GI[] groups, Integer rowBackground, Integer oddRowBackground) {
        this.groups = groups;
        this.rowBackground = rowBackground;
        this.oddRowBackground = oddRowBackground;
    }

    @Override
    public int getGroupCount() {
        return groups.length;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return groups[groupPosition].getElements().length;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return (((long) groupPosition) << 32) + childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public G getGroup(int groupPosition) {
        return groups[groupPosition].getGroupKey();
    }

    @Override
    public C getChild(int groupPosition, int childPosition) {
        return groups[groupPosition].getElements()[childPosition];
    }

    @Override
    public final View getGroupView(int group, boolean isExpanded, View convertView, ViewGroup parent) {
        GV view = null;
        if (convertView != null && convertView instanceof IItemSettable) {
            try {
                view = (GV) convertView;
            } catch (ClassCastException e) {
                view = null;
            }
        }

        if (view == null) {
            view = buildGroupView(group, parent);
        }

        view.setItem(group, getGroup(group));

        return view;
    }

    @Override
    public View getChildView(int group, int child, boolean isLastChild, View convertView, ViewGroup parent) {
        CV view = null;
        if (convertView != null && convertView instanceof IItemSettable) {
            try {
                view = (CV) convertView;
            } catch (ClassCastException e) {
                view = null;
            }
        }

        if (view == null) {
            view = buildChildView(group, child, parent);
        }

        if (rowBackground != null) {
            if (oddRowBackground == null) {
                view.setBackgroundResource(rowBackground);
            } else {
                view.setBackgroundResource((child % 2 == 0) ? rowBackground : oddRowBackground);
            }
        }
        view.setItem(child, getChild(group, child));
        return view;
    }

    protected abstract GV buildGroupView(int position, ViewGroup parent);

    protected abstract CV buildChildView(int groupPosition, int childPosition, ViewGroup parent);
}
