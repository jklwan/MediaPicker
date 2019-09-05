package com.chends.media.picker.utils;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
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

    private PickerBean bean;

    public void setBean(@NonNull PickerBean bean) {
        this.bean = bean;
        bean.init();
    }

    public String getAllImage() {
        return MimeType.getSelectionType(bean.imageList);
    }


    public String getAllVideo() {
        return MimeType.getSelectionType(bean.videoList);
    }


    public String getAllAudio() {
        return MimeType.getSelectionType(bean.audioList);
    }

    /**
     * 初始化数据
     * @param activity activity
     */
    public void init(Activity activity) {
        Context context = activity.getApplicationContext();
        Constant.Folder_Name_All_Media = context.getString(R.string.chooseAllMedia);
        Constant.Folder_Name_All_Image = context.getString(R.string.chooseAllImage);
        Constant.Folder_Name_All_Video = context.getString(R.string.chooseAllVideo);
        Constant.Folder_Name_All_Audio = context.getString(R.string.chooseAllAudio);
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
