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
package org.monora.uprotocol.client.android.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.genonbeta.android.framework.ui.callback.SnackbarPlacementProvider
import com.google.android.material.snackbar.Snackbar
import org.monora.uprotocol.client.android.R
import org.monora.uprotocol.client.android.app.Activity
import org.monora.uprotocol.client.android.database.model.UClient
import org.monora.uprotocol.client.android.database.model.UClientAddress
import org.monora.uprotocol.client.android.fragment.ClientsFragment
import org.monora.uprotocol.client.android.fragment.NetworkManagerFragment
import org.monora.uprotocol.client.android.receiver.BgBroadcastReceiver
import org.monora.uprotocol.client.android.service.BackgroundService
import org.monora.uprotocol.core.protocol.ClientType

class AddDeviceActivity : Activity(), SnackbarPlacementProvider {
    private val filter = IntentFilter()

    private lateinit var networkManagerFragment: NetworkManagerFragment

    private lateinit var clientsFragment: ClientsFragment

    private lateinit var optionsFragment: OptionsFragment

    private val toolbar: Toolbar by lazy {
        findViewById(R.id.toolbar)
    }

    private val progressBar: ProgressBar by lazy {
        findViewById(R.id.activity_connection_establishing_progress_bar)
    }

    private val connectionMode by lazy {
        intent?.getSerializableExtra(EXTRA_CONNECTION_MODE) ?: ConnectionMode.Return
    }

