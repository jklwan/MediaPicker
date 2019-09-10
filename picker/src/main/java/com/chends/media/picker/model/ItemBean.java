package com.chends.media.picker.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.TextUtils;

/**
 * 单个文件
 * @author chends create on 2019/9/2.
 */
public class ItemBean implements Parcelable {
    /**
     * id
     */
    private final String mId;
    /**
     * 文件路径
     */
    private final String path;
    /**
     * 文件类型
     */
    private final String mimeType;
    private final long duration;

    private ItemBean(String mId, String path, String mimeType, long duration) {
        this.mId = mId;
        this.path = path;
        this.mimeType = mimeType;
        this.duration = duration;
    }

    /**
     * from cursor
     * @param cursor cursor
     * @return ItemBean
     */
    public static ItemBean valueOf(Cursor cursor) {
        long duration = 0;
        int durationIndex = cursor.getColumnIndex(MediaStore.Video.Media.DURATION);
        if (durationIndex != -1) {
            duration = cursor.getLong(durationIndex);
        }
        return new ItemBean(cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns._ID)),
                cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA)),
                cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)),
                duration);
    }

    public String getId() {
        return mId;
    }

    public String getPath() {
        return path;
    }

    public String getMimeType() {
        return mimeType;
    }

    public long getDuration() {
        return duration;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ItemBean) {
            ItemBean item = (ItemBean) obj;
            return TextUtils.equals(mId, item.getId()) && TextUtils.equals(path, item.getPath());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        if (!TextUtils.isEmpty(mId)) {
            hashCode = 31 * hashCode + mId.hashCode();
        }
        if (!TextUtils.isEmpty(path)) {
            hashCode = 31 * hashCode + path.hashCode();
        }
        return hashCode;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mId);
        dest.writeString(this.path);
        dest.writeString(this.mimeType);
        dest.writeLong(this.duration);
    }

    protected ItemBean(Parcel in) {
        this.mId = in.readString();
        this.path = in.readString();
        this.mimeType = in.readString();
        this.duration = in.readLong();
    }

    public static final Creator<ItemBean> CREATOR = new Creator<ItemBean>() {
        @Override
        public ItemBean createFromParcel(Parcel source) {
            return new ItemBean(source);
        }

        @Override
        public ItemBean[] newArray(int size) {
            return new ItemBean[size];
        }
    };
}
