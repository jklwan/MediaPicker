package com.chends.media.picker.model;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author chends create on 2019/9/5.
 */
public class Constant {
    public static final String IMAGE_START = "image/";
    public static final String VIDEO_START = "video/";
    public static final String AUDIO_START = "audio/";

    public static final String Folder_Id_All = String.valueOf(-1);
    public static final String Folder_Id_All_Image = String.valueOf(-2);
    public static final String Folder_Id_All_Video = String.valueOf(-3);
    public static final String Folder_Id_All_Audio = String.valueOf(-4);

    public static String Folder_Name_All;
    public static String Folder_Name_All_Image;
    public static String Folder_Name_All_Video;
    public static String Folder_Name_All_Audio;

    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_VIDEO = TYPE_IMAGE + 1;
    public static final int TYPE_AUDIO = TYPE_VIDEO + 1;

    /**
     * 文件类型
     */
    @IntDef({TYPE_IMAGE, TYPE_VIDEO, TYPE_AUDIO})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ItemType {
    }

    /**
     * 保存状态
     */
    public static final String STATE_CURRENT_SELECTION = "state_current_selection";
    /**
     * 权限
     */
    public static final int PERMISSION_STORAGE_REQUEST_CODE = 1;
}
