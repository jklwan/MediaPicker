package com.chends.media.picker.utils;

import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.chends.media.picker.MimeType;
import com.chends.media.picker.model.Constant;
import com.chends.media.picker.model.PickerBean;

import java.util.Iterator;

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
        StringBuilder selection = new StringBuilder("(");
        if (PickerBean.getInstance().hasImage) {
            selection.append("(").append(MediaStore.Files.FileColumns.MEDIA_TYPE)
                    .append(" = ").append(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)
                    .append(" and ").append(imageSelection()).append(")");
        }
        if (PickerBean.getInstance().hasVideo) {
            if (selection.length() > 1) {
                selection.append(" or ");
            }
            selection.append("(").append(MediaStore.Files.FileColumns.MEDIA_TYPE)
                    .append(" = ").append(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)
                    .append(" and ").append(videoSelection()).append(")");
        }
        if (PickerBean.getInstance().hasAudio) {
            if (selection.length() > 1) {
                selection.append(" or ");
            }
            selection.append("(").append(MediaStore.Files.FileColumns.MEDIA_TYPE)
                    .append(" = ").append(MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO)
                    .append(" and ").append(audioSelection()).append(")");
        }
        selection.append(")");
        return selection.toString();
    }

    /**
     * 查询条件：图片
     */
    private static String imageSelection() {
        return "(" + MediaStore.MediaColumns.MIME_TYPE + " in " +
                MimeType.getSelectionType(PickerBean.getInstance().imageList) + " or (" +
                MediaStore.MediaColumns.MIME_TYPE + "= '" + MimeType.IMAGE + "' and " +
                MimeType.getSelectionSuffix(PickerBean.getInstance().imageList,
                        MediaStore.MediaColumns.DATA, MimeType.ImageSuffix) + "))";
    }


    /**
     * 查询条件：所有视频
     */
    private static String videoSelection() {
        return "(" + MediaStore.MediaColumns.MIME_TYPE + " in " +
                MimeType.getSelectionType(PickerBean.getInstance().videoList) + " or (" +
                MediaStore.MediaColumns.MIME_TYPE + "= '" + MimeType.VIDEO + "' and " +
                MimeType.getSelectionSuffix(PickerBean.getInstance().videoList,
                        MediaStore.MediaColumns.DATA, MimeType.VideoSuffix) + "))";
    }

    /**
     * 查询条件：所有音频
     */
    private static String audioSelection() {
        return "(" + MediaStore.MediaColumns.MIME_TYPE + " in " +
                MimeType.getSelectionType(PickerBean.getInstance().audioList) + " or (" +
                MediaStore.MediaColumns.MIME_TYPE + "= '" + MimeType.AUDIO + "' and " +
                MimeType.getSelectionSuffix(PickerBean.getInstance().audioList,
                        MediaStore.MediaColumns.DATA, null) + "))";
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
        folderProjection = PROJECTION;
        if (data.hasAll) {
            folderUri = FILE_URI;
            selection.append(allSelection());
        } else {
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
        selection.append(MIN_SIZE).append(GROUP_BUCKET);
        if (data.hasAll) {
            selection.append("),(").append(MediaStore.Files.FileColumns.MEDIA_TYPE);
        }
        folderSelection = selection.toString();
    }

    /**
     * folderUir
     * @return folderUir
     */
    public static Uri getFolderUri() {
        return getInstance().folderUri;
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
     * @param folderId 文件夹id
     * @return projection
     */
    public static String[] getItemProjection(String folderId) {
        if (TextUtils.equals(Constant.Folder_Id_All, folderId) ||
                TextUtils.equals(Constant.Folder_Id_All_Video, folderId) ||
                TextUtils.equals(Constant.Folder_Id_All_Audio, folderId)) {
            return ITEM_PROJECTION;
        } else {
            return new String[]{ITEM_PROJECTION[0], ITEM_PROJECTION[1], ITEM_PROJECTION[2]};
        }
    }

    /**
     * 获取 projection
     * @return projection
     */
    public static String[] getItemSearchProjection() {
        PickerBean data = PickerBean.getInstance();
        if (data.hasAll || data.hasVideo || data.hasAudio) {
            return ITEM_PROJECTION;
        } else {
            return new String[]{ITEM_PROJECTION[0], ITEM_PROJECTION[1], ITEM_PROJECTION[2]};
        }
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
    public static String getSearchSelection() {
        StringBuilder selection = new StringBuilder(MediaStore.MediaColumns.DATA);
        selection.append(" in (");
        Iterator<String> it = PickerBean.getInstance().chooseList.iterator();
        if (it.hasNext()) {
            selection.append("'").append(it.next()).append("'");
            while (it.hasNext()) {
                selection.append(",");
                selection.append("'").append(it.next()).append("'");
            }
        }
        selection.append(")").append(MIN_SIZE);
        return selection.toString();
    }
}
