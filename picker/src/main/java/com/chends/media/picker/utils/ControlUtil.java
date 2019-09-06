package com.chends.media.picker.utils;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * 管理
 * @author chends create on 2019/9/6.
 */
public class ControlUtil implements LifecycleObserver {
    private FolderLoaderUtil folderUtil;
    private ItemLoaderUtil itemUtil;

    public ControlUtil(AppCompatActivity activity) {
        activity.getLifecycle().addObserver(this);
        folderUtil = new FolderLoaderUtil();
        //itemUtil = new ItemLoaderUtil();
    }

    /**
     * 创建，前提是必须有存储卡读写权限
     */
    public void onCreate(){
        //if (folderUtil != null) folderUtil.onCreate();
        //if (itemUtil != null) itemUtil.onCreateLoader();
    }

    /**
     * onSaveInstanceState
     * @param outState outState
     */
    public void onSaveInstanceState(Bundle outState) {
        //if (folderUtil != null) folderUtil.onSaveInstanceState(outState);
    }

    /**
     * onRestoreInstanceState
     * @param outState outState
     */
    public void onRestoreInstanceState(Bundle outState) {
        //if (itemUtil != null) itemUtil.onRestoreInstanceState(outState);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy(){
        if (folderUtil != null){
            //folderUtil.onDestroy();
        }
        if (itemUtil != null){
            //itemUtil.onDestroy();
        }
    }
}
