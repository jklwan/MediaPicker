package com.chends.media.picker.utils;

import android.provider.MediaStore;

/**
 * 查询
 * @author chends create on 2019/9/5.
 */
public class SelectUtil {

    /**
     * 查询中大小的条件：必须大于0
     */
    public static final String MIN_SIZE = " and " + MediaStore.MediaColumns.SIZE + ">0";

    /**
     * 查询条件：所有图片
     */
    public static String getAllImage() {
        return "(" + MediaStore.MediaColumns.MIME_TYPE + " in " +
                PickerUtil.getInstance().getAllImage() + " and " +
                MediaStore.Files.FileColumns.MEDIA_TYPE + "=" +
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE + ")";
    }


    /**
     * 查询条件：所有视频
     */
    public static String AllVideo() {
        return "(" + MediaStore.MediaColumns.MIME_TYPE + " in " +
                PickerUtil.getInstance().getAllVideo() + " and " +
                MediaStore.Files.FileColumns.MEDIA_TYPE + "=" +
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO + ")";
    }

    /**
     * 查询条件：所有音频
     */
    public static String AllAudio() {
        return "(" + MediaStore.MediaColumns.MIME_TYPE + " in " +
                PickerUtil.getInstance().getAllAudio() + " and " +
                MediaStore.Files.FileColumns.MEDIA_TYPE + "=" +
                MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO + ")";
    }
}
