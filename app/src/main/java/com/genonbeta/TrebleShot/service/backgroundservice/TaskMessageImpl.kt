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
package com.genonbeta.TrebleShot.service.backgroundservice

import android.app.Activity
import android.content.*
import android.view.View
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import com.genonbeta.TrebleShot.R
import com.genonbeta.TrebleShot.util.NotificationUtils
import java.util.*

class TaskMessageImpl : TaskMessage {
    private var mTitle: String? = null
    private var mMessage: String? = null
    private var mTone: Tone = Tone.Neutral
    private val mActionList: MutableList<TaskMessage.Action> = ArrayList()
    override fun addAction(action: TaskMessage.Action): TaskMessage {
        synchronized(mActionList) { mActionList.add(action) }
        return this
    }

    override fun addAction(context: Context, nameRes: Int, callback: TaskMessage.Callback?): TaskMessage {
        return addAction(context.getString(nameRes), callback)
    }

    override fun addAction(name: String?, callback: TaskMessage.Callback?): TaskMessage {
        return addAction(name, Tone.Neutral, callback)
    }

    override fun addAction(context: Context, nameRes: Int, tone: Tone?, callback: TaskMessage.Callback?): TaskMessage {
        return addAction(context.getString(nameRes), tone, callback)
    }

    override fun addAction(name: String?, tone: Tone?, callback: TaskMessage.Callback?): TaskMessage {
        val action = TaskMessage.Action()
        action.name = name
        action.tone = tone
        action.callback = callback
        return addAction(action)
    }

    override fun getActionList(): List<TaskMessage.Action> {
        synchronized(mActionList) { return ArrayList(mActionList) }
    }

    override fun getMessage(): String? {
        return mMessage
    }

    override fun getTitle(): String? {
        return mTitle
    }

    override fun getTone(): Tone {
        return mTone
    }

    override fun removeAction(action: TaskMessage.Action): TaskMessage {
        synchronized(mActionList) { mActionList.remove(action) }
        return this
    }

    override fun setMessage(context: Context, msgRes: Int): TaskMessage {
        return setMessage(context.getString(msgRes))
    }

    override fun setMessage(msg: String?): TaskMessage {
        mMessage = msg
        return this
    }

    override fun setTitle(context: Context, titleRes: Int): TaskMessage {
        return setTitle(context.getString(titleRes))
    }

    override fun setTitle(title: String?): TaskMessage {
        mTitle = title
        return this
    }

    override fun setTone(tone: Tone): TaskMessage {
        mTone = tone
        return this
    }

    override fun sizeOfActions(): Int {
        synchronized(mActionList) { return mActionList.size }
    }

    override fun toDialogBuilder(activity: Activity?): AlertDialog.Builder {
        val builder = AlertDialog.Builder(activity!!)
            .setTitle(title)
            .setMessage(message)
        synchronized(mActionList) {
            val appliedTones = BooleanArray(TaskMessage.Tone.values().size)
            for (action in mActionList) {
                if (appliedTones[action.tone.ordinal]) continue
                when (action.tone) {
                    Tone.Positive -> builder.setPositiveButton(action.name) { dialog: DialogInterface?, which: Int ->
                        action.callback.call(
                            activity
                        )
                    }
                    Tone.Negative -> builder.setNegativeButton(action.name) { dialog: DialogInterface?, which: Int ->
                        action.callback.call(
                            activity
                        )
                    }
                    Tone.Neutral -> builder.setNeutralButton(action.name) { dialog: DialogInterface?, which: Int ->
                        action.callback.call(
                            activity
                        )
                    }
                }
                appliedTones[action.tone.ordinal] = true
            }
            if (appliedTones.size < 1 || !appliedTones[Tone.Negative.ordinal]) builder.setNegativeButton(
                R.string.butn_close,
                null
            )
        }
        return builder
    }

    override fun toNotification(task: AsyncTask): DynamicNotification? {
        val context = task.context.applicationContext
        val utils = task.notificationHelper.utils
        val notification: DynamicNotification? = utils.buildDynamicNotification(
            task.hashCode().toLong(),
            NotificationUtils.Companion.NOTIFICATION_CHANNEL_HIGH
        )
        val intent: PendingIntent = PendingIntent.getActivity(
            context, 0, Intent(context, HomeActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 0
        )
        notification.setSmallIcon(iconFor(mTone))
            .setGroup(task.taskGroup)
            .setContentTitle(mTitle)
            .setContentText(mMessage)
            .setContentIntent(intent)
            .setAutoCancel(true)
        for (action in mActionList) notification.addAction(
            iconFor(action.tone), action.name, PendingIntent.getActivity(
                context,
                0, Intent(context, HomeActivity::class.java), 0
            )
        )
        return notification
    }

    override fun toSnackbar(view: View?): Snackbar {
        val snackbar: Snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        if (sizeOfActions() > 0) {
            synchronized(mActionList) {
                val action = mActionList[0]
                snackbar.setAction(action.name, View.OnClickListener { v: View -> action.callback.call(v.context) })
            }
        }
        return snackbar
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("Title=")
            .append(title)
            .append(" Msg=")
            .append(message)
            .append(" Tone=")
            .append(tone)
        for (action in mActionList) stringBuilder.append(action)
        return stringBuilder.toString()
    }

    companion object {
        @DrawableRes
        fun iconFor(tone: Tone?): Int {
            return when (tone) {
                Tone.Confused -> R.drawable.ic_help_white_24_static
                Tone.Positive -> R.drawable.ic_check_white_24dp_static
                Tone.Negative -> R.drawable.ic_close_white_24dp_static
                Tone.Neutral -> R.drawable.ic_trebleshot_white_24dp_static
                else -> R.drawable.ic_trebleshot_white_24dp_static
            }
        }
    }
}