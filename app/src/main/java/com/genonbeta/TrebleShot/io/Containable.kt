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
package com.genonbeta.TrebleShot.io

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import kotlinx.parcelize.Parcelize

@Parcelize
data class Containable(var targetUri: Uri, var children: Array<Uri>) : Parcelable {
    constructor(targetUri: Uri, children: List<Uri>) : this(targetUri, children.toTypedArray())

    override fun equals(other: Any?): Boolean {
        return if (other is Containable) targetUri == other.targetUri else super.equals(other)
    }

    override fun hashCode(): Int {
        return targetUri.hashCode()
    }
}