package com.chends.media.picker.utils;

import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.chends.media.picker.MimeType;
import com.chends.media.picker.model.Constant;
import com.chends.media.picker.model.PickerBean;

import java.util.List;

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

    private static final Uri IMAGE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    private static final Uri VIDEO_URI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
    private static final Uri AUDIO_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    private static final Uri FILE_URI = MediaStore.Files.getContentUri("external");

    /**
     * 总数量
     */
    public static final String COLUMN_COUNT = "count";
    /**
     * 图片数量
     */
    static final String IMAGE_COUNT = "image_count";
    /**
     * 图片类型
     */
    static final String IMAGE_MIME_TYPE = "image_mime_type";
    /**
     * 图片封面
     */
    static final String IMAGE_COVER = "image_cover";
    /**
     * 视频数量
     */
    static final String VIDEO_COUNT = "video_count";
    /**
     * 视频类型
     */
    static final String VIDEO_MIME_TYPE = "video_mime_type";
    /**
     * 视频封面
     */
    static final String VIDEO_COVER = "video_cover";
    /**
     * 音频数量
     */
    static final String AUDIO_COUNT = "audio_count";
    /**
     * 音频类型
     */
    static final String AUDIO_MIME_TYPE = "audio_mime_type";
    /**
     * 音频封面
     */
    static final String AUDIO_COVER = "audio_cover";

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
     * item projection
     */
    private static final String[] ITEM_PROJECTION = {
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.Video.Media.DURATION};
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
    private static String allSelection() {
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
    private static String imageSelection() {
        return MediaStore.MediaColumns.MIME_TYPE + " in " +
                MimeType.getSelectionType(PickerBean.getInstance().imageList);
    }


    /**
     * 查询条件：所有视频
     */
    private static String videoSelection() {
        return MediaStore.MediaColumns.MIME_TYPE + " in " +
                MimeType.getSelectionType(PickerBean.getInstance().videoList);
    }

    /**
     * 查询条件：所有音频
     */
    private static String audioSelection() {
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
            length += 3;
        }
        if (data.hasVideo) {
            length += 3;
        }
        if (data.hasAudio) {
            length += 3;
        }
        result = new String[length];
        System.arraycopy(PROJECTION, 0, result, 0, srcLength);
        srcLength--;
        if (data.hasImage) {
            srcLength++;
            result[srcLength] = "(case when " + MediaStore.MediaColumns.MIME_TYPE + " in " +
                    MimeType.getSelectionType(PickerBean.getInstance().imageList) + " then " +
                    MediaStore.MediaColumns.DATA + " else '' end) as " + IMAGE_COVER;
            srcLength++;
            result[srcLength] = "(case when " + MediaStore.MediaColumns.MIME_TYPE + " in " +
                    MimeType.getSelectionType(PickerBean.getInstance().imageList) + " then " +
                    MediaStore.MediaColumns.MIME_TYPE + " else '' end) as " + IMAGE_MIME_TYPE;
            srcLength++;
            result[srcLength] = "sum(case when " + MediaStore.MediaColumns.MIME_TYPE + " in " +
                    MimeType.getSelectionType(PickerBean.getInstance().imageList) +
                    " then 1 else 0 end) as " + IMAGE_COUNT;
        }
        if (data.hasVideo) {
            srcLength++;
            result[srcLength] = "(case when " + MediaStore.MediaColumns.MIME_TYPE + " in " +
                    MimeType.getSelectionType(PickerBean.getInstance().videoList) + " then " +
                    MediaStore.MediaColumns.DATA + " else '' end) as " + VIDEO_COVER;
            srcLength++;
            result[srcLength] = "(case when " + MediaStore.MediaColumns.MIME_TYPE + " in " +
                    MimeType.getSelectionType(PickerBean.getInstance().videoList) + " then " +
                    MediaStore.MediaColumns.MIME_TYPE + " else '' end) as " + VIDEO_MIME_TYPE;
            srcLength++;
            result[srcLength] = "sum(case when " + MediaStore.MediaColumns.MIME_TYPE + " in " +
                    MimeType.getSelectionType(PickerBean.getInstance().videoList) +
                    " then 1 else 0 end) as " + VIDEO_COUNT;
        }
        if (data.hasAudio) {
            srcLength++;
            result[srcLength] = "(case when " + MediaStore.MediaColumns.MIME_TYPE + " in " +
                    MimeType.getSelectionType(PickerBean.getInstance().audioList) + " then " +
                    MediaStore.MediaColumns.DATA + " else '' end) as " + AUDIO_COVER;
            srcLength++;
            result[srcLength] = "(case when " + MediaStore.MediaColumns.MIME_TYPE + " in " +
                    MimeType.getSelectionType(PickerBean.getInstance().audioList) + " then " +
                    MediaStore.MediaColumns.MIME_TYPE + " else '' end) as " + AUDIO_MIME_TYPE;
            srcLength++;
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

    /**
     * 获取uri
     * @param folderId folderId
     * @return uri
     */
    public static Uri getItemUri(String folderId) {
        if (TextUtils.equals(Constant.Folder_Id_All, folderId)) {
            return FILE_URI;
        } else if (TextUtils.equals(Constant.Folder_Id_All_Video, folderId)) {
            return VIDEO_URI;
        } else if (TextUtils.equals(Constant.Folder_Id_All_Audio, folderId)) {
            return AUDIO_URI;
        } else if (TextUtils.equals(Constant.Folder_Id_All_Image, folderId)) {
            return IMAGE_URI;
        } else {
            return IMAGE_URI;
        }
    }

    /**
     * 获取 projection
     * @return projection
     */
    public static String[] getItemProjection() {
        return ITEM_PROJECTION;
    }

    /**
     * 获取 selection
     * @param folderId folderId
     * @return uri
     */
    public static String getItemSelection(String folderId) {
        StringBuilder selection = new StringBuilder();
        if (TextUtils.equals(Constant.Folder_Id_All, folderId)) {
            selection.append(allSelection());
        } else if (TextUtils.equals(Constant.Folder_Id_All_Video, folderId)) {
            selection.append(videoSelection());
        } else if (TextUtils.equals(Constant.Folder_Id_All_Audio, folderId)) {
            selection.append(audioSelection());
        } else if (TextUtils.equals(Constant.Folder_Id_All_Image, folderId)) {
            selection.append(imageSelection());
        } else {
            selection.append(imageSelection())
                    .append(" and ")
                    .append(MediaStore.Images.Media.BUCKET_ID)
                    .append("=")
                    .append(folderId);
        }
        return selection.append(MIN_SIZE).toString();
    }

    /**
     * 查询条件
     * @return selection
     */
    public static String getSearchSelection(){
        StringBuilder selection = new StringBuilder(MediaStore.MediaColumns.DATA);
        selection.append(" in (");
        List<String> list = PickerBean.getInstance().chooseList;
        for (String item : list){
            if (!TextUtils.isEmpty(item)) {
                selection.append("'").append(item).append("'");
            }
        }
        selection.append(")").append(MIN_SIZE);
        return selection.toString();
    }
}
