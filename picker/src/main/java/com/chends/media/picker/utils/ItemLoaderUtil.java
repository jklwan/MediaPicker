package com.chends.media.picker.utils;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;

import com.chends.media.picker.listener.ItemLoaderCallback;
import com.chends.media.picker.loader.ItemLoader;
import com.chends.media.picker.model.Constant;

/**
 * @author chends create on 2019/9/5.
 */
public class ItemLoaderUtil implements LoaderManager.LoaderCallbacks<Cursor> {
    private final int LOADER_ID = 2;
    private final String BUNDLE_ID = "bundle_id";
    private Context context;
    private LoaderManager manager;
    private ItemLoaderCallback mCallback;

    public ItemLoaderUtil(AppCompatActivity activity, ItemLoaderCallback mCallback) {
        this.context = activity;
        this.mCallback = mCallback;
        manager = activity.getSupportLoaderManager();
    }

    public void startLoader(String id) {
        Bundle args = new Bundle();
        args.putString(BUNDLE_ID, id);
        manager.restartLoader(LOADER_ID, null, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String folderId = Constant.Folder_Id_All;
        if (args != null) {
            folderId = args.getString(BUNDLE_ID, folderId);
        }
        return ItemLoader.create(context, folderId);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (mCallback != null) {
            mCallback.onItemLoaderFinish(cursor);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if (mCallback != null) {
            mCallback.onItemLoaderReset();
        }
    }

    public void onDestroy() {
        manager.destroyLoader(LOADER_ID);
        mCallback = null;
    }
}
