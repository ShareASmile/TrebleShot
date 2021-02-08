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
package com.genonbeta.TrebleShot.dataobject

import java.util.*

class LoadedMember : TransferMember, Editable {
    @JvmField
    var device: Device? = null

    constructor() {}
    constructor(transferId: Long, deviceId: String?, type: TransferItem.Type?) : super(transferId, deviceId, type) {}

    override fun applyFilter(filteringKeywords: Array<String>): Boolean {
        return false
    }

    override var id: Long
        get() = String.format(Locale.getDefault(), "%s_%d", deviceId, transferId).hashCode().toLong()
        set(id) {}

    override fun comparisonSupported(): Boolean {
        return false
    }

    override val comparableName: String?
        get() = device!!.username
    override val comparableDate: Long
        get() = device!!.lastUsageTime
    override val comparableSize: Long
        get() = 0
    val selectableTitle: String
        get() = device!!.username!!
    val isSelectableSelected: Boolean
        get() = false

    override fun setSelectableSelected(selected: Boolean): Boolean {
        return false
    }
}