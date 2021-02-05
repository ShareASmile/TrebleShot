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

package com.genonbeta.TrebleShot.dialog;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import com.genonbeta.TrebleShot.R;
import com.genonbeta.TrebleShot.dataobject.MappedSelectable;
import com.genonbeta.android.framework.object.Selectable;
import com.genonbeta.android.framework.util.actionperformer.IBaseEngineConnection;
import com.genonbeta.android.framework.util.actionperformer.IEngineConnection;
import com.genonbeta.android.framework.util.actionperformer.IPerformerEngine;
import com.genonbeta.android.framework.util.actionperformer.PerformerEngineProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * created by: Veli
 * date: 5.01.2018 10:38
 */

public class SelectionEditorDialog extends AlertDialog.Builder
{
    public static final String TAG = SelectionEditorDialog.class.getSimpleName();

    private LayoutInflater mLayoutInflater;
    private SelfAdapter mAdapter;
    private final List<MappedSelectable<?>> mList;
    private final List<MappedConnection<?>> mMappedConnectionList = new ArrayList<>();

    public SelectionEditorDialog(Activity activity, PerformerEngineProvider provider)
    {
        super(activity);

        IPerformerEngine engine = provider.getPerformerEngine();

        mLayoutInflater = LayoutInflater.from(activity);
        mAdapter = new SelfAdapter();
        mList = MappedSelectable.compileFrom(engine);

        if (engine != null)
            for (IBaseEngineConnection baseEngineConnection : engine.getConnectionList())
                if (baseEngineConnection instanceof IEngineConnection<?>)
                    addToMappedObjectList((IEngineConnection<?>) baseEngineConnection);

        View view = mLayoutInflater.inflate(R.layout.layout_selection_editor, null, false);
        ListView listView = view.findViewById(R.id.listView);

        listView.setAdapter(mAdapter);
        listView.setDividerHeight(0);

        if (mList.size() > 0)
            setView(view);
        else
            setMessage(R.string.text_listEmpty);

        setTitle(R.string.text_previewAndEditList);

        setNeutralButton(R.string.butn_check, null);
        setNegativeButton(R.string.butn_uncheck, null);
        setPositiveButton(R.string.butn_close, null);
    }

    public void checkReversed(TextView textView, View removeSign, Selectable selectable)
    {
        selectable.setSelectableSelected(!selectable.isSelectableSelected());
        mark(textView, removeSign, selectable);
    }

    public void mark(TextView textView, View removeSign, Selectable selectable)
    {
        boolean selected = selectable.isSelectableSelected();
        textView.setEnabled(selected);
        removeSign.setVisibility(selected ? View.GONE : View.VISIBLE);
    }

    public void massCheck(boolean check)
    {
        synchronized (mList) {
            for (MappedConnection<?> mappedConnection : mMappedConnectionList)
                massCheck(check, mappedConnection);
        }

        mAdapter.notifyDataSetChanged();
    }

    private <T extends Selectable> void massCheck(boolean check, MappedConnection<T> mappedConnection)
    {
        mappedConnection.connection.setSelected(mappedConnection.list, new int[mappedConnection.list.size()], check);
    }

    @Override
    public AlertDialog show()
    {
        final AlertDialog dialog = super.show();

        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> massCheck(true));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> massCheck(false));

        return dialog;
    }

    private class SelfAdapter extends BaseAdapter
    {
        @Override
        public int getCount()
        {
            return mList.size();
        }

        @Override
        public Object getItem(int position)
        {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if (convertView == null)
                convertView = mLayoutInflater.inflate(R.layout.list_selection, parent, false);

            final MappedSelectable<?> selectable = (MappedSelectable<?>) getItem(position);
            TextView textView1 = convertView.findViewById(R.id.text);
            View removalSignView = convertView.findViewById(R.id.removalSign);

            textView1.setText(selectable.getSelectableTitle());
            mark(textView1, removalSignView, selectable);

            convertView.setClickable(true);
            convertView.setOnClickListener(v -> checkReversed(textView1, removalSignView, selectable));

            return convertView;
        }
    }

    private <T extends Selectable> void addToMappedObjectList(IEngineConnection<T> connection)
    {
        mMappedConnectionList.add(new MappedConnection<>(connection, connection.getSelectedItemList()));
    }

    private static class MappedConnection<T extends Selectable>
    {
        public IEngineConnection<T> connection;
        public List<T> list;

        public MappedConnection(IEngineConnection<T> connection, List<T> list)
        {
            this.connection = connection;
            this.list = new ArrayList<>(list);
        }
    }
}