    private val selfReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_CHANGE_FRAGMENT == intent.action && intent.hasExtra(EXTRA_FRAGMENT_ENUM)) {
                val fragmentEnum = intent.getSerializableExtra(EXTRA_FRAGMENT_ENUM) as AvailableFragment?
                setFragment(fragmentEnum)
            } else if (BgBroadcastReceiver.ACTION_DEVICE_ACQUAINTANCE == intent.action) {
                val device: UClient? = intent.getParcelableExtra(BgBroadcastReceiver.EXTRA_DEVICE)
                val address: UClientAddress? = intent.getParcelableExtra(BgBroadcastReceiver.EXTRA_DEVICE_ADDRESS)
                if (device != null && address != null) handleResult(device, address)
            } else if (BgBroadcastReceiver.ACTION_INCOMING_TRANSFER_READY == intent.action
                && intent.hasExtra(BgBroadcastReceiver.EXTRA_TRANSFER)
            ) {
                TransferDetailActivity.startInstance(
                    this@AddDeviceActivity,
                    intent.getParcelableExtra(BgBroadcastReceiver.EXTRA_TRANSFER)
                )
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setResult(RESULT_CANCELED)
        setContentView(R.layout.activity_add_device)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val deviceListArgs = Bundle()
        deviceListArgs.putStringArrayList(
            ClientsFragment.ARG_HIDDEN_DEVICES_LIST,
            arrayListOf(ClientType.Web.toString())
        )
        val factory = supportFragmentManager.fragmentFactory
        optionsFragment = factory.instantiate(classLoader, OptionsFragment::class.java.name) as OptionsFragment
        networkManagerFragment = factory.instantiate(
            classLoader, NetworkManagerFragment::class.java.name
        ) as NetworkManagerFragment
        clientsFragment = factory.instantiate(classLoader, ClientsFragment::class.java.name) as ClientsFragment
        clientsFragment.arguments = deviceListArgs
        filter.addAction(ACTION_CHANGE_FRAGMENT)
        filter.addAction(BgBroadcastReceiver.ACTION_DEVICE_ACQUAINTANCE)
        filter.addAction(BgBroadcastReceiver.ACTION_INCOMING_TRANSFER_READY)
    }

    override fun onResume() {
        super.onResume()
        checkFragment()
        registerReceiver(selfReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(selfReceiver)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            when (requestCode) {
                REQUEST_BARCODE_SCAN -> handleResult(
                    data.getParcelableExtra(BarcodeScannerActivity.EXTRA_DEVICE),
                    data.getParcelableExtra(BarcodeScannerActivity.EXTRA_DEVICE_ADDRESS)
                )
                REQUEST_IP_DISCOVERY -> handleResult(
                    data.getParcelableExtra(ManualConnectionActivity.EXTRA_CLIENT),
                    data.getParcelableExtra(ManualConnectionActivity.EXTRA_CLIENT_ADDRESS)
                )
            }
        }
    }

    override fun onBackPressed() {
        if (getShowingFragment() is OptionsFragment) {
            super.onBackPressed()
        } else {
            setFragment(AvailableFragment.Options)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        } else {
            return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun applyViewChanges(fragment: Fragment) {
        if (supportActionBar != null) {
            val titleRes = when (fragment) {
                is NetworkManagerFragment -> R.string.butn_generateQrCode
                else -> R.string.text_chooseDevice
            }

            toolbar.title = getString(titleRes)
        }
    }

    private fun checkFragment() {
        val currentFragment = getShowingFragment()
        if (currentFragment == null) setFragment(AvailableFragment.Options) else applyViewChanges(currentFragment)
    }

    override fun createSnackbar(resId: Int, vararg objects: Any?): Snackbar {
        return Snackbar.make(
            findViewById(R.id.activity_connection_establishing_content_view),
            getString(resId, *objects), Snackbar.LENGTH_LONG
        )
    }

    fun getShowingFragmentId(): AvailableFragment {
        val fragment = getShowingFragment()
        if (fragment is NetworkManagerFragment)
            return AvailableFragment.GenerateQrCode
        else if (fragment is ClientsFragment)
            return AvailableFragment.AllDevices

        // Probably OptionsFragment
        return AvailableFragment.Options
    }

    private fun getShowingFragment(): Fragment? {
        return supportFragmentManager.findFragmentById(R.id.activity_connection_establishing_content_view)
    }

    private fun handleResult(client: UClient?, address: UClientAddress?) {
        if (client == null || address == null) return

        if (ConnectionMode.Return == connectionMode) {
            returnResult(this, client, address)
        } else if (ConnectionMode.WaitForRequests == connectionMode) {
            createSnackbar(R.string.mesg_completing).show()
        }
    }

    fun setFragment(fragment: AvailableFragment?) {
        val activeFragment = getShowingFragment()
        val fragmentCandidate = when (fragment) {
            AvailableFragment.EnterAddress -> {
                startManualConnectionActivity()
                return
            }
            AvailableFragment.ScanQrCode -> {
                startCodeScanner()
                return
            }
            AvailableFragment.GenerateQrCode -> networkManagerFragment
            AvailableFragment.AllDevices -> clientsFragment
            AvailableFragment.Options -> optionsFragment
            else -> optionsFragment
        }
        if (activeFragment == null || fragmentCandidate !== activeFragment) {
            val transaction = supportFragmentManager.beginTransaction()
            if (activeFragment != null) transaction.remove(activeFragment)
            if (activeFragment != null && fragmentCandidate is OptionsFragment) transaction.setCustomAnimations(
                R.anim.enter_from_left,
                R.anim.exit_to_right
            ) else transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left)
            transaction.add(R.id.activity_connection_establishing_content_view, fragmentCandidate)
            transaction.commit()
            applyViewChanges(fragmentCandidate)
        }
    }

    private fun startCodeScanner() {
        startActivityForResult(Intent(this, BarcodeScannerActivity::class.java), REQUEST_BARCODE_SCAN)
    }

    protected fun startManualConnectionActivity() {
        startActivityForResult(Intent(this, ManualConnectionActivity::class.java), REQUEST_IP_DISCOVERY)
    }

    enum class AvailableFragment {
        Options, GenerateQrCode, AllDevices, ScanQrCode, CreateHotspot, EnterAddress
    }

    class OptionsFragment : Fragment(R.layout.layout_connection_options_fragment) {
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val listener = View.OnClickListener { v: View ->
                updateFragment(
                    when (v.id) {
                        R.id.connection_option_devices -> AvailableFragment.AllDevices
                        R.id.connection_option_generate_qr_code -> AvailableFragment.GenerateQrCode
                        R.id.connection_option_manual_ip -> AvailableFragment.EnterAddress
                        R.id.connection_option_scan -> AvailableFragment.ScanQrCode
                        else -> AvailableFragment.Options
                    }
                )
            }
            view.findViewById<View>(R.id.connection_option_devices).setOnClickListener(listener)
            view.findViewById<View>(R.id.connection_option_generate_qr_code).setOnClickListener(listener)
            view.findViewById<View>(R.id.connection_option_scan).setOnClickListener(listener)
            view.findViewById<View>(R.id.connection_option_manual_ip).setOnClickListener(listener)
        }

        private fun updateFragment(fragment: AvailableFragment) {
            context?.sendBroadcast(Intent(ACTION_CHANGE_FRAGMENT).putExtra(EXTRA_FRAGMENT_ENUM, fragment))
        }
    }

    enum class ConnectionMode {
        WaitForRequests, Return
    }

    companion object {
        const val ACTION_CHANGE_FRAGMENT = "com.genonbeta.intent.action.CHANGE_FRAGMENT"

        const val EXTRA_FRAGMENT_ENUM = "extraFragmentEnum"

        const val EXTRA_DEVICE = "extraDevice"

        const val EXTRA_DEVICE_ADDRESS = "extraDeviceAddress"

        const val EXTRA_CONNECTION_MODE = "extraConnectionMode"

        const val REQUEST_BARCODE_SCAN = 100

        const val REQUEST_IP_DISCOVERY = 110

        fun returnResult(activity: android.app.Activity, client: UClient?, address: UClientAddress?) {
            activity.setResult(
                RESULT_OK, Intent()
                    .putExtra(EXTRA_DEVICE, client)
                    .putExtra(EXTRA_DEVICE_ADDRESS, address)
            )
            activity.finish()
        }
    }
}