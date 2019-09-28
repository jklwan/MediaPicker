package com.chends.media.picker.preview.listener;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.chends.media.picker.scaleview.ImageSource;

import java.io.File;

import androidx.annotation.NonNull;

/**
 * @author chends create on 2019/9/12.
 */
public abstract class PreviewLoaderCallback {
    /**
     * 使用缩放布局进行加载
     * @param file file
     */
    public void onLoadImageUseScale(File file) {
    }

    /**
     * 图片加载完成后
     * @param useScaleImage 是否使用缩放布局
     * @param bitmap        bitmap
     * @param needRecycle   使用完后是否需要主动回收
     */
    public void onLoadImage(boolean useScaleImage, Bitmap bitmap, boolean needRecycle) {
    }

    /**
     * 使用缩放布局进行加载
     * @param source source
     */
    public void onLoadImageUseScale(ImageSource source) {
    }

    @NonNull
    public abstract ImageView getImageView();
}
