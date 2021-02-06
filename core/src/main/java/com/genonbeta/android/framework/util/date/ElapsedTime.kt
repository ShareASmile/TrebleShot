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
package com.genonbeta.android.framework.util.date

import androidx.test.runner.AndroidJUnit4
import android.content.ContentResolver
import kotlin.Throws
import com.genonbeta.android.framework.io.StreamInfo.FolderStateException
import android.provider.OpenableColumns
import com.genonbeta.android.framework.io.StreamInfo
import com.genonbeta.android.framework.io.LocalDocumentFile
import com.genonbeta.android.framework.io.StreamDocumentFile
import androidx.annotation.RequiresApi
import android.provider.DocumentsContract
import android.content.Intent
import android.content.pm.PackageManager
import android.webkit.MimeTypeMap
import com.google.android.material.snackbar.Snackbar
import com.genonbeta.android.framework.util.actionperformer.PerformerCallback
import com.genonbeta.android.framework.util.actionperformer.PerformerListener
import android.view.MenuInflater
import com.genonbeta.android.framework.util.actionperformer.IPerformerEngine
import com.genonbeta.android.framework.util.actionperformer.IBaseEngineConnection
import com.genonbeta.android.framework.``object`

/**
 * created by: Veli
 * date: 6.02.2018 12:27
 */
class ElapsedTime(time: Long) {
    private var mElapsedTime: Long = 0
    private var mYears: Long = 0
    private var mMonths: Long = 0
    private var mDays: Long = 0
    private var mHours: Long = 0
    private var mMinutes: Long = 0
    private var mSeconds: Long = 0
    fun getElapsedTime(): Long {
        return mElapsedTime
    }

    fun getDays(): Long {
        return mDays
    }

    fun getHours(): Long {
        return mHours
    }

    fun getMinutes(): Long {
        return mMinutes
    }

    fun getMonths(): Long {
        return mMonths
    }

    fun getSeconds(): Long {
        return mSeconds
    }

    fun getYears(): Long {
        return mYears
    }

    fun set(time: Long) {
        mElapsedTime = time
        val calculator = ElapsedTimeCalculator(time / 1000)
        mYears = calculator.crop(62208000)
        mMonths = calculator.crop(2592000)
        mDays = calculator.crop(86400)
        mHours = calculator.crop(3600)
        mMinutes = calculator.crop(60)
        mSeconds = calculator.getLeftTime()
    }

    class ElapsedTimeCalculator(private var mTime: Long) {
        fun crop(summonBy: Long): Long {
            var result: Long = 0
            if (getLeftTime() > summonBy) {
                result = getLeftTime() / summonBy
                setTime(getLeftTime() - result * summonBy)
            }
            return result
        }

        fun getLeftTime(): Long {
            return mTime
        }

        fun setTime(time: Long) {
            mTime = time
        }
    }

    init {
        set(time)
    }
}