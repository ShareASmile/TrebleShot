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

package com.genonbeta.TrebleShot.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import com.genonbeta.TrebleShot.R;
import com.genonbeta.TrebleShot.app.Activity;
import com.genonbeta.TrebleShot.object.DeviceRoute;
import com.genonbeta.TrebleShot.service.backgroundservice.BaseAttachableBgTask;
import com.genonbeta.TrebleShot.service.backgroundservice.TaskMessage;
import com.genonbeta.TrebleShot.task.DeviceIntroductionTask;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class IpAddressConnectionActivity extends Activity implements DeviceIntroductionTask.ResultListener
{
    public static final String
            TAG = IpAddressConnectionActivity.class.getSimpleName(),
            EXTRA_DEVICE = "extraDevice",
            EXTRA_CONNECTION = "extraConnection";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ip_address_connection);

        findViewById(R.id.confirm_button).setOnClickListener((v) -> {
            AppCompatEditText editText = findViewById(R.id.editText);
            String ipAddress = editText.getText().toString();

            if (ipAddress.matches("([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3})")) {
                try {
                    runUiTask(new DeviceIntroductionTask(InetAddress.getByName(ipAddress), -1));
                } catch (UnknownHostException e) {
                    editText.setError(getString(R.string.mesg_unknownHostError));
                }
            } else
                editText.setError(getString(R.string.mesg_errorNotAnIpAddress));
        });
    }

    @Override
    protected void onAttachTasks(List<BaseAttachableBgTask> taskList)
    {
        super.onAttachTasks(taskList);

        boolean hasDeviceIntroductionTask = false;
        for (BaseAttachableBgTask task : taskList)
            if (task instanceof DeviceIntroductionTask) {
                hasDeviceIntroductionTask = true;
                ((DeviceIntroductionTask) task).setAnchor(this);
            }

        setShowProgress(hasDeviceIntroductionTask);
    }

    @Override
    public void onTaskStateChanged(BaseAttachableBgTask task)
    {
        setShowProgress(task instanceof DeviceIntroductionTask && !task.isFinished());
    }

    @Override
    public boolean onTaskMessage(TaskMessage message)
    {
        return false;
    }

    private void setShowProgress(boolean show)
    {
        findViewById(R.id.progressBar).setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDeviceReached(DeviceRoute deviceRoute)
    {
        setResult(RESULT_OK, new Intent()
                .putExtra(EXTRA_DEVICE, deviceRoute.device)
                .putExtra(EXTRA_CONNECTION, deviceRoute.connection));
        finish();
    }
}
