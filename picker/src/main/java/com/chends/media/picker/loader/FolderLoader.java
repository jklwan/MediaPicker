package com.chends.media.picker.loader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;

import com.chends.media.picker.utils.SelectUtil;

/**
 * 文件夹Loader<br/>
 * <ol>
 * <li>只有图片的情况<br>
 * <table BORDER="1">
 * <tr>所有图片</tr>
 * <tr>文件夹</tr>
 * <tr>……</tr></table>
 * </li>
 * <li>只有音频/视频的情况<br>
 * <table BORDER="1">
 * <tr>所有音频/视频</tr></table>
 * </li>
 * <li>混合的情况（图片，视频，音频）<br>
 * <table BORDER="1">
 * <tr>所有文件</tr>
 * <tr>（如果需要显示视频）所有视频</tr>
 * <tr>（如果需要显示音频）所有音频</tr>
 * <tr>（如果需要显示图片）文件夹（图片）</tr>
 * <tr>（如果需要显示图片）……</tr></table>
 * </li>
 * </ol>
 * @author chends create on 2019/9/2.
 */
public class FolderLoader extends CursorLoader {

    private FolderLoader(@NonNull Context context, @NonNull Uri uri, @Nullable String[] projection,
                         @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        super(context, uri, projection, selection, selectionArgs, sortOrder);
    }

    public static FolderLoader newInstance(Context context) {
        return new FolderLoader(context, SelectUtil.getFolderUri(), SelectUtil.getFolderProjection(),
                SelectUtil.getFolderSelection(), null, SelectUtil.SORT_ORDER);
    }

    @Override
    public Cursor loadInBackground() {
        return super.loadInBackground();
    }
}