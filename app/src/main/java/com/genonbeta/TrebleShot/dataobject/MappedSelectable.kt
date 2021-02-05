/*
 * Copyright (C) 2020 Veli Tasalı
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
package com.genonbeta.TrebleShot.dataobject

import com.genonbeta.TrebleShot.io.Containable
import com.genonbeta.android.database.DatabaseObject
import android.os.Parcelable
import android.os.Parcel
import androidx.core.util.ObjectsCompat
import com.genonbeta.android.database.SQLQuery
import com.genonbeta.TrebleShot.database.Kuick
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.genonbeta.android.database.KuickDb
import com.genonbeta.android.database.Progress
import com.genonbeta.TrebleShot.dataobject.TransferMember
import android.os.Parcelable.Creator
import com.genonbeta.TrebleShot.dataobject.DeviceAddress
import com.genonbeta.TrebleShot.dataobject.DeviceRoute
import com.genonbeta.android.framework.``object`
import java.util.ArrayList

class MappedSelectable<T : Selectable?>(var selectable: T, engineConnection: IEngineConnection<T>) : Selectable {
    var engineConnection: IEngineConnection<T>
    val selectableTitle: String
        get() = selectable.getSelectableTitle()
    val isSelectableSelected: Boolean
        get() = selectable.isSelectableSelected()

    override fun setSelectableSelected(selected: Boolean): Boolean {
        return engineConnection.setSelected(selectable, selected)
    }

    companion object {
        private fun <T : Selectable?> addToMappedObjectList(
            list: MutableList<MappedSelectable<*>>,
            connection: IEngineConnection<T>
        ) {
            for (selectable in connection.getSelectedItemList()) list.add(MappedSelectable<T>(selectable, connection))
        }

        @JvmStatic
        fun compileFrom(engine: IPerformerEngine?): List<MappedSelectable<*>> {
            val list: MutableList<MappedSelectable<*>> = ArrayList()
            if (engine != null) for (baseEngineConnection in engine.getConnectionList()) if (baseEngineConnection is IEngineConnection<*>) addToMappedObjectList(
                list,
                baseEngineConnection as IEngineConnection<*>
            )
            return list
        }
    }

    init {
        this.engineConnection = engineConnection
    }
}