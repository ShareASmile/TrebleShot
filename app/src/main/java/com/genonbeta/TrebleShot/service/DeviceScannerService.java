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

package com.genonbeta.TrebleShot.service;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import com.genonbeta.TrebleShot.app.Service;
import com.genonbeta.TrebleShot.config.AppConfig;
import com.genonbeta.TrebleShot.object.DeviceAddress;
import com.genonbeta.TrebleShot.object.Device;
import com.genonbeta.TrebleShot.util.AppUtils;
import com.genonbeta.TrebleShot.util.DeviceLoader;
import com.genonbeta.TrebleShot.util.NetworkDeviceScanner;
import com.genonbeta.TrebleShot.util.NetworkUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.List;

public class DeviceScannerService extends Service implements NetworkDeviceScanner.ScannerHandler
{
    public static final String TAG = DeviceScannerService.class.getSimpleName();

    public static final String ACTION_SCAN_DEVICES = "genonbeta.intent.action.SCAN_DEVICES",
            ACTION_SCAN_STARTED = "genonbeta.intent.action.SCAN_STARTED",
            ACTION_DEVICE_SCAN_COMPLETED = "genonbeta.intent.action.DEVICE_SCAN_COMPLETED",
            EXTRA_SCAN_STATUS = "genonbeta.intent.extra.SCAN_STATUS",
            STATUS_OK = "genonbeta.intent.status.OK",
            STATUS_NO_NETWORK_INTERFACE = "genonbeta.intent.status.NO_NETWORK_INTERFACE",
            SCANNER_NOT_AVAILABLE = "genonbeta.intent.status.SCANNER_NOT_AVAILABLE";

    private NetworkDeviceScanner mDeviceScanner = new NetworkDeviceScanner();
    private IBinder mBinder = new Binder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);

        if (intent != null && AppUtils.checkRunningConditions(this))
            if (ACTION_SCAN_DEVICES.equals(intent.getAction())) {
                String result = SCANNER_NOT_AVAILABLE;

                if (!mDeviceScanner.isBusy()) {
                    List<NetworkInterface> interfaceList = NetworkUtils.getInterfaces(true,
                            AppConfig.DEFAULT_DISABLED_INTERFACES);

                    Device localDevice = AppUtils.getLocalDevice(getApplicationContext());
                    getKuick().publish(localDevice);

                    for (NetworkInterface networkInterface : interfaceList) {
                        DeviceAddress connection = new DeviceAddress(networkInterface.getDisplayName(),
                                NetworkUtils.getFirstInet4Address(networkInterface).getHostAddress(), localDevice.uid,
                                System.currentTimeMillis());
                        getKuick().publish(connection);
                    }

                    getKuick().broadcast();
                    result = mDeviceScanner.scan(interfaceList, this) ? STATUS_OK : STATUS_NO_NETWORK_INTERFACE;
                }

                getApplicationContext().sendBroadcast(new Intent(ACTION_SCAN_STARTED).putExtra(EXTRA_SCAN_STATUS,
                        result));

                return START_STICKY;
            }

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return mBinder;
    }

    @Override
    public void onDeviceFound(InetAddress address, NetworkInterface networkInterface)
    {
        DeviceAddress connection = new DeviceAddress(networkInterface.getDisplayName(), address.getHostAddress(),
                "", System.currentTimeMillis());
        getKuick().publish(connection);

        DeviceLoader.load(getKuick(), address.getHostAddress(), null);
        getKuick().broadcast();
    }

    @Override
    public void onThreadsCompleted()
    {
        Log.d(TAG, "onThreadsCompleted: Compeleted");
        getApplicationContext().sendBroadcast(new Intent(ACTION_DEVICE_SCAN_COMPLETED));
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mDeviceScanner.interrupt();
    }

    public NetworkDeviceScanner getDeviceScanner()
    {
        return mDeviceScanner;
    }

    public class Binder extends android.os.Binder
    {
        public DeviceScannerService getService()
        {
            return DeviceScannerService.this;
        }
    }
}
