package com.chends.media.picker.utils;

import android.widget.ImageView;

import com.github.piasy.biv.view.BigImageView;

/**
 * MediaLoader
 * @author cds created on 2019/9/8.
 */
public abstract class MediaLoader {
    public void loadImageThumbnail(ImageView view, String path, int width, int height, boolean isGif) {

    }

    public void loadImageFull(BigImageView view, String path, int width, int height, boolean isGif) {

    }

    public void loadVideoThumbnail(ImageView view, String path, int width, int height) {

    }

    public void loadVideoFull(BigImageView view, String path, int width, int height) {

    }

    public void loadAudioThumbnail(ImageView view, String path, int width, int height) {

    }

    public void loadAudioFull(BigImageView view, String path, int width, int height) {

    }
}
