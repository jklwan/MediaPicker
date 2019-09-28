package com.chends.media.picker.utils;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import com.chends.media.picker.listener.ItemLoaderCallback;
import com.chends.media.picker.loader.ItemLoader;
import com.chends.media.picker.model.Constant;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

/**
 * @author chends create on 2019/9/5.
 */
public class ItemLoaderUtil implements LoaderManager.LoaderCallbacks<Cursor> {
    private final int LOADER_ID = 2;
    private final int LOADER_SEARCH_ID = 3; // 查询
    private final String BUNDLE_ID = "bundle_id";
    private Context context;
    private LoaderManager manager;
    private ItemLoaderCallback mCallback;
    private boolean isSearch = false;

    public ItemLoaderUtil(AppCompatActivity activity, ItemLoaderCallback mCallback) {
        this.context = activity;
        this.mCallback = mCallback;
        manager = activity.getSupportLoaderManager();
    }

    /**
     * loader
     * @param id folder id
     */
    public void startLoader(String id) {
        isSearch = false;
        Bundle args = new Bundle();
        args.putString(BUNDLE_ID, id);
        manager.restartLoader(LOADER_ID, args, this);
    }

    public void search() {
        isSearch = true;
        manager.restartLoader(LOADER_SEARCH_ID, null, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        if (id == LOADER_SEARCH_ID){
            return ItemLoader.search(context);
        } else {
            String folderId = Constant.Folder_Id_All;
            if (args != null) {
                folderId = args.getString(BUNDLE_ID, folderId);
            }
            return ItemLoader.create(context, folderId);
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (mCallback != null) {
            mCallback.onItemLoaderFinish(cursor, isSearch);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if (mCallback != null) {
            mCallback.onItemLoaderReset(isSearch);
        }
    }

    public void onDestroy() {
        manager.destroyLoader(LOADER_ID);
        mCallback = null;
    }
}
