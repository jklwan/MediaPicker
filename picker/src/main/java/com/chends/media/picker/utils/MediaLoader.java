package com.chends.media.picker.utils;

import android.widget.ImageView;

import com.chends.media.picker.model.Constant;

import java.io.Serializable;

/**
 * MediaLoader
 * @author cds created on 2019/9/8.
 */
public abstract class MediaLoader implements Serializable {
    /**
     * 加载图片缩略图
     * @param view   view
     * @param path   path
     * @param width  width
     * @param height height
     * @param type   type
     */
    public void loadImageThumbnail(ImageView view, String path, int width, int height, @Constant.ImageType int type) {

    }

    /**
     * 加载视频封面
     * @param view   view
     * @param path   path
     * @param width  width
     * @param height height
     */
    public void loadVideoThumbnail(ImageView view, String path, int width, int height) {

    }

    /**
     * 加载音频封面
     * @param view   view
     * @param path   path
     * @param width  width
     * @param height height
     */
    public void loadAudioThumbnail(ImageView view, String path, int width, int height) {

    }

}
