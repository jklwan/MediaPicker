package com.chends.media.picker.sample.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.media.ExifInterface;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * 图片处理<br/>
 * Created by cds on 2017/7/6.
 */
public class BitmapUtil {
    /**
     * 获取当前图片的旋转角度
     * @param path 图片路径
     */
    public static int getImageAngle(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 图片缩放，生成缩略图
     * @param reqWidth  需要的宽度
     * @param reqHeight 需要的高度
     * @param filePath  为空时，不是本地图片，不进行旋转
     * @param source    source
     * @return bitmap
     */
    public static Bitmap scaleBitmap(int reqWidth, int reqHeight, String filePath, Bitmap source, boolean isImage) {
        if (source == null || source.isRecycled()) return null;
        if (!TextUtils.isEmpty(filePath) && isImage && isImage(new File(filePath))) {
            int degree = getImageAngle(filePath);
            if (degree != 0) {
                Matrix m = new Matrix();
                m.postRotate(degree);
                source = Bitmap.createBitmap(source, 0, 0, source.getWidth(),
                        source.getHeight(), m, true);
            }
        }
        int sourceWidth = source.getWidth(), sourceHeight = source.getHeight();
        int maxTextureSize = FileUtil.getMaxTextureSize() - 10;
        Bitmap result;
        int targetWidth, targetHeight;
        if (reqWidth > 0 && reqHeight > 0) {
            if (reqWidth > maxTextureSize || reqHeight > maxTextureSize) {
                // 需要的宽高大于最大宽高
                if (reqWidth > reqHeight) {
                    reqHeight = (maxTextureSize * reqHeight) / reqWidth;
                    reqWidth = maxTextureSize;
                } else {
                    reqWidth = (maxTextureSize * reqWidth) / reqHeight;
                    reqHeight = maxTextureSize;
                }
            }
            float widthSize = sourceWidth / (float) reqWidth;
            float heightSize = sourceHeight / (float) reqHeight;
            if (widthSize == heightSize) {
                // 直接缩放即可
                result = Bitmap.createScaledBitmap(source, reqWidth, reqHeight, false);
            } else {
                float ratio = reqWidth / (float) reqHeight, sx = 1, sy = 1;
                boolean needScale;
                if (widthSize > heightSize) {
                    targetWidth = (int) (sourceHeight * ratio);
                    targetHeight = sourceHeight;
                    needScale = (sourceHeight > reqHeight);
                    if (needScale) {
                        sx = (float) reqHeight / sourceHeight;
                        sy = sx;
                    }
                } else {
                    targetWidth = sourceWidth;
                    targetHeight = (int) (sourceWidth / ratio);
                    needScale = (sourceWidth > reqWidth);
                    if (needScale) {
                        sx = (float) reqWidth / sourceWidth;
                        sy = sx;
                    }
                }
                int x = Math.max((sourceWidth - targetWidth) / 2, 0);
                int y = Math.max((sourceHeight - targetHeight) / 2, 0);
                if (needScale) {
                    Matrix m = new Matrix();
                    m.setScale(sx, sy);
                    result = Bitmap.createBitmap(source, x, y, targetWidth, targetHeight, m, false);
                } else {
                    result = Bitmap.createBitmap(source, x, y, targetWidth, targetHeight);
                }
            }
        } else {
            // 判断最大宽高
            if (sourceHeight > maxTextureSize || sourceWidth > maxTextureSize) {
                // 宽或高大于最大高度
                if (sourceHeight > sourceWidth) {
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
        }
        if (result != source) {
            // Same bitmap is returned if sizes are the same
            source.recycle();
        }
        return result;
    }

    /**
     * 是否图片文件
     * @param file file
     * @return true or false
     */
    private static boolean isImage(File file) {
        try {
            if (file != null && file.isFile() && file.exists()) {
                int[] wh = getImageWH(file.getAbsolutePath());
                return wh[0] > 0 && wh[1] > 0;
            }
            return false;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * 获取图片的宽高
     * @param imagePath imagePath
     * @return int[]，0：宽，1：高
     */
    private static int[] getImageWH(String imagePath) {
        int[] wh = new int[]{0, 0};
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imagePath, options);
            wh[0] = options.outWidth;
            wh[1] = options.outHeight;
            if (wh[0] <= 0 || wh[1] <= 0) {
                ExifInterface exifInterface = new ExifInterface(imagePath);
                wh[1] = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, ExifInterface.ORIENTATION_NORMAL);//获取图片的高度
                wh[0] = exifInterface.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, ExifInterface.ORIENTATION_NORMAL);//获取图片的宽度
            }
        } catch (Exception ex) {
            LogUtil.e("getImageWH error:" + ex.getMessage());
        }
        return wh;
    }

    /**
     * 根据需要的大小生成bitmap图片
     * @param bitmap      bitmap
     * @param requireSize 需要的大小
     */
    public static void compressBitmap(Bitmap bitmap, double requireSize, ByteArrayOutputStream byteArrayOs) {
        int quality = 100;
        int sub;
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOs);
        double byteSize = byteArrayOs.toByteArray().length / 1024.0;
        while (byteSize > requireSize * 1.1 && quality > 10) {
            double times = ((byteSize * 1.0) / requireSize);
            switch ((int) Math.round(times)) {
                case 1:
                    if (times > 1.2) {
                        quality = (int) (quality * 0.93f);
                    } else {
                        quality = (int) (quality * 0.96f);
                    }
                    sub = 1;
                    break;
                case 2:
                    quality = (int) (quality * 0.9f);
                    sub = 2;
                    break;
                case 3:
                case 4:
                    quality = (int) (quality * 0.82f);
                    sub = 4;
                    break;
                default:
                    quality = (int) (quality * 0.75f);
                    sub = 6;
                    break;
            }
            byteArrayOs.reset();
            quality -= sub;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOs);
            byteSize = byteArrayOs.toByteArray().length / 1024.0;
            LogUtil.d("quality:" + quality);
        }
    }
}
