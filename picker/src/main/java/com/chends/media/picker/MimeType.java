package com.chends.media.picker;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.chends.media.picker.model.Constant;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * 类型
 * @author chends create on 2019/9/5.
 */
public class MimeType {
    public static final String JPEG = "image/jpeg";
    public static final String PNG = "image/png";
    public static final String BMP = "image/x-ms-bmp";
    public static final String BMP2 = "image/bmp";
    public static final String WEBP = "image/webp";
    public static final String GIF = "image/gif";
    public static final String MPEG = "video/mpeg";
    public static final String MP4 = "video/mp4";
    public static final String V3GP = "video/3gpp";
    public static final String MKV = "video/x-matroska";
    public static final String AVI = "video/avi";

    public static final String IMAGE = "image/*";
    public static final String VIDEO = "video/*";
    public static final String AUDIO = "audio/*";
    public static final Map<String, String[]> ImageSuffix;
    public static final Map<String, String[]> VideoSuffix;

    static {
        ImageSuffix = new HashMap<String, String[]>() {{
            put(JPEG, new String[]{"jpg", "jpeg", "jpe"});
            put(PNG, new String[]{"png"});
            put(BMP, new String[]{"bmp"});
            put(BMP2, new String[]{"bmp"});
            put(WEBP, new String[]{"webp"});
            put(GIF, new String[]{"gif"});
        }};

        VideoSuffix = new HashMap<String, String[]>() {{
            put(MPEG, new String[]{"mpeg", "mpg", "mpe", "VOB"});
            put(MP4, new String[]{"mp4"});
            put(V3GP, new String[]{"3gpp", "3gp"});
            put(MKV, new String[]{"mkv"});
            put(AVI, new String[]{"avi"});
        }};
    }

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
            add(BMP2);
            add(WEBP);
            add(GIF);
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
     * 查询时获取后缀集合（type为*）
     * @param types  types
     * @param column 列
     * @param map    map
     * @return selections
     */
    public static String getSelectionSuffix(Set<String> types, String column, Map<String, String[]> map) {
        if (types == null || types.isEmpty()) {
            return "";
        }
        String suffix = null;
        String[] suffixArr;
        Set<String> suffixSet = new HashSet<>();
        for (String type : types) {
            if (TextUtils.isEmpty(type)) {
                continue;
            }
            if (map != null) {
                suffixArr = map.get(type);
                if (suffixArr != null) {
                    suffixSet.addAll(Arrays.asList(suffixArr));
                } else {
                    suffix = MimeTypeMap.getSingleton().getExtensionFromMimeType(type);
                }
            } else {
                suffix = MimeTypeMap.getSingleton().getExtensionFromMimeType(type);
            }
            if (!TextUtils.isEmpty(suffix)) {
                suffixSet.add(suffix);
            }
        }
        StringBuilder builder = new StringBuilder("(");
        Iterator<String> suffixIt = suffixSet.iterator();
        while (suffixIt.hasNext()) {
            suffix = suffixIt.next();
            if (TextUtils.isEmpty(suffix)) {
                continue;
            }
            builder.append(column).append(" like '%_%.").append(suffix).append("'");
            if (suffixIt.hasNext()) {
                builder.append(" or ");
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
        if (!TextUtils.isEmpty(path)) {
            return getImageTypeByPath(path);
        } else {
            if (mimeType.equalsIgnoreCase(GIF)) {
                return Constant.TYPE_GIF;
            } else if (mimeType.equalsIgnoreCase(PNG)) {
                if (!TextUtils.isEmpty(path)) {
                    return getImageTypeByPath(path);
                }
            } else if (mimeType.equalsIgnoreCase(WEBP)) {
                if (!TextUtils.isEmpty(path)) {
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
        FileInputStream is = null;
        try {
            is = new FileInputStream(new File(path));
            byte[] header = new byte[41];
            int read = is.read(header);
            if (read >= 3 && isGifHeader(header)) {
                type = Constant.TYPE_GIF;
            } else if (read >= 8 && isPNG(header)) {
                if (read >= 40 && isAPNG(header)) {
                    type = Constant.TYPE_APNG;
                }
            } else if (read >= 12 && isWebpHeader(header)) {
                if (read >= 33 && isAnimWebp(header)) {
                    type = Constant.TYPE_ANIMATED_WEBP;
                } else {
                    type = Constant.TYPE_WEBP;
                }
            }
        } catch (IOException ignore) {
        } finally {
            if (is != null) {
                try {
                    is.close();
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
     * is Webp header<br/>
     * <a href="https://github.com/webmproject/webp-wic-codec/blob/master/src/libwebp/dec/webp.c">libwebp</a>
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
     * 是否png
     * @param header header
     * @return is png  80 78 71 13 10 26 10
     */
    private static boolean isPNG(byte[] header) {
        return header[0] == (byte) 0x89 && header[1] == 'P' && header[2] == 'N' && header[3] == 'G' &&
                header[4] == (byte) 0x0D && header[5] == (byte) 0x0A && header[6] == (byte) 0x1A && header[7] == (byte) 0x0A;
    }

    /*private static final int acTL_VALUE = 'a' << 24 | 'c' << 16 | 'T' << 8 | 'L';
    private static final int IEND_VALUE = 'I' << 24 | 'E' << 16 | 'N' << 8 | 'D';*/

    /**
     * 是否apng 动图
     * @param header header
     * @return is apng
     */
    private static boolean isAPNG(byte[] header) {
        return header[37] == 'a' && header[38] == 'c' && header[39] == 'T' && header[40] == 'L';
        /*boolean result = false;
        if (is != null) {
            try {
                long start = SystemClock.elapsedRealtime();
                Log.i("parseHeader", "start");
                int capacity = is.available() + 4 * 1024;
                ByteArrayOutputStream buffer = new ByteArrayOutputStream(capacity);
                buffer.write(header, 0, header.length);
                int nRead;
                byte[] data = new byte[16 * 1024];
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();
                ByteBuffer rawData = ByteBuffer.wrap(buffer.toByteArray()).asReadOnlyBuffer();
                rawData.position(0);
                rawData.order(ByteOrder.BIG_ENDIAN);
                Log.i("parseHeader", "length:" + rawData.limit());
                rawData.position(8);
                int length, code;
                boolean done = false;
                while (!done && (rawData.position() < rawData.limit())) {
                    length = rawData.getInt();
                    code = rawData.getInt();
                    Log.i("parseHeader", "length: " + length + ", code:" + code + ", limit:" + rawData.limit());
                    switch (code) {
                        case acTL_VALUE:
                            result = true;
                            done = true;
                            break;
                        case IEND_VALUE:
                            done = true;
                            break;
                        default:
                            if (rawData.position() + length + 4 >= rawData.limit()) {
                                done = true;
                            } else {
                                rawData.position(rawData.position() + length + 4);
                            }
                            break;
                    }
                }
                Log.i("parseHeader", "end use time: " + (SystemClock.elapsedRealtime() - start) + "ms");
            } catch (Exception e) {
                Log.w("Exception", "Error reading data from stream", e);
            }
        }
        Log.i("parseHeader", "result: " + result);
        return result;*/
    }
}
