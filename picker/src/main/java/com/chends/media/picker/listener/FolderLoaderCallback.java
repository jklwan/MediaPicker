package com.chends.media.picker.listener;

import com.chends.media.picker.model.FolderBean;

import java.util.List;

import androidx.annotation.NonNull;

/**
 * @author chends create on 2019/9/7.
 */
public interface FolderLoaderCallback {
    void onFolderLoaderFinish(@NonNull List<FolderBean> list);

    void onFolderLoaderReset();
}
