package com.chends.media.picker.utils;

import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.chends.media.picker.R;
import com.chends.media.picker.listener.FolderLoaderCallback;
import com.chends.media.picker.model.FolderBean;
import com.chends.media.picker.widget.MediaPickerView;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * 管理
 * @author chends create on 2019/9/6.
 */
public class ControlUtil implements LifecycleObserver {
    private WeakReference<AppCompatActivity> reference;
    private FolderLoaderUtil folderUtil;
    private ItemLoaderUtil itemUtil;
    private MediaPickerView pickerView;

    public ControlUtil(AppCompatActivity activity) {
        this.reference = new WeakReference<>(activity);
        activity.getLifecycle().addObserver(this);
        LoaderCallBack callBack = new LoaderCallBack();
        folderUtil = new FolderLoaderUtil(activity,callBack);
        //itemUtil = new ItemLoaderUtil();
        pickerView = new MediaPickerView(activity);
        pickerView.setClickListener(new PickerClick());
    }

    private class PickerClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.topBar_back) {
                reference.get().onBackPressed();
            } else if (v.getId() == R.id.topBar_finish) {

            } else if (v.getId() == R.id.picker_folder) {

            } else if (v.getId() == R.id.picker_preview) {

            }
        }
    }

    /**
     * 开始获取数据前提是必须有存储卡读写权限
     */
    public void startLoader() {
        if (checkFolderUtil()) {
            folderUtil.startLoader();
        }
    }

    private class LoaderCallBack implements FolderLoaderCallback{
        @Override
        public void onFolderLoaderFinish(@NonNull List<FolderBean> list) {
            if (checkActivity()) {
                if (list.isEmpty()) {
                    ToastUtils.showLong(reference.get(), R.string.string_media_picker_no_media);
                } else {
                    pickerView.onFolderLoad(list);
                }

            }
        }

        @Override
        public void onFolderLoaderReset() {
            if (checkActivity()) {
                pickerView.onFolderLoaderReset();
            }
        }
    }

    /**
     * onSaveInstanceState
     * @param outState outState
     */
    public void onSaveInstanceState(Bundle outState) {
        if (checkFolderUtil()) {
            folderUtil.onSaveInstanceState(outState);
        }
    }

    /**
     * onRestoreInstanceState
     * @param outState outState
     */
    public void onRestoreInstanceState(Bundle outState) {
        if (checkFolderUtil()) {
            folderUtil.onRestoreInstanceState(outState);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        if (checkFolderUtil()) {
            folderUtil.onDestroy();
        }
        if (checkItemUtil()) {
            //itemUtil.onDestroy();
        }
    }

    private boolean checkActivity(){
        Activity activity;
        if (reference != null && (activity = reference.get()) != null && !activity.isFinishing()){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                return !activity.isDestroyed();
            }
            return true;
        }
        return false;
    }

    private boolean checkFolderUtil() {
        return folderUtil != null;
    }

    private boolean checkItemUtil() {
        return itemUtil != null;
    }
}
