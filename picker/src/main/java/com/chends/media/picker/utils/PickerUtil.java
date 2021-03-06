package com.chends.media.picker.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.support.media.ExifInterface;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.chends.media.picker.MimeType;
import com.chends.media.picker.R;
import com.chends.media.picker.model.Constant;
import com.chends.media.picker.model.ItemBean;
import com.chends.media.picker.model.PickerBean;

import java.io.File;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

/**
 * @author chends create on 2019/9/5.
 */
public class PickerUtil {
    private static class Singleton {
        private static final PickerUtil single = new PickerUtil();
    }

    public static PickerUtil getInstance() {
        return Singleton.single;
    }

    private PickerUtil() {
    }

    private int statusHeight = 0;
    private float density = 0.0f;
    private int maxTextureSize = 0;

    /**
     * 获得状态栏的高度
     * @param context Context
     */
    public static int getStatusHeight(Context context) {
        if (getInstance().statusHeight == 0) {
            try {
                int resourceId = context.getResources().getIdentifier("status_bar_height",
                        "dimen", "android");
                if (resourceId > 0) {
                    //根据资源ID获取响应的尺寸值
                    getInstance().statusHeight = context.getResources().getDimensionPixelSize(resourceId);
                }
            } catch (Exception ignore) {
            }
        }
        return getInstance().statusHeight;
    }

    public static int dp2px(float dpValue) {
        return (int) (dpValue * getDensity() + 0.5f);
    }

    public static float getDensity() {
        if (getInstance().density <= 0.0f) {
            getInstance().density = Resources.getSystem().getDisplayMetrics().density;
        }
        return getInstance().density;
    }

    /**
     * 初始化数据
     * @param activity activity
     */
    public void init(Activity activity) {
        Context context = activity.getApplicationContext();
        Constant.Folder_Name_All = context.getString(R.string.string_media_picker_chooseAll);
        Constant.Folder_Name_All_Image = context.getString(R.string.string_media_picker_chooseAllImage);
        Constant.Folder_Name_All_Video = context.getString(R.string.string_media_picker_chooseAllVideo);
        Constant.Folder_Name_All_Audio = context.getString(R.string.string_media_picker_chooseAllAudio);
        initOther();
    }

    private void initOther() {
        PickerBean.getInstance().init();
        SelectUtil.getInstance().init();
    }

    /**
     * 是否支持当前type
     * @param type type
     * @return type
     */
    public static boolean support(String type) {
        if (MimeTypeMap.getSingleton().hasMimeType(type)) {
            if (!TextUtils.isEmpty(type)) {
                type = type.toLowerCase();
                return type.startsWith(Constant.IMAGE_START) ||
                        type.startsWith(Constant.VIDEO_START) ||
                        type.startsWith(Constant.AUDIO_START);
            }
        }
        return false;
    }

    /**
     * checkFile
     * @param context context
     * @param bean    bean
     * @return 文件是否有效
     */
    public static boolean checkFile(Context context, ItemBean bean) {
        if (!isFileExist(context, bean.getPath())) {
            scanMediaFile(context, bean.getPath());
            return false;
        }
        int type = MimeType.getItemType(bean.getMimeType());
        if (type == Constant.TYPE_IMAGE) {
            return isImageFile(bean.getPath());
        } else {
            return bean.getDuration() > 0;
        }
    }

    /**
     * 是否存在
     * @param filePath path
     * @return true or false
     */
    public static boolean isFileExist(Context context, String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        File file = new File(filePath);
        if (file.exists()) {
            return true;
        }
        scanMediaFile(context, file.getAbsolutePath());
        return false;
    }

    /**
     * 通知扫描文件
     * @param context context
     * @param path    path
     */
    public static void scanMediaFile(Context context, String path) {
        MediaScannerConnection.scanFile(context, new String[]{path}, null, null);
    }

