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
package com.genonbeta.android.framework.ui

import android.content.Context
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.view.SupportMenuInflater
import com.genonbeta.android.framework.ui.PerformerMenu.Callback
import com.genonbeta.android.framework.util.actionperformer.*

/**
 * The idea here is that this class bridges one or more menus with a [IEngineConnection] to perform a specific
 * task whenever a new item is added or removed and whenever the any item on a menu is clicked.
 *
 * The class that is responsible for the performer menu should also provide the [IPerformerEngine]
 * to which this will add callbacks and listeners.
 *
 * Because [SelectionModel] is referred to as the base class, the [Callback] methods shouldn't be used to
 * identify the derivatives. Instead, you should use the engine connection to identify the objects.
 */
class PerformerMenu(val context: Context, val callback: Callback) : PerformerCallback, PerformerListener,
    MenuItem.OnMenuItemClickListener {
    val menuInflater = SupportMenuInflater(context)

    fun invokeMenuItemSelected(menuItem: MenuItem): Boolean {
        return callback.onPerformerMenuSelected(this, menuItem)
    }

    /**
     * Load the given menu by calling [Callback.onPerformerMenuList].
     *
     * @param targetMenu To populate.
     * @return True when the given menu is populated.
     */
    fun load(targetMenu: Menu): Boolean {
        if (!populateMenu(targetMenu))
            return false

        for (i in 0 until targetMenu.size())
            targetMenu.getItem(i).setOnMenuItemClickListener(this)

        return true
    }

    /**
     * This is a call similar to [android.app.Activity.onCreateOptionsMenu]. This creates the menu list
     * which will be provided by [Callback.onPerformerMenuList]. If you
     * are not willing to make the [.invokeMenuItemSelected] calls manually, use
     * [load] so that menu item selection calls will be handled directly by the [Callback].
     *
     * The main difference is that when you want to work with more than one [IEngineConnection], the best is to
     * avoid using this, because you will often will not able to treat each [IEngineConnection] individually.
     * However, for example, if you are using a fragment and want to bridge default fragment callbacks like
     * [androidx.fragment.app.Fragment.onOptionsItemSelected] with this, it is best to use this so
     * that you can trigger menu creation as needed. To give an example again, you may want to keep a boolean variable
     * that goes 'selectionActivated' which will be used to assess whether the menu items will represent the selection.
     * And to reset the menus you can use [Activity.invalidateOptionsMenu] method.
     *
     * @param targetMenu To be populated.
     */
    fun populateMenu(targetMenu: Menu): Boolean {
        return callback.onPerformerMenuList(this, menuInflater, targetMenu)
    }

    /**
     * Register the callbacks of this instance, so that any change will be reported to us.
     *
     * @param engine that we are going to be informed about
     */
    fun setUp(engine: IPerformerEngine) {
        engine.addPerformerListener(this)
        engine.addPerformerCallback(this)
    }

    /**
     * Unregister the previously registered callbacks of this instance.
     *
     * @param engine To no longer be informed about.
     */
    fun dismantle(engine: IPerformerEngine) {
        engine.removePerformerCallback(this)
        engine.removePerformerListener(this)
    }

    override fun onSelection(
        engine: IPerformerEngine, owner: IBaseEngineConnection, selectionModel: SelectionModel,
        isSelected: Boolean, position: Int,
    ): Boolean {
        return callback.onPerformerMenuItemSelection(this, engine, owner, selectionModel, isSelected, position)
    }

    override fun onSelection(
        engine: IPerformerEngine, owner: IBaseEngineConnection,
        selectionModelList: MutableList<out SelectionModel>, isSelected: Boolean, positions: IntArray,
    ): Boolean {
        return callback.onPerformerMenuItemSelection(
            this, engine, owner, selectionModelList, isSelected, positions
        )
    }

    override fun onSelected(
        engine: IPerformerEngine, owner: IBaseEngineConnection, selectionModel: SelectionModel,
        isSelected: Boolean, position: Int,
    ) {
        callback.onPerformerMenuItemSelected(this, engine, owner, selectionModel, isSelected, position)
    }

    override fun onSelected(
        engine: IPerformerEngine, owner: IBaseEngineConnection,
        selectionModelList: MutableList<out SelectionModel>, isSelected: Boolean, positions: IntArray,
    ) {
        callback.onPerformerMenuItemSelected(this, engine, owner, selectionModelList, isSelected, positions)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        return callback.onPerformerMenuSelected(this, item)
    }

    /**
     * The callback to connect the menu actions to. The data will be redirected from the other callbacks.
     */
    interface Callback {
        /**
         * Called when [PerformerMenu.load] is invoked to populate the menu.
         *
         * @param performerMenu That redirects the call.
         * @param inflater      To inflate the menus with.
         * @param targetMenu    To populate.
         * @return True when there was not problem populating the menu.
         */
        fun onPerformerMenuList(performerMenu: PerformerMenu, inflater: MenuInflater, targetMenu: Menu): Boolean

        /**
         * Called when a menu item on a populated menu (with callbacks registered) was clicked.
         *
         * @param performerMenu That redirects the call.
         * @param item          That was clicked.
         * @return True when the input is known and the descendant is not needed the perform any other action.
         */
        fun onPerformerMenuSelected(performerMenu: PerformerMenu, item: MenuItem): Boolean

        /**
         * Called when a [SelectionModel] is being altered. This is called during the process which is not still
         * finished.
         *
         * @param performerMenu Instance that redirects the call.
         * @param engine        Owning the [IBaseEngineConnection].
         * @param owner         That is managing the selection list and informing the [IPerformerEngine].
         * @param selectionModel    Being altered.
         * @param isSelected    True when selected.
         * @param position      Of model on [SelectionModelProvider].
         * @return True if there is no problem with altering the state of selection of the item.
         */
        fun onPerformerMenuItemSelection(
            performerMenu: PerformerMenu, engine: IPerformerEngine, owner: IBaseEngineConnection,
            selectionModel: SelectionModel, isSelected: Boolean, position: Int,
        ): Boolean

        /**
         * Called when a [SelectionModel] is being altered. This is called during the process which is not still
         * finished.
         *
         * @param performerMenu  That redirects the call.
         * @param engine         Owning the [IBaseEngineConnection].
         * @param owner          Managing the selection list and informing the [IPerformerEngine].
         * @param selectionModelList To alter.
         * @param isSelected     True if selecting.
         * @param positions      Of the items on [SelectionModelProvider].
         * @return True if there is no problem with altering the state of selection of the item.
         */
        fun onPerformerMenuItemSelection(
            performerMenu: PerformerMenu, engine: IPerformerEngine, owner: IBaseEngineConnection,
            selectionModelList: MutableList<out SelectionModel>, isSelected: Boolean, positions: IntArray,
        ): Boolean

        /**
         * Called after the [onPerformerMenuItemSelection] to inform about the new state of the model.
         *
         * @param performerMenu That redirects the call.
         * @param engine        Owning the [IBaseEngineConnection].
         * @param owner         That is managing the selection list and informing the [IPerformerEngine].
         * @param selectionModel To alter.
         * @param isSelected    True if selecting.
         * @param position      Of the item on [SelectionModelProvider].
         */
        fun onPerformerMenuItemSelected(
            performerMenu: PerformerMenu, engine: IPerformerEngine, owner: IBaseEngineConnection,
            selectionModel: SelectionModel, isSelected: Boolean, position: Int,
        )

        /**
         * Called after the [onPerformerMenuItemSelection] to inform about the new state of the list of items.
         *
         * @param performerMenu  That redirects the call.
         * @param engine         Owning the [IBaseEngineConnection].
         * @param owner          That is managing the selection list and informing the [IPerformerEngine].
         * @param selectionModelList To alter.
         * @param isSelected     True if selecting.
         * @param positions      Of the items on [SelectionModelProvider].
         */
        fun onPerformerMenuItemSelected(
            performerMenu: PerformerMenu, engine: IPerformerEngine,
            owner: IBaseEngineConnection, selectionModelList: MutableList<out SelectionModel>,
            isSelected: Boolean, positions: IntArray,
        )
    }
}