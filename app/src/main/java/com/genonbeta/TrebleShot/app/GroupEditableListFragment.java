/*
 * Copyright (C) 2019 Veli Tasalı
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.genonbeta.TrebleShot.app;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import com.genonbeta.TrebleShot.R;
import com.genonbeta.TrebleShot.widget.GroupEditableListAdapter;

import java.util.Map;

/**
 * created by: veli
 * date: 30.03.2018 16:10
 */

public abstract class GroupEditableListFragment<T extends GroupEditableListAdapter.GroupEditable,
        V extends GroupEditableListAdapter.GroupViewHolder, E extends GroupEditableListAdapter<T, V>>
        extends EditableListFragment<T, V, E>
{
    private final Map<String, Integer> mGroupingOptions = new ArrayMap<>();
    private int mDefaultGroupingCriteria = GroupEditableListAdapter.MODE_GROUP_BY_NOTHING;

    @Override
    public int onGridSpanSize(int viewType, int currentSpanSize)
    {
        return viewType == GroupEditableListAdapter.VIEW_TYPE_REPRESENTATIVE
                || viewType == GroupEditableListAdapter.VIEW_TYPE_ACTION_BUTTON
                ? currentSpanSize : super.onGridSpanSize(viewType, currentSpanSize);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);

        if (!isUsingLocalSelection() || !isLocalSelectionActivated()) {
            Map<String, Integer> options = new ArrayMap<>();

            onGroupingOptions(options);

            mGroupingOptions.clear();
            mGroupingOptions.putAll(options);

            if (mGroupingOptions.size() > 0) {
                inflater.inflate(R.menu.actions_abs_group_shareable_list, menu);
                MenuItem groupingItem = menu.findItem(R.id.actions_abs_group_shareable_grouping);

                if (groupingItem != null)
                    applyDynamicMenuItems(groupingItem, R.id.actions_abs_group_shareable_group_grouping,
                            mGroupingOptions);
            }
        }
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu)
    {
        super.onPrepareOptionsMenu(menu);

        if (!isUsingLocalSelection() || !isLocalSelectionActivated()) {
            checkPreferredDynamicItem(menu.findItem(R.id.actions_abs_group_shareable_grouping), getGroupingCriteria(),
                    mGroupingOptions);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getGroupId() == R.id.actions_abs_group_shareable_group_grouping)
            changeGroupingCriteria(item.getOrder());
        else
            return super.onOptionsItemSelected(item);

        return true;
    }

    public void onGroupingOptions(Map<String, Integer> options)
    {
    }

    public void changeGroupingCriteria(int criteria)
    {
        getViewPreferences().edit()
                .putInt(getUniqueSettingKey("GroupBy"), criteria)
                .apply();

        getAdapter().setGroupBy(criteria);

        refreshList();
    }

    public int getGroupingCriteria()
    {
        return getViewPreferences().getInt(getUniqueSettingKey("GroupBy"), mDefaultGroupingCriteria);
    }

    public void setDefaultGroupingCriteria(int groupingCriteria)
    {
        mDefaultGroupingCriteria = groupingCriteria;
    }

    @Override
    protected void setListAdapter(E adapter, boolean hadAdapter)
    {
        super.setListAdapter(adapter, hadAdapter);
        adapter.setGroupBy(getGroupingCriteria());
    }
}
