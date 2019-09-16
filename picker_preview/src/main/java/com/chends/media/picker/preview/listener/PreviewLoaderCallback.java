package com.chends.media.picker.preview.listener;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.davemorrissey.labs.subscaleview.ImageSource;

import java.io.File;

/**
 * @author chends create on 2019/9/12.
 */
public class PreviewLoaderCallback {
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

    @Nullable
    public ImageView getImageView() {
        return null;
    }
}
