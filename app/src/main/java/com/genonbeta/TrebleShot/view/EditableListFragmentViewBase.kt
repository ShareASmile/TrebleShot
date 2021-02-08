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
package com.genonbeta.TrebleShot.viewimport

import com.genonbeta.TrebleShot.app.EditableListFragment.LayoutClickListener
import com.genonbeta.android.framework.widget.RecyclerViewAdapter

/**
 * created by: veli
 * date: 8/24/18 1:36 PM
 */
interface EditableListFragmentViewBase<V : RecyclerViewAdapter.ViewHolder> {
    fun performLayoutClick(holder: V): Boolean

    fun performLayoutLongClick(holder: V): Boolean

    fun registerLayoutViewClicks(holder: V)

    fun setItemSelected(holder: V): Boolean

    fun setItemSelected(holder: V, force: Boolean): Boolean

    fun setLayoutClickListener(clickListener: LayoutClickListener<V>?)
}