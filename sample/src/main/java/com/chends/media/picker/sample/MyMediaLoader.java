package com.chends.media.picker.sample;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.chends.media.picker.model.Constant;
import com.chends.media.picker.preview.listener.PreviewLoaderCallback;
import com.chends.media.picker.preview.utils.PreviewMediaLoader;
import com.chends.media.picker.sample.audio.AudioCoverModel;
import com.davemorrissey.labs.subscaleview.ImageSource;

import java.io.File;

/**
 * @author chends create on 2019/9/9.
 */
public class MyMediaLoader extends PreviewMediaLoader {
    @Override
    public void loadImageThumbnail(ImageView view, String path, int width, int height, @Constant.ImageType int type) {
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
    public void loadImageFull(FrameLayout frameLayout, String path, int width, int height,
                              @Constant.ImageType int type, final PreviewLoaderCallback callback) {
        //MediaLoaderUtil.getInstance(view.getContext()).loadImage(path, view, width, height);
        switch (type) {
            case Constant.TYPE_NORMAL:
                callback.onLoadImageUseScale(new File(path));
                break;
            case Constant.TYPE_WEBP:
            case Constant.TYPE_ANIMATED_WEBP:
            case Constant.TYPE_APNG:
                GlideApp.with(frameLayout)
                        .asBitmap()
                        .load(path)
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                callback.onLoadImage(true, resource, false);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                                callback.onLoadImageUseScale(ImageSource.resource(R.drawable.ic_image_default));
                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                callback.onLoadImageUseScale(ImageSource.resource(R.drawable.ic_image_default));
                            }
                        });
                break;
            case Constant.TYPE_GIF:
                if (callback.getImageView() != null) {
                    GlideApp.with(frameLayout.getContext())
                            .asGif()
                            .load(path)
                            .into(callback.getImageView());
                }
                break;
            case Constant.TYPE_SVG:
                // load svg
                break;
        }
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
    public void loadVideoFull(FrameLayout frameLayout, String path, int width, int height, PreviewLoaderCallback callback) {
        //MediaLoaderUtil.getInstance(view.getContext()).loadVideoFull(path, view);
        LoadAV(frameLayout, true, path, callback);
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
    public void loadAudioFull(FrameLayout frameLayout, String path, int width, int height, PreviewLoaderCallback callback) {
        //MediaLoaderUtil.getInstance(view.getContext()).loadAudioFull(path, view);
        LoadAV(frameLayout, false, path, callback);
    }

    /**
     * 加载音视频
     * @param frameLayout frameLayout
     * @param isVideo     是否视频
     * @param path        path
     */
    private void LoadAV(final FrameLayout frameLayout, final boolean isVideo, String path, final PreviewLoaderCallback callback) {
        GlideApp.with(frameLayout.getContext())
                .asBitmap()
                .load(isVideo ? path : new AudioCoverModel(path))
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        callback.onLoadImage(true, resource, false);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        callback.onLoadImageUseScale(ImageSource.resource(isVideo ? R.drawable.ic_video_default : R.drawable.ic_audio_default));
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        callback.onLoadImageUseScale(ImageSource.resource(isVideo ? R.drawable.ic_video_default : R.drawable.ic_audio_default));
                    }
                });
    }
}
