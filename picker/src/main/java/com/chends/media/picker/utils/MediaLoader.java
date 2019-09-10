package com.chends.media.picker.utils;

import android.widget.ImageView;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

/**
 * MediaLoader
 * @author cds created on 2019/9/8.
 */
public abstract class MediaLoader {
    public void loadImageThumbnail(ImageView view, String path, int width, int height, boolean isGif) {

    }

    public void loadImageFull(SubsamplingScaleImageView view, String path, int width, int height, boolean isGif) {

    }

    public void loadVideoThumbnail(ImageView view, String path, int width, int height) {

    }

    public void loadVideoFull(SubsamplingScaleImageView view, String path, int width, int height) {

    }

    public void loadAudioThumbnail(ImageView view, String path, int width, int height) {

    }

    public void loadAudioFull(SubsamplingScaleImageView view, String path, int width, int height) {

    }
}
