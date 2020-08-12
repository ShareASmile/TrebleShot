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

package com.genonbeta.TrebleShot.object;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import com.genonbeta.TrebleShot.database.Kuick;
import com.genonbeta.TrebleShot.util.AppUtils;
import com.genonbeta.TrebleShot.util.FileUtils;
import com.genonbeta.TrebleShot.util.Transfers;
import com.genonbeta.android.database.DatabaseObject;
import com.genonbeta.android.database.KuickDb;
import com.genonbeta.android.database.Progress;
import com.genonbeta.android.database.SQLQuery;
import com.genonbeta.android.framework.io.DocumentFile;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.InvalidParameterException;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by: veli
 * Date: 4/24/17 11:50 PM
 */

public class TransferObject implements DatabaseObject<TransferGroup>, Editable
{
    public String name;
    public String file;
    public String mimeType;
    public String directory;
    public long id;
    public long groupId;
    public long size = 0;
    public long lastChangeDate;
    public Type type = Type.INCOMING;

    // When the type is outgoing, the sender gets to have device id : flag list
    protected final Map<String, Flag> mSenderFlagList = new ArrayMap<>();

    // When the type is incoming, the receiver will only have a flag for its status.
    private Flag mReceiverFlag = Flag.PENDING;

    private boolean mDeleteOnRemoval = false;
    private boolean mIsSelected = false;

    public TransferObject()
    {
    }

    public TransferObject(long id, long groupId, String name, String file, String mimeType, long size, Type type)
    {
        this.id = id;
        this.groupId = groupId;
        this.name = name;
        this.file = file;
        this.mimeType = mimeType;
        this.size = size;
        this.type = type;
    }

    public TransferObject(long groupId, long id, Type type)
    {
        this.groupId = groupId;
        this.id = id;
        this.type = type;
    }

    public static TransferObject from(DocumentFile file, long groupId, String directory)
    {
        TransferObject object = new TransferObject(AppUtils.getUniqueNumber(), groupId, file.getName(),
                file.getUri().toString(), file.getType(), file.length(), Type.OUTGOING);

        if (directory != null)
            object.directory = directory;

        return object;
    }

    public static TransferObject from(Shareable shareable, long groupId, String directory)
    {
        TransferObject object = new TransferObject(AppUtils.getUniqueNumber(), groupId, shareable.fileName,
                shareable.uri.toString(), shareable.mimeType, shareable.size, Type.OUTGOING);

        if (directory != null)
            object.directory = directory;

        return object;
    }

    @Override
    public boolean applyFilter(String[] filteringKeywords)
    {
        for (String keyword : filteringKeywords)
            if (name.contains(keyword))
                return true;

        return false;
    }

