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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import com.genonbeta.TrebleShot.R;
import com.genonbeta.TrebleShot.activity.ViewTransferActivity;
import com.genonbeta.TrebleShot.graphics.drawable.TextDrawable;
import com.genonbeta.TrebleShot.object.IndexOfTransferGroup;
import com.genonbeta.TrebleShot.object.ShowingAssignee;
import com.genonbeta.TrebleShot.object.TransferObject;
import com.genonbeta.TrebleShot.util.AppUtils;
import com.genonbeta.TrebleShot.util.DeviceLoader;
import com.genonbeta.TrebleShot.util.Transfers;

public class ToggleMultipleTransferDialog extends AlertDialog.Builder
{
    private ViewTransferActivity mActivity;
    private ShowingAssignee[] mAssignees;
    private LayoutInflater mInflater;
    private TextDrawable.IShapeBuilder mIconBuilder;

    public ToggleMultipleTransferDialog(@NonNull final ViewTransferActivity activity, final IndexOfTransferGroup index)
    {
        super(activity);

        mActivity = activity;
        mInflater = LayoutInflater.from(activity);
        mIconBuilder = AppUtils.getDefaultIconBuilder(activity);
        mAssignees = index.assignees;

        if (mAssignees.length > 0)
            setAdapter(new ActiveListAdapter(), (dialog, which) -> startTransfer(activity, index, mAssignees[which]));

        setNegativeButton(R.string.butn_close, null);

        if (index.hasOutgoing())
            setNeutralButton(R.string.butn_addDevices, (dialog, which) -> activity.startDeviceAddingActivity());

        ShowingAssignee senderAssignee = null;

        for (ShowingAssignee assignee : index.assignees)
            if (TransferObject.Type.INCOMING.equals(assignee.type)) {
                senderAssignee = assignee;
                break;
            }

        if (index.hasIncoming() && senderAssignee != null) {
            ShowingAssignee finalSenderAssignee = senderAssignee;
            setPositiveButton(R.string.butn_receive, (dialog, which) -> startTransfer(activity, index,
                    finalSenderAssignee));
        }
    }

    private void startTransfer(ViewTransferActivity activity, IndexOfTransferGroup index, ShowingAssignee assignee)
    {
        if (mActivity.isDeviceRunning(assignee.deviceId))
            Transfers.pauseTransfer(activity, assignee);
        else
            Transfers.startTransferWithTest(activity, index.group, assignee);
    }

    private class ActiveListAdapter extends BaseAdapter
    {
        @Override
        public int getCount()
        {
            return mAssignees.length;
        }

        @Override
        public Object getItem(int position)
        {
            return mAssignees[position];
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
                convertView = mInflater.inflate(R.layout.list_toggle_transfer, parent, false);

            ShowingAssignee assignee = (ShowingAssignee) getItem(position);
            ImageView image = convertView.findViewById(R.id.image);
            TextView text = convertView.findViewById(R.id.text);
            ImageView actionImage = convertView.findViewById(R.id.actionImage);

            text.setText(assignee.device.username);
            actionImage.setImageResource(mActivity.isDeviceRunning(assignee.deviceId) ? R.drawable.ic_pause_white_24dp
                    : (TransferObject.Type.INCOMING.equals(assignee.type) ? R.drawable.ic_arrow_down_white_24dp
                    : R.drawable.ic_arrow_up_white_24dp));
            DeviceLoader.showPictureIntoView(assignee.device, image, mIconBuilder);

            return convertView;
        }
    }
}
