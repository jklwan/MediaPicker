package com.chends.media.picker.loader;

import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;

import com.chends.media.picker.model.Constant;

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
 * <tr>文件夹（图片）</tr>
 * <tr>……</tr></table>
 * </li>
 * </ol>
 * @author chends create on 2019/9/2.
 */
public class FolderLoader extends CursorLoader {

    /**
     * projection
     */
    private static final String[] PROJECTION = {
            MediaStore.MediaColumns._ID,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.DATA,
            "count(*) as " + Constant.COLUMN_COUNT};

    public FolderLoader(@NonNull Context context) {
        super(context);
    }

    public FolderLoader(@NonNull Context context, @NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        super(context, uri, projection, selection, selectionArgs, sortOrder);
    }

    /**
     * 混合情况下构建projection
     * @return projection
     */
    /*private static String[] buildProjection(){
        String[] result;
        PickerUtil.getInstance().getBean();
        return result;
    }*/
}