    @Override
    public boolean comparisonSupported()
    {
        return true;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof TransferObject))
            return super.equals(obj);

        TransferObject otherObject = (TransferObject) obj;
        return otherObject.id == id && type.equals(otherObject.type);
    }

    public Flag getFlag()
    {
        if (!Type.INCOMING.equals(type))
            throw new InvalidParameterException();

        return mReceiverFlag;
    }

    public Flag getFlag(String deviceId)
    {
        if (!Type.OUTGOING.equals(type))
            throw new InvalidParameterException();

        Flag flag;

        synchronized (mSenderFlagList) {
            flag = mSenderFlagList.get(deviceId);
        }

        return flag == null ? Flag.PENDING : flag;
    }

    public Flag[] getFlags()
    {
        synchronized (mSenderFlagList) {
            Flag[] flags = new Flag[mSenderFlagList.size()];
            mSenderFlagList.values().toArray(flags);
            return flags;
        }
    }

    public Map<String, Flag> getSenderFlagList()
    {
        synchronized (mSenderFlagList) {
            Map<String, Flag> map = new ArrayMap<>();
            map.putAll(mSenderFlagList);
            return map;
        }
    }

    public void setFlag(Flag flag)
    {
        if (!Type.INCOMING.equals(type))
            throw new InvalidParameterException();

        mReceiverFlag = flag;
    }

    @Override
    public void setId(long id)
    {
        this.id = id;
    }

    public void putFlag(String deviceId, Flag flag)
    {
        if (!Type.OUTGOING.equals(type))
            throw new InvalidParameterException();

        synchronized (mSenderFlagList) {
            mSenderFlagList.put(deviceId, flag);
        }
    }

    public double getPercentage(ShowingAssignee[] assignees, @Nullable String deviceId)
    {
        if (assignees.length == 0)
            return 0;

        if (Type.INCOMING.equals(type))
            return Transfers.getPercentageByFlag(getFlag(), size);
        else if (deviceId != null)
            return Transfers.getPercentageByFlag(getFlag(deviceId), size);

        double percentageIndex = 0;
        int senderAssignees = 0;
        for (ShowingAssignee assignee : assignees) {
            if (!Type.OUTGOING.equals(assignee.type))
                continue;

            senderAssignees++;
            percentageIndex += Transfers.getPercentageByFlag(getFlag(
                    assignee.deviceId), size);
        }

        return percentageIndex > 0 ? percentageIndex / senderAssignees : 0;
    }

    @Override
    public SQLQuery.Select getWhere()
    {
        return new SQLQuery.Select(Kuick.TABLE_TRANSFER).setWhere(
                String.format("%s = ? AND %s = ? AND %s = ?", Kuick.FIELD_TRANSFER_GROUPID,
                        Kuick.FIELD_TRANSFER_ID, Kuick.FIELD_TRANSFER_TYPE),
                String.valueOf(groupId), String.valueOf(id), type.toString());
    }

    @Override
    public ContentValues getValues()
    {
        ContentValues values = new ContentValues();

        values.put(Kuick.FIELD_TRANSFER_ID, id);
        values.put(Kuick.FIELD_TRANSFER_GROUPID, groupId);
        values.put(Kuick.FIELD_TRANSFER_NAME, name);
        values.put(Kuick.FIELD_TRANSFER_SIZE, size);
        values.put(Kuick.FIELD_TRANSFER_MIME, mimeType);
        values.put(Kuick.FIELD_TRANSFER_TYPE, type.toString());
        values.put(Kuick.FIELD_TRANSFER_FILE, file);
        values.put(Kuick.FIELD_TRANSFER_DIRECTORY, directory);
        values.put(Kuick.FIELD_TRANSFER_LASTCHANGETIME, lastChangeDate);

        if (Type.INCOMING.equals(type)) {
            values.put(Kuick.FIELD_TRANSFER_FLAG, mReceiverFlag.toString());
        } else {
            JSONObject object = new JSONObject();

            synchronized (mSenderFlagList) {
                for (String deviceId : mSenderFlagList.keySet())
                    try {
                        object.put(deviceId, mSenderFlagList.get(deviceId));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
            }

            values.put(Kuick.FIELD_TRANSFER_FLAG, object.toString());
        }

        return values;
    }

    @Override
    public void reconstruct(SQLiteDatabase db, KuickDb kuick, ContentValues item)
    {
        this.name = item.getAsString(Kuick.FIELD_TRANSFER_NAME);
        this.file = item.getAsString(Kuick.FIELD_TRANSFER_FILE);
        this.size = item.getAsLong(Kuick.FIELD_TRANSFER_SIZE);
        this.mimeType = item.getAsString(Kuick.FIELD_TRANSFER_MIME);
        this.id = item.getAsLong(Kuick.FIELD_TRANSFER_ID);
        this.groupId = item.getAsLong(Kuick.FIELD_TRANSFER_GROUPID);
        this.type = Type.valueOf(item.getAsString(Kuick.FIELD_TRANSFER_TYPE));
        this.directory = item.getAsString(Kuick.FIELD_TRANSFER_DIRECTORY);

        // Added with DB version 13
        if (item.containsKey(Kuick.FIELD_TRANSFER_LASTCHANGETIME))
            this.lastChangeDate = item.getAsLong(Kuick.FIELD_TRANSFER_LASTCHANGETIME);

        String flagString = item.getAsString(Kuick.FIELD_TRANSFER_FLAG);

        if (Type.INCOMING.equals(this.type)) {
            try {
                mReceiverFlag = Flag.valueOf(flagString);
            } catch (Exception e) {
                try {
                    mReceiverFlag = Flag.IN_PROGRESS;
                    mReceiverFlag.setBytesValue(Long.parseLong(flagString));
                } catch (NumberFormatException e1) {
                    mReceiverFlag = Flag.PENDING;
                }
            }
        } else {
            try {
                JSONObject jsonObject = new JSONObject(flagString);
                Iterator<String> iterator = jsonObject.keys();

                synchronized (mSenderFlagList) {
                    mSenderFlagList.clear();
                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        String value = jsonObject.getString(key);
                        Flag flag;

                        try {
                            flag = Flag.valueOf(value);
                        } catch (Exception e) {
                            try {
                                flag = Flag.IN_PROGRESS;
                                flag.setBytesValue(Long.parseLong(value));
                            } catch (NumberFormatException e1) {
                                flag = Flag.PENDING;
                            }
                        }

                        mSenderFlagList.put(key, flag);
                    }
                }
            } catch (JSONException ignored) {
            }
        }
    }

    public void setDeleteOnRemoval(boolean delete)
    {
        mDeleteOnRemoval = delete;
    }

    @Override
    public void onCreateObject(SQLiteDatabase db, KuickDb kuick, TransferGroup parent, Progress.Listener listener)
    {
        lastChangeDate = System.currentTimeMillis();
    }

    @Override
    public void onUpdateObject(SQLiteDatabase db, KuickDb kuick, TransferGroup parent, Progress.Listener listener)
    {
        lastChangeDate = System.currentTimeMillis();
    }

    @Override
    public void onRemoveObject(SQLiteDatabase db, KuickDb kuick, TransferGroup parent, Progress.Listener listener)
    {
        // Normally we'd like to check every file, but it may take a while.
        if (!Type.INCOMING.equals(type) || (!Flag.INTERRUPTED.equals(getFlag())
                && (!Flag.DONE.equals(getFlag()) || !mDeleteOnRemoval)))
            return;

        try {
            if (parent == null) {
                Log.d(TransferObject.class.getSimpleName(), "onRemoveObject: Had to recreate the group");
                parent = new TransferGroup(groupId);
                kuick.reconstruct(parent);
            }

            DocumentFile file = FileUtils.getIncomingPseudoFile(kuick.getContext(), this, parent,
                    false);

            if (file != null && file.isFile())
                file.delete();
        } catch (Exception ignored) {
            // do nothing
        }
    }

    @Override
    public String getComparableName()
    {
        return getSelectableTitle();
    }

    @Override
    public long getComparableDate()
    {
        return lastChangeDate;
    }

    @Override
    public long getComparableSize()
    {
        return size;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public long getId()
    {
        return String.format("%d_%d", id, type.ordinal()).hashCode();
    }

    @Override
    public String getSelectableTitle()
    {
        return name;
    }

    @Override
    public boolean isSelectableSelected()
    {
        return mIsSelected;
    }

    @Override
    public boolean setSelectableSelected(boolean selected)
    {
        mIsSelected = selected;
        return true;
    }

    public enum Type
    {
        INCOMING,
        OUTGOING
    }

    public enum Flag
    {
        INTERRUPTED,
        PENDING,
        REMOVED,
        IN_PROGRESS,
        DONE;

        private long bytesValue;

        public long getBytesValue()
        {
            return bytesValue;
        }

        public void setBytesValue(long bytesValue)
        {
            this.bytesValue = bytesValue;
        }

        @NonNull
        @Override
        public String toString()
        {
            return getBytesValue() > 0 ? String.valueOf(getBytesValue()) : super.toString();
        }
    }
}