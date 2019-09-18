package com.chends.media.picker.model;

import android.database.Cursor;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.chends.media.picker.utils.SelectUtil;

/**
 * 文件夹
 * @author chends create on 2019/9/2.
 */
public class FolderBean {

    /**
     * 封面
     */
    private final String mCoverPath;
    /**
     * 封面类型
     */
    private final String mimeType;

    /**
     * 文件夹id
     */
    private final String mId;
    /**
     * 文件夹名称
     */
    private final String mDisplayName;
    /**
     * 总数
     */
    private final int mCount;

    /**
     * @param mCoverPath   封面
     * @param mId          文件夹id
     * @param mDisplayName 显示名称
     * @param mCount       总数
     * @param mimeType     封面类型
     */
    public FolderBean(String mCoverPath, String mimeType, String mId, String mDisplayName, int mCount) {
        this.mCoverPath = mCoverPath;
        this.mimeType = mimeType;
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
        return new FolderBean(cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA)),
                cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)),
                cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID)),
                cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)),
                cursor.getInt(cursor.getColumnIndex(SelectUtil.COLUMN_COUNT)));
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

    public String getMimeType() {
        return mimeType;
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
