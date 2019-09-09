package com.chends.media.picker.sample;

import android.net.Uri;
import android.widget.ImageView;

import com.chends.media.picker.utils.MediaLoader;
import com.github.piasy.biv.view.BigImageView;
import com.github.piasy.biv.view.GlideImageViewFactory;

import java.io.File;

/**
 * @author chends create on 2019/9/9.
 */
public class MyMediaLoader extends MediaLoader {
    private GlideImageViewFactory factory;
    public MyMediaLoader(){
        factory = new GlideImageViewFactory();
    }
    @Override
    public void loadImageThumbnail(ImageView view, String path, int width, int height, boolean isGif) {
        MediaLoaderUtil.getInstance(view.getContext()).loadImage(path, view, width, height);
    }

    @Override
    public void loadImageFull(BigImageView view, String path, int width, int height, boolean isGif) {
        view.setImageViewFactory(factory);
        view.showImage(Uri.fromFile(new File(path)));
    }

    @Override
    public void loadVideoThumbnail(ImageView view, String path, int width, int height) {
        MediaLoaderUtil.getInstance(view.getContext()).loadVideo(path, view, width, height);
    }

    @Override
    public void loadVideoFull(BigImageView view, String path, int width, int height) {
        MediaLoaderUtil.getInstance(view.getContext()).loadVideoFull(path, view);
    }

    @Override
    public void loadAudioThumbnail(ImageView view, String path, int width, int height) {
        MediaLoaderUtil.getInstance(view.getContext()).loadAudio(path, view, width, height);
    }

    @Override
    public void loadAudioFull(BigImageView view, String path, int width, int height) {
        MediaLoaderUtil.getInstance(view.getContext()).loadAudioFull(path, view);
    }
}
