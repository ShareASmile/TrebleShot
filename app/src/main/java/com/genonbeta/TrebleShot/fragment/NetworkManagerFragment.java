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

package com.genonbeta.TrebleShot.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import com.genonbeta.TrebleShot.GlideApp;
import com.genonbeta.TrebleShot.R;
import com.genonbeta.TrebleShot.config.Keyword;
import com.genonbeta.TrebleShot.service.BackgroundService;
import com.genonbeta.TrebleShot.ui.callback.IconProvider;
import com.genonbeta.TrebleShot.ui.callback.TitleProvider;
import com.genonbeta.TrebleShot.util.AppUtils;
import com.genonbeta.TrebleShot.util.ConnectionUtils;
import com.genonbeta.TrebleShot.util.InetAddresses;
import com.genonbeta.android.framework.app.Fragment;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetworkManagerFragment extends Fragment implements TitleProvider, IconProvider
{
    private final int REQUEST_LOCATION_PERMISSION = 1;

    private IntentFilter mIntentFilter = new IntentFilter();
    private StatusReceiver mStatusReceiver = new StatusReceiver();
    private ConnectionUtils mConnectionUtils;

    private View mContainerText1;
    private View mContainerText2;
    private View mContainerText3;
    private Button mActionButton;
    private TextView mText1;
    private TextView mText2;
    private TextView mText3;
    private ImageView mCodeView;
    private ColorStateList mColorPassiveState;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mConnectionUtils = new ConnectionUtils(requireContext());
        mIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(BackgroundService.ACTION_PIN_USED);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        View view = getLayoutInflater().inflate(R.layout.layout_network_manager, container, false);

        mColorPassiveState = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), AppUtils.getReference(
                requireContext(), R.attr.colorPassive)));
        mCodeView = view.findViewById(R.id.layout_network_manager_qr_image);
        mContainerText1 = view.findViewById(R.id.layout_network_manager_info_container_text1_container);
        mContainerText2 = view.findViewById(R.id.layout_network_manager_info_container_text2_container);
        mContainerText3 = view.findViewById(R.id.layout_network_manager_info_container_text3_container);
        mText1 = view.findViewById(R.id.layout_network_manager_info_container_text1);
        mText2 = view.findViewById(R.id.layout_network_manager_info_container_text2);
        mText3 = view.findViewById(R.id.layout_network_manager_info_container_text3);
        mActionButton = view.findViewById(R.id.layout_network_manager_info_toggle_button);

        mActionButton.setOnClickListener(v -> {
            if (mConnectionUtils.canReadWifiInfo())
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            else
                mConnectionUtils.validateLocationPermission(getActivity(), REQUEST_LOCATION_PERMISSION);
        });

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION)
            updateState();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        requireContext().registerReceiver(mStatusReceiver, mIntentFilter);
        updateState();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        requireContext().unregisterReceiver(mStatusReceiver);
    }

    @Override
    public int getIconRes()
    {
        return R.drawable.ic_wifi_white_24dp;
    }

    @Override
    public CharSequence getDistinctiveTitle(Context context)
    {
        return context.getString(R.string.text_useExistingNetwork);
    }

    public void updateViewsLocationDisabled()
    {
        updateViews(null, R.string.butn_enable, getString(R.string.mesg_locationPermissionRequiredAny), null, null);
    }

    public void updateViewsWithBlank()
    {
        updateViews(null, R.string.butn_wifiSettings, getString(R.string.mesg_connectToWiFiNetworkHelp), null, null);
    }

    // for connection addressing purpose
    public void updateViews(String networkName, String ipAddress, String bssid)
    {
        try {
            JSONObject object = new JSONObject()
                    .put(Keyword.NETWORK_ADDRESS_IP, ipAddress)
                    .put(Keyword.NETWORK_BSSID, bssid);

            updateViews(object, R.string.butn_wifiSettings, getString(R.string.text_easyDiscoveryHelp), networkName, ipAddress);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateViews(@Nullable JSONObject codeIndex, @StringRes int buttonText, @Nullable String text1,
                            @Nullable String text2, @Nullable String text3)
    {
        boolean showQRCode = codeIndex != null && codeIndex.length() > 0 && getContext() != null;

        try {
            if (showQRCode) {
                codeIndex.put(Keyword.NETWORK_PIN, AppUtils.generateNetworkPin(getContext()));

                MultiFormatWriter formatWriter = new MultiFormatWriter();
                BitMatrix bitMatrix = formatWriter.encode(codeIndex.toString(), BarcodeFormat.QR_CODE, 400,
                        400);
                BarcodeEncoder encoder = new BarcodeEncoder();
                Bitmap bitmap = encoder.createBitmap(bitMatrix);

                GlideApp.with(getContext())
                        .load(bitmap)
                        .into(mCodeView);
            } else
                mCodeView.setImageResource(R.drawable.ic_qrcode_white_128dp);

            ImageViewCompat.setImageTintList(mCodeView, showQRCode ? null : mColorPassiveState);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mContainerText1.setVisibility(text1 == null ? View.GONE : View.VISIBLE);
            mContainerText2.setVisibility(text2 == null ? View.GONE : View.VISIBLE);
            mContainerText3.setVisibility(text3 == null ? View.GONE : View.VISIBLE);

            mActionButton.setText(buttonText);
            mText1.setText(text1);
            mText2.setText(text2);
            mText3.setText(text3);
        }
    }

    public void updateState()
    {
        WifiInfo connectionInfo = mConnectionUtils.getWifiManager().getConnectionInfo();

        if (!mConnectionUtils.canReadWifiInfo()) {
            updateViewsLocationDisabled();
        } else if (!mConnectionUtils.isConnectedToAnyNetwork())
            updateViewsWithBlank();
        else {
            String networkName = ConnectionUtils.getCleanNetworkName(connectionInfo.getSSID());
            String hostAddress;

            try {
                hostAddress = InetAddress.getByAddress(InetAddresses.toByteArray(
                        connectionInfo.getIpAddress())).getHostAddress();
            } catch (UnknownHostException e) {
                hostAddress = "0.0.0.0";
            }

            updateViews(networkName, hostAddress, connectionInfo.getBSSID());
        }
    }

    private class StatusReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())
                    || ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())
                    || BackgroundService.ACTION_PIN_USED.equals(intent.getAction()))
                updateState();
        }
    }
}