    /**
     * 是否图片文件
     * @param path path
     * @return true or false
     */
    public static boolean isImageFile(String path) {
        try {
            File file = new File(path);
            if (!TextUtils.isEmpty(path) && file.isFile() && file.exists()) {
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
    public static int[] getImageWH(String imagePath) {
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
        } catch (Exception ignore) {
        }
        return wh;
    }

    /**
     * 时间长度
     * @param duration duration
     * @return duration
     */
    public static String getDuration(long duration) {
        long durationS = (duration - 1) / 1000 + 1;
        long second = durationS % 60;
        long minute = durationS / 60;
        StringBuilder builder = new StringBuilder();
        if (minute > 0) {
            if (minute < 10) {
                builder.append("0");
            }
            builder.append(minute).append(":");
        } else {
            builder.append("00:");
        }
        if (second > 0) {
            if (second < 10) {
                builder.append("0");
            }
            builder.append(second);
        } else {
            builder.append("00");
        }
        return builder.toString();
    }

    /**
     * 获取名称无扩展名
     * @param path path
     * @return name
     */
    public static String getFileNameNoExtension(String path) {
        int last = path.lastIndexOf('#');
        if (last < 0) {
            last = path.length();
        }
        int lasDot = path.lastIndexOf('.');
        if (lasDot != -1 && lasDot < last) {
            last = lasDot;
        }
        int lastPath = path.lastIndexOf('/') + 1;
        String ext = "";
        if (lastPath > 0) {
            ext = path.substring(lastPath, last);
        }
        return ext;
    }

    /**
     * select点击
     * @param context context
     * @param item    item
     */
    public static boolean selectPath(Context context, ItemBean item) {
        String path = item.getPath();
        if (PickerBean.getInstance().chooseList.contains(path)) {
            PickerBean.getInstance().chooseList.remove(path);
            PickerBean.getInstance().chooseItem.remove(item);
        } else {
            if (PickerBean.getInstance().chooseList.size() >= PickerBean.getInstance().maxNum) {
                int resId;
                if (!PickerBean.getInstance().hasAll && PickerBean.getInstance().hasImage) {
                    resId = R.string.string_media_picker_chooseMaxImage;
                } else {
                    resId = R.string.string_media_picker_chooseMaxFile;
                }
                ToastUtils.showShort(context, context.getString(resId, PickerBean.getInstance().maxNum));
                return false;
            }
            PickerBean.getInstance().chooseList.add(path);
            PickerBean.getInstance().chooseItem.add(item);
        }
        return true;
    }

    /**
     * 获取预览页
     * @return preview
     */
    public static Class getPreview() {
        Class cls = null;
        try {
            cls = Class.forName("com.chends.media.picker.preview.ui.PreviewActivity");
        } catch (ClassNotFoundException ignore) {

        }
        return cls;
    }

    /**
     * 最大可显示图片宽高
     * @return size
     */
    public static int maxTextureSize() {
        if (getInstance().maxTextureSize <= 0) {
            // Safe minimum default size
            final int IMAGE_MAX_BITMAP_DIMENSION = 512;

            // Get EGL Display
            EGL10 egl = (EGL10) EGLContext.getEGL();
            EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

            // Initialise
            int[] version = new int[2];
            egl.eglInitialize(display, version);

            // Query total number of configurations
            int[] totalConfigurations = new int[1];
            egl.eglGetConfigs(display, null, 0, totalConfigurations);

            // Query actual list configurations
            EGLConfig[] configurationsList = new EGLConfig[totalConfigurations[0]];
            egl.eglGetConfigs(display, configurationsList, totalConfigurations[0], totalConfigurations);

            int[] textureSize = new int[1];
            int maximumTextureSize = 0;

            // Iterate through all the configurations to located the maximum texture size
            for (int i = 0; i < totalConfigurations[0]; i++) {
                // Only need to check for width since opengl textures are always squared
                egl.eglGetConfigAttrib(display, configurationsList[i], EGL10.EGL_MAX_PBUFFER_WIDTH, textureSize);

                // Keep track of the maximum texture size
                if (maximumTextureSize < textureSize[0]) {
                    maximumTextureSize = textureSize[0];
                }
            }

            // Release
            egl.eglTerminate(display);

            // Return largest texture size found, or default
            getInstance().maxTextureSize = Math.max(maximumTextureSize, IMAGE_MAX_BITMAP_DIMENSION);
        }
        return getInstance().maxTextureSize;
    }
}
