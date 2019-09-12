package com.chends.media.picker;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.chends.media.picker.model.Constant;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 类型
 * @author chends create on 2019/9/5.
 */
public class MimeType {
    public static final String JPEG = "image/jpeg";
    public static final String PNG = "image/png";
    public static final String BMP = "image/bmp";
    public static final String WEBP = "image/webp";
    public static final String GIF = "image/gif";
    public static final String SVG = "image/svg+xm";
    public static final String MPEG = "video/mpeg";
    public static final String MP4 = "video/mp4";
    public static final String V3GP = "video/3gpp";
    public static final String MKV = "video/x-matroska";
    public static final String AVI = "video/avi";

    /**
     * 所有类型，包括图片和视频
     * @return 所有类型
     */
    public static Set<String> all() {
        return new HashSet<String>() {{
            addAll(allImage());
            addAll(allVideo());
        }};
    }

    /**
     * 所有图片类型
     * @return 图片类型
     */
    public static Set<String> allImage() {
        return new HashSet<String>() {{
            add(JPEG);
            add(PNG);
            add(BMP);
            add(WEBP);
            add(GIF);
            add(SVG);
        }};
    }

    /**
     * 所有视频类型
     * @return 视频类型
     */
    public static Set<String> allVideo() {
        return new HashSet<String>() {{
            add(MPEG);
            add(MP4);
            add(V3GP);
            add(MKV);
            add(AVI);
        }};
    }

    /**
     * 查询时获取type条件
     * @param types types
     * @return selections
     */
    @Nullable
    public static String getSelectionType(Set<String> types) {
        if (types == null || types.isEmpty()) {
            return null;
        }
        StringBuilder builder = new StringBuilder("(");
        String type;
        Iterator<String> it = types.iterator();
        while (it.hasNext()) {
            type = it.next();
            if (TextUtils.isEmpty(type)) {
                continue;
            }
            builder.append("'").append(type).append("'");
            if (it.hasNext()) {
                builder.append(",");
            }
        }
        builder.append(")");
        return builder.toString();
    }

    /**
     * 根据mimeType获取文件type
     * @param mimeType mimeType
     */
    @Constant.ItemType
    public static int getItemType(String mimeType) {
        if (TextUtils.isEmpty(mimeType)) return Constant.TYPE_IMAGE;
        String lower = mimeType.toLowerCase();
        if (lower.startsWith(Constant.IMAGE_START)) {
            return Constant.TYPE_IMAGE;
        } else if (lower.startsWith(Constant.VIDEO_START)) {
            return Constant.TYPE_VIDEO;
        } else {
            return Constant.TYPE_AUDIO;
        }
    }

    /**
     * 是否gif
     * @param mimeType mimeType
     * @return is gif
     */
    public static boolean isGif(String mimeType) {
        if (TextUtils.isEmpty(mimeType)) return false;
        return mimeType.equalsIgnoreCase(GIF);
    }

    /**
     * 图片类型
     * @param path path
     * @return type
     */
    @Constant.ImageType
    public static int getImageType(String mimeType, String path) {
        if (TextUtils.isEmpty(mimeType)) {
            if (!TextUtils.isEmpty(path)) {
                return getImageTypeByPath(path);
            }
        } else {
            if (mimeType.equalsIgnoreCase(GIF)) {
                return Constant.TYPE_GIF;
            } else if (mimeType.equalsIgnoreCase(SVG)) {
                return Constant.TYPE_SVG;
            } else if (mimeType.equalsIgnoreCase(PNG)) {
                if (!TextUtils.isEmpty(path)){
                    return getImageTypeByPath(path);
                }
            } else if (mimeType.equalsIgnoreCase(WEBP)) {
                if (!TextUtils.isEmpty(path)){
                    return getImageTypeByPath(path);
                }
            } else {
                return Constant.TYPE_NORMAL;
            }
        }
        return Constant.TYPE_NORMAL;
    }

    /**
     * 获取图片类型
     * @param path path
     * @return 图片类型
     */
    private static int getImageTypeByPath(String path) {
        int type = Constant.TYPE_NORMAL;
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(path));
            byte[] header = new byte[41];
            int read = inputStream.read(header);
            if (read >= 3 && isGifHeader(header)) {
                type = Constant.TYPE_GIF;
            } else if (read >= 12 && isWebpHeader(header)) {
                if (read >= 33 && isAnimWebp(header)) {
                    type = Constant.TYPE_ANIMATED_WEBP;
                } else {
                    type = Constant.TYPE_WEBP;
                }
            } else if (read >= 40 && isAPNG(header)) {
                type = Constant.TYPE_APNG;
            }
        } catch (IOException ignore) {
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignore) {
                }
            }
        }
        return type;
    }

    /**
     * isGif
     * @param header header
     * @return isGif
     */
    private static boolean isGifHeader(byte[] header) {
        return header[0] == 'G' && header[1] == 'I' && header[2] == 'F';
    }

    /**
     * is Webp
     * @param header header
     * @return is webp
     */
    private static boolean isWebpHeader(byte[] header) {
        return header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P';
    }

    /**
     * is anim Webp
     * @param header header
     * @return is webp
     */
    private static boolean isAnimWebp(byte[] header) {
        return header[12] == 'V' && header[13] == 'P' && header[14] == '8' && header[15] == 'X'
                && header[30] == 'A' && header[31] == 'N' && header[32] == 'I' && header[33] == 'M';
    }

    /**
     * 是否apng 动图
     * @param header header
     * @return is apng
     */
    private static boolean isAPNG(byte[] header) {
        return header[1] == 'P' && header[2] == 'N' && header[3] == 'G' &&
                header[37] == 'a' && header[38] == 'c' && header[39] == 'T' && header[40] == 'L';
    }
}
