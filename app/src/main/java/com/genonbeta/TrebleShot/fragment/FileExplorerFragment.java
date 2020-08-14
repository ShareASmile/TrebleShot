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

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.genonbeta.TrebleShot.R;
import com.genonbeta.TrebleShot.adapter.PathResolverRecyclerAdapter;
import com.genonbeta.TrebleShot.app.Activity;
import com.genonbeta.TrebleShot.dialog.FolderCreationDialog;
import com.genonbeta.TrebleShot.ui.callback.IconProvider;
import com.genonbeta.android.framework.io.DocumentFile;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by: veli
 * Date: 5/30/17 10:47 AM
 */

public class FileExplorerFragment extends FileListFragment implements Activity.OnBackPressedListener, IconProvider
{
    public static final String TAG = FileExplorerFragment.class.getSimpleName();

    private RecyclerView mPathView;
    private FilePathResolverRecyclerAdapter mPathAdapter;
    private DocumentFile mRequestedPath = null;

    public static DocumentFile getReadableFolder(DocumentFile documentFile)
    {
        DocumentFile parent = documentFile.getParentFile();

        if (parent == null)
            return null;

        return parent.canRead() ? parent : getReadableFolder(parent);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setLayoutResId(R.layout.layout_file_explorer);
        setDividerView(R.id.fragment_fileexplorer_separator);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        mPathView = view.findViewById(R.id.fragment_fileexplorer_pathresolver);
        mPathAdapter = new FilePathResolverRecyclerAdapter(getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL,
                false);

        layoutManager.setStackFromEnd(true);
        mPathView.setLayoutManager(layoutManager);
        mPathView.setHasFixedSize(true);
        mPathView.setAdapter(mPathAdapter);
        mPathAdapter.setOnClickListener(holder -> goPath(holder.index.object));

        if (mRequestedPath != null)
            requestPath(mRequestedPath);
    }

    @Override
    public boolean onBackPressed()
    {
        DocumentFile path = getAdapter().getPath();

        if (path == null)
            return false;

        DocumentFile parentFile = getReadableFolder(path);

        if (parentFile == null || File.separator.equals(parentFile.getName()))
            goPath(null);
        else
            goPath(parentFile);

        return true;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.actions_file_explorer, menu);
    }

    @Override
    protected void onListRefreshed()
    {
        super.onListRefreshed();

        mPathAdapter.goTo(getAdapter().getPath());
        mPathAdapter.notifyDataSetChanged();

        if (mPathAdapter.getItemCount() > 0)
            mPathView.smoothScrollToPosition(mPathAdapter.getItemCount() - 1);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.actions_file_explorer_create_folder) {
            if (getAdapter().getPath() != null && getAdapter().getPath().canWrite())
                new FolderCreationDialog(getContext(), getAdapter().getPath(), directoryFile -> refreshList()).show();
            else
                Snackbar.make(getListView(), R.string.mesg_currentPathUnavailable, Snackbar.LENGTH_SHORT).show();
        } else
            return super.onOptionsItemSelected(item);

        return true;
    }

    @Override
    public int getIconRes()
    {
        return R.drawable.ic_folder_white_24dp;
    }

    public RecyclerView getPathView()
    {
        return mPathView;
    }

    @Override
    public CharSequence getDistinctiveTitle(Context context)
    {
        return context.getString(R.string.text_fileExplorer);
    }

    public void requestPath(DocumentFile file)
    {
        if (!isAdded()) {
            mRequestedPath = file;
            return;
        }

        mRequestedPath = null;

        goPath(file);
    }

    private class FilePathResolverRecyclerAdapter extends PathResolverRecyclerAdapter<DocumentFile>
    {
        public FilePathResolverRecyclerAdapter(Context context)
        {
            super(context);
        }

        @Override
        public Index<DocumentFile> onFirstItem()
        {
            return new Index<>(getContext().getString(R.string.text_home), R.drawable.ic_home_white_24dp, null);
        }

        public void goTo(DocumentFile file)
        {
            ArrayList<Index<DocumentFile>> pathIndex = new ArrayList<>();
            DocumentFile currentFile = file;

            while (currentFile != null) {
                Index<DocumentFile> index = new Index<>(currentFile.getName(), currentFile);
                pathIndex.add(index);
                currentFile = currentFile.getParentFile();

                if (currentFile == null && ".".equals(index.title))
                    index.title = getString(R.string.text_fileRoot);
            }

            initAdapter();

            synchronized (getList()) {
                while (pathIndex.size() != 0) {
                    int currentStage = pathIndex.size() - 1;
                    getList().add(pathIndex.get(currentStage));
                    pathIndex.remove(currentStage);
                }
            }
        }
    }
}
