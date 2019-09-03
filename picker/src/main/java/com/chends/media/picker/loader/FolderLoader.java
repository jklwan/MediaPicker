package com.chends.media.picker.loader;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;

/**
 * 文件夹Loader
 * @author chends create on 2019/9/2.
 */
public class FolderLoader extends CursorLoader {
    public FolderLoader(@NonNull Context context) {
        super(context);
    }

    public FolderLoader(@NonNull Context context, @NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        super(context, uri, projection, selection, selectionArgs, sortOrder);
    }
}