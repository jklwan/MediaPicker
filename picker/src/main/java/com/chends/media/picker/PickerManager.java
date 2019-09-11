package com.chends.media.picker;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.chends.media.picker.model.PickerBean;
import com.chends.media.picker.ui.MediaPickerActivity;
import com.chends.media.picker.utils.MediaLoader;
import com.chends.media.picker.utils.PickerUtil;

import java.util.List;
import java.util.Set;

/**
 * PickerManager
 * @author chends create on 2019/9/6.
 */
public final class PickerManager {
    private final MediaPicker picker;
    private final PickerBean pickerBean;

    PickerManager(MediaPicker picker, Set<String> types) {
        this.picker = picker;
        this.pickerBean = PickerBean.resetInstance();
        pickerBean.addTypes(types);
    }

    /**
     * 添加选择的类型（可以多次调用，暂时只支持图片和音视频）
     * @param types types
     */
    public PickerManager addTypes(Set<String> types) {
        pickerBean.addTypes(types);
        return this;
    }

    /**
     * 最大选择数量
     * @param maxNum maxNum
     */
    public PickerManager maxNum(int maxNum) {
        pickerBean.setMaxNum(maxNum);
        return this;
    }

    /**
     * 设置已选择的列表
     * @param list 已选择的
     */
    public PickerManager chooseList(List<String> list) {
        pickerBean.setChooseList(list);
        return this;
    }

    /**
     * 限制视频时长
     * @param second second
     */
    /*public PickerManager setVideoLimit(int second) {
        pickerBean.setVideoLimit(second);
        return this;
    }*/

    /**
     * 限制音频时长
     * @param second second
     */
    /*public PickerManager setAudioLimit(int second) {
        pickerBean.setAudioLimit(second);
        return this;
    }*/


    /**
     * 图片加载
     */
    public PickerManager setLoader(MediaLoader loader) {
        pickerBean.setLoader(loader);
        return this;
    }

    /**
     * 每行显示数量
     * @param spanCount spanCount
     */
    public PickerManager setSpanCount(int spanCount) {
        pickerBean.setSpanCount(spanCount);
        return this;
    }

    /**
     * 开始
     * @param requestCode requestCode
     */
    public void start(int requestCode) {
        if (picker == null) return;
        Activity activity = picker.getActivity();
        if (activity == null) {
            return;
        }
        PickerUtil.getInstance().init(activity);

        Intent intent = new Intent(activity, MediaPickerActivity.class);
        Object fragment = picker.getFragment();
        if (fragment != null) {
            if (fragment instanceof Fragment) {
                ((Fragment) fragment).startActivityForResult(intent, requestCode);
            } else if (fragment instanceof android.app.Fragment) {
                ((android.app.Fragment) fragment).startActivityForResult(intent, requestCode);
            }
        } else {
            activity.startActivityForResult(intent, requestCode);
        }
    }
}
