package com.chends.media.picker.utils;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.chends.media.picker.MimeType;
import com.chends.media.picker.R;
import com.chends.media.picker.model.Constant;
import com.chends.media.picker.model.PickerBean;

/**
 * @author chends create on 2019/9/5.
 */
public class PickerUtil {
    private static final PickerUtil single = new PickerUtil();

    public static PickerUtil getInstance() {
        return single;
    }

    private PickerUtil() {
    }

    private int statusHeight = 0;

    /**
     * 获得状态栏的高度
     * @param context Context
     */
    public static int getStatusHeight(Context context) {
        if (getInstance().statusHeight == 0) {
            try {
                int resourceId = context.getResources().getIdentifier("status_bar_height",
                        "dimen", "android");
                if (resourceId > 0) {
                    //根据资源ID获取响应的尺寸值
                    getInstance().statusHeight = context.getResources().getDimensionPixelSize(resourceId);
                }
            } catch (Exception ignore) {
            }
        }
        return getInstance().statusHeight;
    }

    public String getAllImage() {
        return MimeType.getSelectionType(PickerBean.getInstance().imageList);
    }


    public String getAllVideo() {
        return MimeType.getSelectionType(PickerBean.getInstance().videoList);
    }


    public String getAllAudio() {
        return MimeType.getSelectionType(PickerBean.getInstance().audioList);
    }

    /**
     * 初始化数据
     * @param activity activity
     */
    public void init(Activity activity) {
        Context context = activity.getApplicationContext();
        Constant.Folder_Name_All_Media = context.getString(R.string.string_media_picker_chooseAllMedia);
        Constant.Folder_Name_All_Image = context.getString(R.string.string_media_picker_chooseAllImage);
        Constant.Folder_Name_All_Video = context.getString(R.string.string_media_picker_chooseAllVideo);
        Constant.Folder_Name_All_Audio = context.getString(R.string.string_media_picker_chooseAllAudio);
    }

    /**
     * 是否支持当前type
     * @param type type
     * @return type
     */
    public static boolean support(String type) {
        if (MimeTypeMap.getSingleton().hasMimeType(type)) {
            if (!TextUtils.isEmpty(type)) {
                type = type.toLowerCase();
                return type.startsWith(Constant.IMAGE_START) ||
                        type.startsWith(Constant.VIDEO_START) ||
                        type.startsWith(Constant.AUDIO_START);
            }
        }
        return false;
    }


}
