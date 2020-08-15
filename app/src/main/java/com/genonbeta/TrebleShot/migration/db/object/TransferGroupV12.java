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

package com.genonbeta.TrebleShot.migration.db.object;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import com.genonbeta.TrebleShot.database.Kuick;
import com.genonbeta.TrebleShot.migration.db.Migration;
import com.genonbeta.android.database.DatabaseObject;
import com.genonbeta.android.database.KuickDb;
import com.genonbeta.android.database.Progress;
import com.genonbeta.android.database.SQLQuery;

/**
 * created by: veli
 * date: 7/31/19 11:02 AM
 */
public class TransferGroupV12 implements DatabaseObject<NetworkDeviceV12>
{
    public long groupId;
    public long dateCreated;
    public String savePath;
    public boolean isServedOnWeb;

    public TransferGroupV12()
    {
    }

    public TransferGroupV12(long groupId)
    {
        this.groupId = groupId;
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof TransferGroupV12 && ((TransferGroupV12) obj).groupId == groupId;
    }

    @Override
    public void reconstruct(SQLiteDatabase db, KuickDb kuick, ContentValues item)
    {
        this.groupId = item.getAsLong(Kuick.FIELD_TRANSFER_ID);
        this.savePath = item.getAsString(Kuick.FIELD_TRANSFER_SAVEPATH);
        this.dateCreated = item.getAsLong(Kuick.FIELD_TRANSFER_DATECREATED);
        this.isServedOnWeb = item.getAsInteger(Kuick.FIELD_TRANSFER_ISSHAREDONWEB) == 1;
    }

    @Override
    public ContentValues getValues()
    {
        ContentValues values = new ContentValues();

        values.put(Kuick.FIELD_TRANSFER_ID, groupId);
        values.put(Kuick.FIELD_TRANSFER_SAVEPATH, savePath);
        values.put(Kuick.FIELD_TRANSFER_DATECREATED, dateCreated);
        values.put(Kuick.FIELD_TRANSFER_ISSHAREDONWEB, isServedOnWeb ? 1 : 0);

        return values;
    }

    @Override
    public SQLQuery.Select getWhere()
    {
        return new SQLQuery.Select(Kuick.TABLE_TRANSFER)
                .setWhere(Kuick.FIELD_TRANSFER_ID + "=?", String.valueOf(groupId));
    }

    @Override
    public void onCreateObject(SQLiteDatabase db, KuickDb kuick, NetworkDeviceV12 parent, Progress.Listener listener)
    {
        this.dateCreated = System.currentTimeMillis();
    }

    @Override
    public void onUpdateObject(SQLiteDatabase db, KuickDb kuick, NetworkDeviceV12 parent, Progress.Listener listener)
    {

    }

    @Override
    public void onRemoveObject(SQLiteDatabase db, KuickDb kuick, NetworkDeviceV12 parent, Progress.Listener listener)
    {
        kuick.remove(db, new SQLQuery.Select(Migration.v12.TABLE_DIVISTRANSFER)
                .setWhere(String.format("%s = ?", Kuick.FIELD_TRANSFERITEM_TRANSFERID), String.valueOf(groupId)));

        kuick.remove(db, new SQLQuery.Select(Kuick.TABLE_TRANSFERMEMBER)
                .setWhere(Kuick.FIELD_TRANSFERMEMBER_TRANSFERID + "=?", String.valueOf(groupId)));

        kuick.removeAsObject(db, new SQLQuery.Select(Kuick.TABLE_TRANSFERITEM)
                        .setWhere(Kuick.FIELD_TRANSFERITEM_TRANSFERID + "=?", String.valueOf(groupId)),
                TransferObjectV12.class, this, listener, null);
    }
}