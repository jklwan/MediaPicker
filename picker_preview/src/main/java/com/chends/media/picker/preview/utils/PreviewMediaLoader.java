package com.chends.media.picker.preview.utils;

import android.widget.FrameLayout;

import com.chends.media.picker.preview.listener.PreviewLoaderCallback;
import com.chends.media.picker.utils.MediaLoader;

/**
 * @author cds created on 2019/9/11.
 */
public abstract class PreviewMediaLoader extends MediaLoader {

    /**
     * 加载大图
     * @param frameLayout 根布局
     * @param path        path
     * @param width       width
     * @param height      height
     * @param type        type
     */
    public void loadImageFull(FrameLayout frameLayout, String path, int width, int height, int type, PreviewLoaderCallback callback) {
    }

    /**
     * 加载视频封面大图
     * @param frameLayout 根布局
     * @param path   path
     * @param width  width
     * @param height height
     */
    public void loadVideoFull(FrameLayout frameLayout, String path, int width, int height, PreviewLoaderCallback callback) {

    }

    /**
     * 加载音频大图
     * @param frameLayout 根布局
     * @param path    path
     * @param width   width
     * @param height  height
     */
    public void loadAudioFull(FrameLayout frameLayout, String path, int width, int height, PreviewLoaderCallback callback) {

    }


}
