package com.chends.media.picker.preview.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import com.chends.media.picker.scaleview.SubsamplingScaleImageView;
import com.chends.media.picker.utils.PickerUtil;

import java.io.File;
import java.io.IOException;

import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

/**
 * @author chends create on 2019/9/12.
 */
public class PreviewUtil {

    /**
     * 获取文件全名称
     * @param path path
     * @return name
     */
    public static String getFileName(String path) {
        int last = path.lastIndexOf('#');
        if (last < 0) {
            last = path.length();
        }
        int lastPath = path.lastIndexOf('/') + 1;
        String ext = "";
        if (lastPath > 0) {
            ext = path.substring(lastPath, last);
        }
        return ext;
    }

    /**
     * 打开文件
     * @param context  context
     * @param path     path
     * @param mimeType 类型
     */
    public static void openFile(Context context, String path, String mimeType) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri fileUri;
            File file = new File(path);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                fileUri = FileProvider.getUriForFile(context,
                        context.getPackageName() + ".picker_fileprovider", file);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } else {
                fileUri = Uri.fromFile(file);
            }
            intent.setDataAndType(fileUri, mimeType);
            context.startActivity(intent);
        } catch (Exception ignore) {
        }
    }

    private static Boolean hasGifScale = null, hasAPNGScale = null;

    /**
     * 是否有GIF scale
     */
    public static boolean hasGifScale() {
        if (hasGifScale == null) {
            Class cls = null;
            try {
                cls = Class.forName("com.chends.media.picker.gifdecoder.StandardGifDecoder");
            } catch (ClassNotFoundException ignore) {
            }
            hasGifScale = (cls != null);
        }
        return hasGifScale;
    }

    /**
     * 是否有APNG scale
     */
    public static boolean hasAPNGScale() {
        if (hasAPNGScale == null) {
            Class cls = null;
            try {
                cls = Class.forName("com.chends.media.picker.apngdecoder.StandardAPngDecoder");
            } catch (ClassNotFoundException ignore) {
            }
            hasAPNGScale = (cls != null);
        }
        return hasAPNGScale;
    }

    /**
     * 查看大图时图片缩放，只进行缩放，不截取
     * @param source source
     * @return bitmap
     */
    public static Bitmap onlyScaleBitmap(Bitmap source, boolean recycle) {
        int sourceWidth = source.getWidth(), sourceHeight = source.getHeight();
        int maxTextureSize = PickerUtil.maxTextureSize();
        Bitmap result;
        int targetWidth, targetHeight;
        if (sourceHeight > maxTextureSize || sourceWidth > maxTextureSize) {
            // 宽或高大于最大高度
            if (sourceWidth > sourceHeight) {
                targetWidth = maxTextureSize;
                targetHeight = (sourceHeight * maxTextureSize) / sourceWidth;
            } else {
                targetHeight = maxTextureSize;
                targetWidth = (sourceWidth * maxTextureSize) / sourceHeight;
            }
            result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false);
        } else {
            result = source;
        }
        if (recycle && result != source) {
            source.recycle();
        }
        return result;
    }

    /**
     * 宽高缩放
     * @param wh wh
     * @return wh
     */
    public static int[] onlyScaleWH(int[] wh) {
        int maxTextureSize = PickerUtil.maxTextureSize();
        int targetWidth, targetHeight;
        if (wh[0] > maxTextureSize || wh[1] > maxTextureSize) {
            // 宽或高大于最大高度
            if (wh[0] > wh[1]) {
                targetWidth = maxTextureSize;
                targetHeight = (wh[1] * maxTextureSize) / wh[0];
            } else {
                targetHeight = maxTextureSize;
                targetWidth = (wh[0] * maxTextureSize) / wh[1];
            }
            return new int[]{targetWidth, targetHeight};
        } else {
            return wh;
        }
    }

    /**
     * 获取当前图片的旋转角度
     * @param path 图片路径
     */
    public static int getImageOrientation(String path) {
        int orientation = SubsamplingScaleImageView.ORIENTATION_0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int attributeInt = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (attributeInt) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    orientation = SubsamplingScaleImageView.ORIENTATION_90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    orientation = SubsamplingScaleImageView.ORIENTATION_180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    orientation = SubsamplingScaleImageView.ORIENTATION_270;
                    break;
            }
        } catch (IOException ignore) {
        }
        return orientation;
    }
}
