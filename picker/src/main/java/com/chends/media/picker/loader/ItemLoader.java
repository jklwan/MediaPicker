package com.chends.media.picker.loader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.chends.media.picker.utils.SelectUtil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.CursorLoader;

/**
 * 文件夹的文件loader<br>
 * @author chends create on 2019/9/2.
 */
public class ItemLoader extends CursorLoader {

    private ItemLoader(@NonNull Context context, @NonNull Uri uri, @Nullable String[] projection,
                       @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        super(context, uri, projection, selection, selectionArgs, sortOrder);
    }

    /**
     * 构建 ItemLoader
     */
    public static ItemLoader create(Context context, String folderId){
        return new ItemLoader(context, SelectUtil.getItemUri(folderId), SelectUtil.getItemProjection(folderId),
                SelectUtil.getItemSelection(folderId), null, SelectUtil.SORT_ORDER);
    }

    /**
     * 首次搜索已选择的文件
     * @param context context
     * @return loader
     */
    public static ItemLoader search(Context context){
        return new ItemLoader(context, SelectUtil.getFolderUri(), SelectUtil.getItemSearchProjection(),
                SelectUtil.getSearchSelection(), null, SelectUtil.SORT_ORDER);
    }

    @Override
    public Cursor loadInBackground() {
        return super.loadInBackground();
    }
}