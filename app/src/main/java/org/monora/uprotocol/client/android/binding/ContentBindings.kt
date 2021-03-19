/*
 * Copyright (C) 2021 Veli Tasalı
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

package org.monora.uprotocol.client.android.binding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import org.monora.uprotocol.client.android.GlideApp
import org.monora.uprotocol.client.android.database.model.UTransferItem
import org.monora.uprotocol.client.android.util.MimeIcons
import org.monora.uprotocol.core.transfer.TransferItem

@BindingAdapter("thumbnailOf")
fun loadThumbnailOf(imageView: ImageView, transferItem: UTransferItem) {
    if (transferItem.type == TransferItem.Type.Outgoing) {
        GlideApp.with(imageView)
            .load(transferItem.location)
            .circleCrop()
            .into(imageView)
    }
}

@BindingAdapter("iconOf")
fun loadIconOf(imageView: ImageView, transferItem: UTransferItem) {
    imageView.setImageResource(MimeIcons.loadMimeIcon(transferItem.mimeType))
}