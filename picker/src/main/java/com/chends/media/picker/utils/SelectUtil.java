package com.chends.media.picker.utils;

import android.net.Uri;
import android.provider.MediaStore;

import com.chends.media.picker.MimeType;
import com.chends.media.picker.model.PickerBean;

/**
 * 查询
 * @author chends create on 2019/9/5.
 */
public class SelectUtil {
    private static class Singleton {
        private static final SelectUtil single = new SelectUtil();
    }

    public static SelectUtil getInstance() {
        return Singleton.single;
    }

    private SelectUtil() {
    }

    public static final Uri IMAGE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    public static final Uri VIDEO_URI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
    public static final Uri AUDIO_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    public static final Uri FILE_URI = MediaStore.Files.getContentUri("external");

    /**
     * 总数量
     */
    public static final String COLUMN_COUNT = "count";
    /**
     * 图片数量
     */
    public static final String IMAGE_COUNT = "image_count";
    /**
     * 图片封面
     */
    public static final String IMAGE_COVER = "image_cover";
    /**
     * 视频数量
     */
    public static final String VIDEO_COUNT = "video_count";
    /**
     * 视频封面
     */
    public static final String VIDEO_COVER = "video_cover";
    /**
     * 音频数量
     */
    public static final String AUDIO_COUNT = "audio_count";
    /**
     * 音频封面
     */
    public static final String AUDIO_COVER = "audio_cover";

    /**
     * projection
     */
    private static final String[] PROJECTION = {
            MediaStore.MediaColumns._ID,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.MIME_TYPE,
            "count(*) as " + COLUMN_COUNT};
    /**
     * 查询中大小的条件：必须大于0
     */
    public static final String MIN_SIZE = " and " + MediaStore.MediaColumns.SIZE + ">0";
    /**
     * group bucket
     */
    private static final String GROUP_BUCKET = ") group by (" + MediaStore.Images.Media.BUCKET_ID;

    /**
     * 排序
     */
    public static final String SORT_ORDER = MediaStore.MediaColumns.DATE_MODIFIED + " desc";

    /**
     * 查询条件：所有
     */
    public static String allSelection() {
        StringBuilder mediaType = new StringBuilder("(");
        if (PickerBean.getInstance().hasImage) {
            mediaType.append(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE);
        }
        if (PickerBean.getInstance().hasVideo) {
            if (mediaType.length() > 1) {
                mediaType.append(",");
            }
            mediaType.append(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO);
        }
        if (PickerBean.getInstance().hasAudio) {
            if (mediaType.length() > 1) {
                mediaType.append(",");
            }
            mediaType.append(MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO);
        }
        mediaType.append(")");
        return "(" + MediaStore.MediaColumns.MIME_TYPE + " in " +
                MimeType.getSelectionType(PickerBean.getInstance().typeSet) + " and " +
                MediaStore.Files.FileColumns.MEDIA_TYPE + " in " +
                mediaType.toString() + ")";
    }

    /**
     * 查询条件：所有图片
     */
    public static String imageSelection() {
        return MediaStore.MediaColumns.MIME_TYPE + " in " +
                MimeType.getSelectionType(PickerBean.getInstance().imageList);
    }


    /**
     * 查询条件：所有视频
     */
    public static String videoSelection() {
        return MediaStore.MediaColumns.MIME_TYPE + " in " +
                MimeType.getSelectionType(PickerBean.getInstance().videoList);
    }

    /**
     * 查询条件：所有音频
     */
    public static String audioSelection() {
        return MediaStore.MediaColumns.MIME_TYPE + " in " +
                MimeType.getSelectionType(PickerBean.getInstance().videoList);
    }

    /**
     * folderUri
     */
    private Uri folderUri;
    private String[] folderProjection;
    private String folderSelection;

    void init() {
        PickerBean data = PickerBean.getInstance();
        // 是否混合查询
        StringBuilder selection = new StringBuilder();
        if (data.hasAll) {
            folderUri = FILE_URI;
            folderProjection = buildProjection(data);
            selection.append(allSelection());
        } else {
            folderProjection = PROJECTION;
            if (data.hasImage) {
                folderUri = IMAGE_URI;
                selection.append(imageSelection());
            } else if (data.hasVideo) {
                folderUri = VIDEO_URI;
                selection.append(videoSelection());
            } else {
                folderUri = AUDIO_URI;
                selection.append(audioSelection());
            }
        }
        folderSelection = selection.append(MIN_SIZE).append(GROUP_BUCKET).toString();
    }

    /**
     * folderUir
     * @return folderUir
     */
    public static Uri getFolderUri() {
        return getInstance().folderUri;
    }

    /**
     * 构建 projection
     * @return String[]
     */
    private String[] buildProjection(PickerBean data) {
        String[] result;
        int length, srcLength;
        length = srcLength = PROJECTION.length;
        if (data.hasImage) {
            length += 2;
        }
        if (data.hasVideo) {
            length += 2;
        }
        if (data.hasAudio) {
            length += 2;
        }
        result = new String[length];
        System.arraycopy(PROJECTION, 0, result, 0, srcLength);
        srcLength--;
        if (data.hasImage) {
            srcLength ++;
            result[srcLength] = "(case when " + MediaStore.MediaColumns.MIME_TYPE + " in " +
                    MimeType.getSelectionType(PickerBean.getInstance().imageList) + " then " +
                    MediaStore.MediaColumns.DATA + " else '' end) as " + IMAGE_COVER;
            srcLength ++;
            result[srcLength] = "sum(case when " + MediaStore.MediaColumns.MIME_TYPE + " in " +
                    MimeType.getSelectionType(PickerBean.getInstance().imageList) +
                    " then 1 else 0 end) as " + IMAGE_COUNT;
        }
        if (data.hasVideo) {
            srcLength ++;
            result[srcLength] = "(case when " + MediaStore.MediaColumns.MIME_TYPE + " in " +
                    MimeType.getSelectionType(PickerBean.getInstance().videoList) + " then " +
                    MediaStore.MediaColumns.DATA + " else '' end) as " + VIDEO_COVER;
            srcLength ++;
            result[srcLength] = "sum(case when " + MediaStore.MediaColumns.MIME_TYPE + " in " +
                    MimeType.getSelectionType(PickerBean.getInstance().videoList) +
                    " then 1 else 0 end) as " + VIDEO_COUNT;
        }
        if (data.hasAudio) {
            srcLength ++;
            result[srcLength] = "(case when " + MediaStore.MediaColumns.MIME_TYPE + " in " +
                    MimeType.getSelectionType(PickerBean.getInstance().audioList) + " then " +
                    MediaStore.MediaColumns.DATA + " else '' end) as " + AUDIO_COVER;
            srcLength ++;
            result[srcLength] = "sum(case when " + MediaStore.MediaColumns.MIME_TYPE + " in " +
                    MimeType.getSelectionType(PickerBean.getInstance().audioList) +
                    " then 1 else 0 end) as " + AUDIO_COUNT;
        }
        return result;
    }

    /**
     * 获取 folderProjection
     * @return folderProjection
     */
    public static String[] getFolderProjection() {
        return getInstance().folderProjection;
    }

    /**
     * 获取 selection
     * @return selection
     */
    public static String getFolderSelection() {
        return getInstance().folderSelection;
    }
}
