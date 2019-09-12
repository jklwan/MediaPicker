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
     * extra data
     */
    public static final String EXTRA_CHOOSE_DATA = "extra_choose_data";
    /**
     * folder id
     */
    public static final String EXTRA_FOLDER_ID = "extra_folder_id";
    /**
     * 选择position
     */
    public static final String EXTRA_POSITION = "extra_position";

    public static final int TYPE_NORMAL = 10;
    public static final int TYPE_GIF = TYPE_NORMAL + 1;
    public static final int TYPE_APNG = TYPE_GIF + 1;
    public static final int TYPE_WEBP = TYPE_APNG + 1;
    public static final int TYPE_ANIMATED_WEBP = TYPE_WEBP + 1;
    public static final int TYPE_SVG = TYPE_ANIMATED_WEBP + 1;

    /**
     * 图片类型
     */
    @IntDef({TYPE_NORMAL, TYPE_GIF, TYPE_APNG, TYPE_WEBP, TYPE_ANIMATED_WEBP, TYPE_SVG})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ImageType {
    }
}
