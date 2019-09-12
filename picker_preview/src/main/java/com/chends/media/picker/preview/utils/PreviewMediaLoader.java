package com.chends.media.picker.preview.utils;

import android.widget.ImageView;

import com.chends.media.picker.preview.listener.PreviewLoaderCallback;
import com.chends.media.picker.utils.MediaLoader;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

/**
 * @author cds created on 2019/9/11.
 */
public abstract class PreviewMediaLoader extends MediaLoader {

    /**
     * 加载大图
     * @param view      view
     * @param imageView imageView
     * @param path      path
     * @param width     width
     * @param height    height
     * @param type      type
     */
    public void loadImageFull(SubsamplingScaleImageView view, ImageView imageView, String path,
                              int width, int height, int type, PreviewLoaderCallback callback) {
    }

    /**
     * 加载视频封面大图
     * @param view   view
     * @param path   path
     * @param width  width
     * @param height height
     */
    public void loadVideoFull(SubsamplingScaleImageView view, String path, int width, int height, PreviewLoaderCallback callback) {

    }

    /**
     * 加载大图
     * @param view   view
     * @param path   path
     * @param width  width
     * @param height height
     */
    public void loadAudioFull(SubsamplingScaleImageView view, String path, int width, int height, PreviewLoaderCallback callback) {

    }


}
