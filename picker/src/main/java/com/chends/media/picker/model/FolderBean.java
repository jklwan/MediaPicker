package com.chends.media.picker.model;

import android.database.Cursor;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.chends.media.picker.MimeType;
import com.chends.media.picker.utils.SelectUtil;

/**
 * 文件夹
 * @author chends create on 2019/9/2.
 */
public class FolderBean {

    /**
     * 封面
     */
    private String mCoverPath;
    /**
     * 封面图片类型（图片，音视频）
     */
    @Constant.ItemType
    private int coverType;
    /**
     * 文件夹id
     */
    private String mId;
    /**
     * 文件夹名称
     */
    private String mDisplayName;
    /**
     * 总数
     */
    private int mCount;

    /**
     * @param mCoverPath   封面
     * @param coverType    封面类型
     * @param mId          文件夹id
     * @param mDisplayName 显示名称
     * @param mCount       总数
     */
    public FolderBean(String mCoverPath, int coverType, String mId, String mDisplayName, int mCount) {
        this.mCoverPath = mCoverPath;
        this.coverType = coverType;
        this.mId = mId;
        this.mDisplayName = mDisplayName;
        this.mCount = mCount;
    }


    /**
     * 单类型
     * @param cursor cursor
     * @return folderBean
     */
    public static FolderBean singleOf(Cursor cursor) {
        String mimeType = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE));
        return new FolderBean(cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA)),
                MimeType.getItemType(mimeType),
                cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID)),
                cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)),
                cursor.getInt(cursor.getColumnIndex(SelectUtil.COLUMN_COUNT)));
    }

    /**
     * folderBean
     * @param cursor    cursor
     * @param coverPath 封面
     * @param coverType 封面类型
     * @param count     总数
     * @return FolderBean
     */
    public static FolderBean valueOfTypeCount(Cursor cursor, String coverPath, int coverType, int count) {
        return new FolderBean(coverPath, coverType,
                cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID)),
                cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)),
                count);
    }

    public int getCount() {
        return mCount;
    }

    public String getCoverPath() {
        return mCoverPath;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public String getId() {
        return mId;
    }

    @Constant.ItemType
    public int getCoverType() {
        return coverType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FolderBean) {
            FolderBean item = (FolderBean) obj;
            return TextUtils.equals(mId, item.mId) && TextUtils.equals(mCoverPath, item.mCoverPath);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        if (!TextUtils.isEmpty(mId)) {
            hashCode = 31 * hashCode + mId.hashCode();
        }
        if (!TextUtils.isEmpty(mCoverPath)) {
            hashCode = 31 * hashCode + mCoverPath.hashCode();
        }
        return hashCode;
    }
}
