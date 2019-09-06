package com.chends.media.picker.utils;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;

/**
 * @author chends create on 2019/9/5.
 */
public class FolderLoaderUtil implements LoaderManager.LoaderCallbacks<Cursor> {
    private final int id = 1;
    private Context context;
    private LoaderManager manager;

    public FolderLoaderUtil() {
    }

    public void onCreate(AppCompatActivity activity){
        context = activity;
        manager = activity.getSupportLoaderManager();
        manager.initLoader(id, null, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}
