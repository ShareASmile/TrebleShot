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

import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.genonbeta.android.framework.util.actionperformer.IPerformerEngine
import com.genonbeta.android.framework.util.actionperformer.PerformerEngine
import com.genonbeta.android.framework.util.actionperformer.PerformerEngineProvider
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import org.monora.uprotocol.client.android.R
import org.monora.uprotocol.client.android.adapter.MainFragmentStateAdapter
import org.monora.uprotocol.client.android.adapter.MainFragmentStateAdapter.PageItem
import org.monora.uprotocol.client.android.app.Activity
import org.monora.uprotocol.client.android.app.ListingFragment
import org.monora.uprotocol.client.android.app.ListingFragmentBase
import org.monora.uprotocol.client.android.fragment.SharedTextFragment
import org.monora.uprotocol.client.android.fragment.content.FileFragment
import org.monora.uprotocol.client.android.util.Selections

/**
 * created by: veli
 * date: 13/04/18 19:45
 */
@AndroidEntryPoint
class ContentSharingActivity : Activity(), PerformerEngineProvider {
    private var backPressedListener: OnBackPressedListener? = null

    private val performerEngine = PerformerEngine()

    private lateinit var progressBar: ProgressBar

    private lateinit var cardView: ViewGroup

    private lateinit var actionMenuView: ActionMenuView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content_sharing)
        actionMenuView = findViewById(R.id.menu_view)
        cardView = findViewById(R.id.activity_content_sharing_cardview)
        progressBar = findViewById(R.id.activity_content_sharing_progress_bar)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val tabLayout: TabLayout = findViewById(R.id.activity_content_sharing_tab_layout)
        val viewPager: ViewPager2 = findViewById(R.id.activity_content_sharing_view_pager)
        val pagerAdapter: MainFragmentStateAdapter = object : MainFragmentStateAdapter(
            this, supportFragmentManager, lifecycle
        ) {
            override fun onItemInstantiated(item: PageItem) {
                val fragment: Fragment? = item.fragment
                if (fragment is ListingFragmentBase<*>) {
                    if (viewPager.currentItem == item.currentPosition) {

                    }
                }
            }
        }
        val arguments = Bundle()
        arguments.putBoolean(ListingFragment.ARG_SELECT_BY_CLICK, true)
        arguments.putBoolean(ListingFragment.ARG_HAS_BOTTOM_SPACE, false)
        // FIXME: 2/21/21 Sharing fragments were here
        pagerAdapter.add(
            PageItem(
                0,
                R.drawable.ic_short_text_white_24dp,
                getString(R.string.text_sharedTexts),
                SharedTextFragment::class.qualifiedName!!,
                arguments
            )
        )
        pagerAdapter.add(
            PageItem(
                1,
                R.drawable.ic_short_text_white_24dp,
                getString(R.string.text_files),
                FileFragment::class.qualifiedName!!,
                arguments
            )
        )
        /*pagerAdapter.add(StableItem(0, ApplicationListFragment::class.qualifiedName!!, arguments))
        pagerAdapter.add(
            StableItem(1, FileExplorerFragment::class.qualifiedName!!, arguments, getString(R.string.text_files))
        )
        pagerAdapter.add(StableItem(2, AudioListFragment::class.qualifiedName!!, arguments))
        pagerAdapter.add(StableItem(3, ImageListFragment::class.qualifiedName!!, arguments))
        pagerAdapter.add(StableItem(4, VideoListFragment::class.qualifiedName!!, arguments))*/
        pagerAdapter.createTabs(tabLayout, withIcon = false, withText = true)
        viewPager.adapter = pagerAdapter

        tabLayout.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    tab?.let {
                        viewPager.setCurrentItem(tab.position, true)
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {

                }

                override fun onTabReselected(tab: TabLayout.Tab?) {

                }
            }
        )

        viewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    tabLayout.getTabAt(position)?.select()
                    val fragment = pagerAdapter.getItem(position).fragment
                    if (fragment is ListingFragmentBase<*>) {
                        val editableListFragment = fragment as ListingFragmentBase<*>
                        Handler(Looper.getMainLooper()).postDelayed(
                            { editableListFragment.adapterImpl.syncAllAndNotify() }, 200
                        )
                    }
                }
            }
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (canExit()) finish()
        } else {
            return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onBackPressed() {
        if (backPressedListener?.onBackPressed() != true && canExit()) {
            super.onBackPressed()
        }
    }

    private fun canExit(): Boolean {
        if (Selections.getTotalSize(performerEngine) > 0) {
            AlertDialog.Builder(this)
                .setMessage(R.string.ques_cancelSelection)
                .setNegativeButton(R.string.butn_no, null)
                .setPositiveButton(R.string.butn_yes) { dialog: DialogInterface?, which: Int -> finish() }
                .show()
            return false
        }
        return true
    }

    override fun getPerformerEngine(): IPerformerEngine {
        return performerEngine
    }
}