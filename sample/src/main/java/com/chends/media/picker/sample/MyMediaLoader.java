package com.chends.media.picker.sample;

import android.widget.ImageView;

import com.chends.media.picker.utils.MediaLoader;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

/**
 * @author chends create on 2019/9/9.
 */
public class MyMediaLoader extends MediaLoader {

    private void loadThumbnail(ImageView view, String path, int width, int height){
        GlideApp.with(view.getContext()).asBitmap().override(width, height).load(path).into(view);
    }

    @Override
    public void loadImageThumbnail(ImageView view, String path, int width, int height, boolean isGif) {
        GlideApp.with(view.getContext())
                .asBitmap()
                .override(width, height)
                .load(path)
                .placeholder(R.drawable.ic_image_default)
                .error(R.drawable.ic_image_default)
                .fallback(R.drawable.ic_image_default)
                .into(view);
        //MediaLoaderUtil.getInstance(view.getContext()).loadImage(path, view, width, height);
    }

    @Override
    public void loadImageFull(SubsamplingScaleImageView view, String path, int width, int height, boolean isGif) {
        //MediaLoaderUtil.getInstance(view.getContext()).loadImage(path, view, width, height);
    }

    @Override
    public void loadVideoThumbnail(ImageView view, String path, int width, int height) {
        GlideApp.with(view.getContext())
                .asBitmap()
                .override(width, height)
                .load(path)
                .placeholder(R.drawable.ic_video_default)
                .error(R.drawable.ic_video_default)
                .fallback(R.drawable.ic_video_default)
                .into(view);
        //MediaLoaderUtil.getInstance(view.getContext()).loadVideo(path, view, width, height);
    }

    @Override
    public void loadVideoFull(SubsamplingScaleImageView view, String path, int width, int height) {
        //MediaLoaderUtil.getInstance(view.getContext()).loadVideoFull(path, view);
    }

    @Override
    public void loadAudioThumbnail(ImageView view, String path, int width, int height) {
        GlideApp.with(view.getContext())
                .asBitmap()
                .override(width, height)
                .load(new AudioCoverModel(path))
                .placeholder(R.drawable.ic_audio_default)
                .error(R.drawable.ic_audio_default)
                .fallback(R.drawable.ic_audio_default)
                .into(view);
        //loadThumbnail(view, path, width, height);
        //MediaLoaderUtil.getInstance(view.getContext()).loadAudio(path, view, width, height);
    }

    @Override
    public void loadAudioFull(SubsamplingScaleImageView view, String path, int width, int height) {
        //MediaLoaderUtil.getInstance(view.getContext()).loadAudioFull(path, view);
    }
}
