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

package com.genonbeta.TrebleShot.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.genonbeta.TrebleShot.R;
import com.genonbeta.TrebleShot.adapter.FileListAdapter;
import com.genonbeta.TrebleShot.app.Activity;
import com.genonbeta.TrebleShot.fragment.FileExplorerFragment;
import com.genonbeta.TrebleShot.util.FileUtils;
import com.genonbeta.android.framework.io.DocumentFile;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

/**
 * Created by: veli
 * Date: 5/29/17 3:18 PM
 */

public class FilePickerActivity extends Activity
{
    public static final String ACTION_CHOOSE_DIRECTORY = "com.genonbeta.intent.action.CHOOSE_DIRECTORY";
    public static final String ACTION_CHOOSE_FILE = "com.genonbeta.intent.action.CHOOSE_FILE";

    public static final String EXTRA_ACTIVITY_TITLE = "activityTitle";
    public static final String EXTRA_START_PATH = "startPath";
    // belongs to returned result intent
    public static final String EXTRA_CHOSEN_PATH = "chosenPath";

    private FileExplorerFragment mFileExplorerFragment;
    private FloatingActionButton mFAB;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filepicker);

        mFileExplorerFragment = (FileExplorerFragment) getSupportFragmentManager().findFragmentById(
                R.id.activity_filepicker_fragment_files);
        mFAB = findViewById(R.id.content_fab);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        if (getIntent() != null) {
            boolean hasTitlesDefined = false;

            if (getIntent() != null && getSupportActionBar() != null) {
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                if (hasTitlesDefined = getIntent().hasExtra(EXTRA_ACTIVITY_TITLE))
                    getSupportActionBar().setTitle(getIntent().getStringExtra(EXTRA_ACTIVITY_TITLE));

            }

            if (ACTION_CHOOSE_DIRECTORY.equals(getIntent().getAction())) {
                if (getSupportActionBar() != null) {
                    if (!hasTitlesDefined)
                        getSupportActionBar().setTitle(R.string.text_chooseFolder);
                    else
                        getSupportActionBar().setSubtitle(R.string.text_chooseFolder);
                }

                mFileExplorerFragment.getAdapter()
                        .setConfiguration(true, false, null);
                mFileExplorerFragment.refreshList();

                RecyclerView recyclerView = mFileExplorerFragment.getListView();
                recyclerView.setPadding(0, 0, 0, 200);
                recyclerView.setClipToPadding(false);

                mFAB.show();
                mFAB.setOnClickListener(v -> {
                    DocumentFile selectedPath = mFileExplorerFragment.getAdapter().getPath();

                    if (selectedPath != null && selectedPath.canWrite())
                        finishWithResult(selectedPath);
                    else
                        Snackbar.make(v, R.string.mesg_currentPathUnavailable, Snackbar.LENGTH_SHORT).show();
                });
            } else if (ACTION_CHOOSE_FILE.equals(getIntent().getAction())) {
                if (getSupportActionBar() != null) {
                    if (!hasTitlesDefined)
                        getSupportActionBar().setTitle(R.string.text_chooseFile);
                    else
                        getSupportActionBar().setSubtitle(R.string.text_chooseFolder);
                }

                mFileExplorerFragment.setLayoutClickListener((listFragment, holder, longClick) -> {
                    if (longClick)
                        return false;
                    FileListAdapter.FileHolder fileHolder = mFileExplorerFragment.getAdapter().getItem(holder);

                    if (fileHolder.file.isFile()) {
                        finishWithResult(fileHolder.file);
                        return true;
                    }
                    return false;
                });
            } else
                finish();

            if (!isFinishing())
                if (getIntent().hasExtra(EXTRA_START_PATH)) {
                    try {
                        mFileExplorerFragment.goPath(FileUtils.fromUri(this,
                                Uri.parse(getIntent().getStringExtra(EXTRA_START_PATH))));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        } else
            finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == android.R.id.home)
            finish();
        else
            return super.onOptionsItemSelected(item);

        return true;
    }

    @Override
    public void onBackPressed()
    {
        if (mFileExplorerFragment == null || !mFileExplorerFragment.onBackPressed())
            super.onBackPressed();
    }

    private void finishWithResult(DocumentFile file)
    {
        setResult(Activity.RESULT_OK, new Intent(ACTION_CHOOSE_DIRECTORY)
                .putExtra(EXTRA_CHOSEN_PATH, file.getUri()));
        finish();
    }
}
