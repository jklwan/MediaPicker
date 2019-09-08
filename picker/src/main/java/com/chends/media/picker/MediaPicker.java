package com.chends.media.picker;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.chends.media.picker.model.Constant;

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

    private MediaPicker(Activity activity, Object fragment) {
        this.activity = new WeakReference<>(activity);
        this.fragment = new WeakReference<>(fragment);
    }

    public static MediaPicker with(Activity activity) {
        return new MediaPicker(activity, null);
    }

    public static MediaPicker with(android.app.Fragment fragment) {
        if (fragment.getActivity() == null) {
            throw new IllegalArgumentException("fragment getActivity is null!");
        }
        return new MediaPicker(fragment.getActivity(), fragment);
    }

    public static MediaPicker with(Fragment fragment) {
        if (fragment.getActivity() == null) {
            throw new IllegalArgumentException("fragment getActivity is null!");
        }
        return new MediaPicker(fragment.getActivity(), fragment);
    }

    /**
     * 添加选择的类型（可以多次调用，暂时只支持图片和音视频）
     * @param types types
     */
    public PickerManager addTypes(Set<String> types) {
        return new PickerManager(this, types);
    }

    /**
     * activity
     * @return activity
     */
    @Nullable
    Activity getActivity() {
        return activity == null ? null : activity.get();
    }

    /**
     * fragment
     * @return fragment
     */
    @Nullable
    Object getFragment() {
        return fragment == null ? null : fragment.get();
    }

    /**
     * 获取选择的 数据
     * @param data bundle
     * @return data
     */
    public List<String> getData(Bundle data){
        return data.getStringArrayList(Constant.EXTRA_CHOOSE_DATA);
    }
}
