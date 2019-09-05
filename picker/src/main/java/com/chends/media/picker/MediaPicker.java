package com.chends.media.picker;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.chends.media.picker.model.PickerBean;
import com.chends.media.picker.ui.PickerActivity;
import com.chends.media.picker.utils.PickerUtil;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Set;

/**
 * 选择器
 * @author chends create on 2019/9/2.
 */
public class MediaPicker {
    private WeakReference<Activity> activity;
    private WeakReference<Object> fragment;
    private PickerBean pickerBean;

    private MediaPicker(Activity activity) {
        this(activity, null);
    }

    private MediaPicker(android.app.Fragment fragment) {
        this(fragment.getActivity(), fragment);
    }

    private MediaPicker(Fragment fragment) {
        this(fragment.getActivity(), fragment);
    }

    private MediaPicker(Activity activity, Object fragment) {
        this.activity = new WeakReference<>(activity);
        this.fragment = new WeakReference<>(fragment);
        pickerBean = new PickerBean();
    }

    public static MediaPicker with(Activity activity) {
        return new MediaPicker(activity);
    }

    public static MediaPicker with(Fragment fragment) {
        return new MediaPicker(fragment);
    }

    /**
     * 添加选择的类型（可以多次调用，暂时只支持图片和音视频）
     * @param types types
     */
    public MediaPicker addTypes(Set<String> types) {
        pickerBean.addTypes(types);
        return this;
    }

    /**
     * 最大选择数量
     * @param maxNum maxNum
     */
    public MediaPicker maxNum(int maxNum) {
        pickerBean.setMaxNum(maxNum);
        return this;
    }

    /**
     * 设置已选择的列表
     * @param list 已选择的
     */
    public MediaPicker chooseList(List<String> list) {
        pickerBean.setChooseList(list);
        return this;
    }

    /**
     * 限制视频时长
     * @param second second
     */
    public MediaPicker setVideoLimit(int second){
        pickerBean.setVideoLimit(second);
        return this;
    }

    /**
     * 限制音频时长
     * @param second second
     */
    public MediaPicker setAudioLimit(int second){
        pickerBean.setAudioLimit(second);
        return this;
    }

    /**
     * activity
     * @return activity
     */
    private Activity getActivity() {
        return activity == null ? null : activity.get();
    }

    /**
     * fragment
     * @return fragment
     */
    private Object getFragment() {
        return fragment == null ? null : fragment.get();
    }

    /**
     * 开始
     * @param requestCode requestCode
     */
    public void start(int requestCode) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        PickerUtil.getInstance().setBean(pickerBean);
        PickerUtil.getInstance().init(activity);

        Intent intent = new Intent(activity, PickerActivity.class);
        Object fragment = getFragment();
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
