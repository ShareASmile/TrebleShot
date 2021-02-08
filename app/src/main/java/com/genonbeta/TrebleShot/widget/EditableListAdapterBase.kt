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
package com.genonbeta.TrebleShot.widgetimport

import com.genonbeta.TrebleShot.dataobject.Editable
import com.genonbeta.android.framework.util.actionperformer.SelectableProvider
import com.genonbeta.android.framework.widget.ListAdapterBase
import java.util.*

/**
 * created by: veli
 * date: 14/04/18 00:51
 */
interface EditableListAdapterBase<T : Editable> : ListAdapterBase<T>, SelectableProvider<T>, Comparator<T> {
    fun filterItem(item: T): Boolean

    fun getItem(position: Int): T

    fun syncAndNotify(adapterPosition: Int)

    fun syncAllAndNotify()
}