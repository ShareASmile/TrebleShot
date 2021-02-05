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
package com.genonbeta.TrebleShot.migration.db.`object`

import com.genonbeta.TrebleShot.dataobject.MappedSelectable.Companion.compileFrom
import com.genonbeta.TrebleShot.dataobject.Identity.Companion.withORs
import com.genonbeta.TrebleShot.dataobject.Identifier.Companion.from
import com.genonbeta.TrebleShot.dataobject.TransferIndex.bytesPending
import com.genonbeta.TrebleShot.dataobject.TransferItem.Flag.bytesValue
import com.genonbeta.TrebleShot.dataobject.TransferItem.flag
import com.genonbeta.TrebleShot.dataobject.TransferItem.putFlag
import com.genonbeta.TrebleShot.dataobject.Identity.Companion.withANDs
import com.genonbeta.TrebleShot.dataobject.TransferItem.Companion.from
import com.genonbeta.TrebleShot.dataobject.DeviceAddress.hostAddress
import com.genonbeta.TrebleShot.dataobject.Container.expand
import com.genonbeta.TrebleShot.dataobject.Device.equals
import com.genonbeta.TrebleShot.dataobject.TransferItem.flags
import com.genonbeta.TrebleShot.dataobject.TransferItem.getFlag
import com.genonbeta.TrebleShot.dataobject.TransferItem.Flag.toString
import com.genonbeta.TrebleShot.dataobject.TransferItem.reconstruct
import com.genonbeta.TrebleShot.dataobject.Device.generatePictureId
import com.genonbeta.TrebleShot.dataobject.TransferItem.setDeleteOnRemoval
import com.genonbeta.TrebleShot.dataobject.MappedSelectable.selectableTitle
import com.genonbeta.TrebleShot.dataobject.TransferIndex.hasOutgoing
import com.genonbeta.TrebleShot.dataobject.TransferIndex.hasIncoming
import com.genonbeta.TrebleShot.dataobject.Comparable.comparisonSupported
import com.genonbeta.TrebleShot.dataobject.Comparable.comparableDate
import com.genonbeta.TrebleShot.dataobject.Comparable.comparableSize
import com.genonbeta.TrebleShot.dataobject.Comparable.comparableName
import com.genonbeta.TrebleShot.dataobject.Editable.applyFilter
import com.genonbeta.TrebleShot.dataobject.Editable.id
import com.genonbeta.TrebleShot.dataobject.Shareable.setSelectableSelected
import com.genonbeta.TrebleShot.dataobject.Shareable.initialize
import com.genonbeta.TrebleShot.dataobject.Shareable.isSelectableSelected
import com.genonbeta.TrebleShot.dataobject.Shareable.comparisonSupported
import com.genonbeta.TrebleShot.dataobject.Shareable.comparableSize
import com.genonbeta.TrebleShot.dataobject.Shareable.applyFilter
import com.genonbeta.TrebleShot.dataobject.Device.hashCode
import com.genonbeta.TrebleShot.dataobject.TransferIndex.percentage
import com.genonbeta.TrebleShot.dataobject.TransferIndex.getMemberAsTitle
import com.genonbeta.TrebleShot.dataobject.TransferIndex.isSelectableSelected
import com.genonbeta.TrebleShot.dataobject.TransferIndex.numberOfCompleted
import com.genonbeta.TrebleShot.dataobject.TransferIndex.numberOfTotal
import com.genonbeta.TrebleShot.dataobject.TransferIndex.bytesTotal
import com.genonbeta.TrebleShot.dataobject.TransferItem.isSelectableSelected
import com.genonbeta.TrebleShot.dataobject.TransferItem.setSelectableSelected
import com.genonbeta.TrebleShot.dataobject.TransferItem.senderFlagList
import com.genonbeta.TrebleShot.dataobject.TransferItem.getPercentage
import com.genonbeta.TrebleShot.dataobject.TransferItem.setId
import com.genonbeta.TrebleShot.dataobject.TransferItem.comparableDate
import com.genonbeta.TrebleShot.dataobject.Identity.equals
import com.genonbeta.TrebleShot.dataobject.Transfer.equals
import com.genonbeta.TrebleShot.dataobject.TransferMember.reconstruct
import android.os.Parcelable
import android.os.Parcel
import com.genonbeta.TrebleShot.io.Containable
import android.os.Parcelable.Creator
import com.genonbeta.TrebleShot.R
import android.content.DialogInterface
import com.genonbeta.TrebleShot.activity.AddDeviceActivity.AvailableFragment
import android.content.Intent
import com.genonbeta.TrebleShot.activity.AddDeviceActivity
import androidx.annotation.DrawableRes
import com.genonbeta.TrebleShot.dataobject.Shareable
import com.genonbeta.android.framework.util.actionperformer.PerformerEngineProvider
import com.genonbeta.TrebleShot.ui.callback.LocalSharingCallback
import com.genonbeta.android.framework.ui.PerformerMenu
import android.view.MenuInflater
import com.genonbeta.android.framework.util.actionperformer.IPerformerEngine
import com.genonbeta.TrebleShot.ui.callback.SharingPerformerMenuCallback
import com.genonbeta.TrebleShot.dataobject.MappedSelectable
import com.genonbeta.TrebleShot.dialog.ChooseSharingMethodDialog
import com.genonbeta.TrebleShot.dialog.ChooseSharingMethodDialog.PickListener
import com.genonbeta.TrebleShot.dialog.ChooseSharingMethodDialog.SharingMethod
import com.genonbeta.TrebleShot.task.OrganizeLocalSharingTask
import com.genonbeta.TrebleShot.App
import com.genonbeta.TrebleShot.util.NotificationUtils
import com.genonbeta.TrebleShot.database.Kuick
import com.genonbeta.TrebleShot.util.AppUtils
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import com.genonbeta.TrebleShot.service.backgroundservice.BaseAttachableAsyncTask
import android.content.IntentFilter
import android.content.BroadcastReceiver
import android.os.Bundle
import androidx.annotation.StyleRes
import android.content.pm.PackageManager
import com.genonbeta.TrebleShot.activity.WelcomeActivity
import com.genonbeta.TrebleShot.GlideApp
import com.bumptech.glide.request.target.CustomTarget
import android.graphics.drawable.Drawable
import android.graphics.Bitmap
import com.genonbeta.TrebleShot.config.AppConfig
import kotlin.jvm.Synchronized
import com.genonbeta.TrebleShot.service.BackgroundService
import android.os.PowerManager
import android.graphics.BitmapFactory
import com.genonbeta.TrebleShot.dialog.RationalePermissionRequest
import com.genonbeta.TrebleShot.service.backgroundservice.AttachedTaskListener
import com.genonbeta.TrebleShot.service.backgroundservice.AttachableAsyncTask
import com.genonbeta.TrebleShot.dialog.ProfileEditorDialog
import android.widget.ProgressBar
import android.view.LayoutInflater
import kotlin.jvm.JvmOverloads
import com.genonbeta.android.framework.widget.RecyclerViewAdapter
import com.genonbeta.TrebleShot.widget.EditableListAdapter
import com.genonbeta.android.framework.app.DynamicRecyclerViewFragment
import com.genonbeta.TrebleShot.app.IEditableListFragment
import com.genonbeta.android.framework.util.actionperformer.IEngineConnection
import com.genonbeta.android.framework.util.actionperformer.EngineConnection
import com.genonbeta.android.framework.util.actionperformer.PerformerEngine
import com.genonbeta.TrebleShot.app.EditableListFragment.FilteringDelegate
import android.database.ContentObserver
import com.genonbeta.TrebleShot.app.EditableListFragment.LayoutClickListener
import com.genonbeta.TrebleShot.app.EditableListFragmentBase
import com.genonbeta.TrebleShot.app.EditableListFragment
import android.view.ViewGroup
import com.genonbeta.TrebleShot.view.LongTextBubbleFastScrollViewProvider
import com.genonbeta.TrebleShot.widget.recyclerview.ItemOffsetDecoration
import com.genonbeta.TrebleShot.widget.EditableListAdapterBase
import android.os.Looper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import android.view.View.OnLongClickListener
import com.genonbeta.android.framework.util.actionperformer.SelectableNotFoundException
import com.genonbeta.android.framework.util.actionperformer.CouldNotAlterException
import com.genonbeta.TrebleShot.widget.recyclerview.SwipeSelectionListener
import com.genonbeta.TrebleShot.util.SelectionUtils
import com.genonbeta.TrebleShot.dialog.SelectionEditorDialog
import com.genonbeta.android.framework.util.actionperformer.IBaseEngineConnection
import com.genonbeta.android.framework.``object`
import java.lang.Exception

/**
 * created by: veli
 * date: 7/31/19 11:03 AM
 */
class NetworkDeviceV12 : DatabaseObject<Any?> {
    var brand: String? = null
    var model: String? = null
    var nickname: String? = null
    var deviceId: String? = null
    var versionName: String? = null
    var versionNumber = 0
    var tmpSecureKey = 0
    var lastUsageTime: Long = 0
    var isTrusted = false
    var isRestricted = false
    var isLocalAddress = false

    constructor() {}
    constructor(deviceId: String?) {
        this.deviceId = deviceId
    }

    fun generatePictureId(): String {
        return String.format("picture_%s", deviceId)
    }

    override fun getWhere(): SQLQuery.Select {
        return SQLQuery.Select(Kuick.Companion.TABLE_DEVICES)
            .setWhere(Kuick.Companion.FIELD_DEVICES_ID + "=?", deviceId)
    }

    override fun getValues(): ContentValues {
        val values = ContentValues()
        values.put(Kuick.Companion.FIELD_DEVICES_ID, deviceId)
        values.put(Kuick.Companion.FIELD_DEVICES_USER, nickname)
        values.put(Kuick.Companion.FIELD_DEVICES_BRAND, brand)
        values.put(Kuick.Companion.FIELD_DEVICES_MODEL, model)
        values.put(Kuick.Companion.FIELD_DEVICES_BUILDNAME, versionName)
        values.put(Kuick.Companion.FIELD_DEVICES_BUILDNUMBER, versionNumber)
        values.put(Kuick.Companion.FIELD_DEVICES_LASTUSAGETIME, lastUsageTime)
        values.put(Kuick.Companion.FIELD_DEVICES_ISRESTRICTED, if (isRestricted) 1 else 0)
        values.put(Kuick.Companion.FIELD_DEVICES_ISTRUSTED, if (isTrusted) 1 else 0)
        values.put(Kuick.Companion.FIELD_DEVICES_ISLOCALADDRESS, if (isLocalAddress) 1 else 0)
        return values
    }

    override fun reconstruct(db: SQLiteDatabase, kuick: KuickDb, item: ContentValues) {
        deviceId = item.getAsString(Kuick.Companion.FIELD_DEVICES_ID)
        nickname = item.getAsString(Kuick.Companion.FIELD_DEVICES_USER)
        brand = item.getAsString(Kuick.Companion.FIELD_DEVICES_BRAND)
        model = item.getAsString(Kuick.Companion.FIELD_DEVICES_MODEL)
        versionName = item.getAsString(Kuick.Companion.FIELD_DEVICES_BUILDNAME)
        versionNumber = item.getAsInteger(Kuick.Companion.FIELD_DEVICES_BUILDNUMBER)
        lastUsageTime = item.getAsLong(Kuick.Companion.FIELD_DEVICES_LASTUSAGETIME)
        isTrusted = item.getAsInteger(Kuick.Companion.FIELD_DEVICES_ISTRUSTED) == 1
        isRestricted = item.getAsInteger(Kuick.Companion.FIELD_DEVICES_ISRESTRICTED) == 1
        isLocalAddress = item.getAsInteger(Kuick.Companion.FIELD_DEVICES_ISLOCALADDRESS) == 1
    }

    override fun onCreateObject(db: SQLiteDatabase, kuick: KuickDb, parent: Any, listener: Progress.Listener) {}
    override fun onUpdateObject(db: SQLiteDatabase, kuick: KuickDb, parent: Any, listener: Progress.Listener) {}
    override fun onRemoveObject(db: SQLiteDatabase, kuick: KuickDb, parent: Any, listener: Progress.Listener) {
        kuick.getContext().deleteFile(generatePictureId())
        kuick.remove(
            db, SQLQuery.Select(Kuick.Companion.TABLE_DEVICEADDRESS)
                .setWhere(Kuick.Companion.FIELD_DEVICEADDRESS_DEVICEID + "=?", deviceId)
        )
        val assignees: List<TransferAssigneeV12> = kuick.castQuery<NetworkDeviceV12, TransferAssigneeV12>(
            db, SQLQuery.Select(
                Kuick.Companion.TABLE_TRANSFERMEMBER
            ).setWhere(
                Kuick.Companion.FIELD_TRANSFERMEMBER_DEVICEID + "=?",
                deviceId
            ), TransferAssigneeV12::class.java, null
        )

        // We are ensuring that the transfer group is still valid for other devices
        for (assignee in assignees) {
            kuick.remove<NetworkDeviceV12, TransferAssigneeV12>(db, assignee, this, listener)
            try {
                val transferGroup = TransferGroupV12(assignee.groupId)
                kuick.reconstruct<NetworkDeviceV12, TransferGroupV12>(db, transferGroup)
                val relatedAssignees: List<TransferAssigneeV12> =
                    kuick.castQuery<NetworkDeviceV12, TransferAssigneeV12>(
                        SQLQuery.Select(
                            Kuick.Companion.TABLE_TRANSFERMEMBER
                        ).setWhere(
                            Kuick.Companion.FIELD_TRANSFERMEMBER_TRANSFERID + "=?",
                            transferGroup.groupId.toString()
                        ), TransferAssigneeV12::class.java
                    )
                if (relatedAssignees.size == 0) kuick.remove<NetworkDeviceV12, TransferGroupV12>(
                    db,
                    transferGroup,
                    this,
                    listener
                )
            } catch (ignored: Exception) {
            }
        }
    }
}