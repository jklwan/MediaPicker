package com.chends.media.picker.utils;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.chends.media.picker.BuildConfig;
import com.chends.media.picker.listener.FolderLoaderCallback;
import com.chends.media.picker.loader.FolderLoader;
import com.chends.media.picker.model.Constant;
import com.chends.media.picker.model.FolderBean;
import com.chends.media.picker.model.PickerBean;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chends create on 2019/9/5.
 */
public class FolderLoaderUtil implements LoaderManager.LoaderCallbacks<Cursor> {
    private final int LOADER_ID = 1;
    private Context context;
    private LoaderManager manager;
    private FolderLoaderCallback mCallback;
    private int mCurrentSelection;

    FolderLoaderUtil(AppCompatActivity activity, FolderLoaderCallback callback) {
        context = activity;
        this.mCallback = callback;
        manager = activity.getSupportLoaderManager();
        LoaderManager.enableDebugLogging(BuildConfig.DEBUG);
    }

    void startLoader() {
        manager.restartLoader(LOADER_ID, null, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return FolderLoader.newInstance(context);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        List<FolderBean> list = new ArrayList<>();
        if (cursor != null && cursor.getCount() > 0) {
            PickerBean data = PickerBean.getInstance();
            int totalCount = 0, videoCount = 0, audioCount = 0, coverType = Constant.TYPE_IMAGE;
            String coverPath = "", imageCover = "", videoCover = "", audioCover = "";
            if (data.hasAll) {
                // 混合查询
                while (cursor.moveToNext()) {
                    FolderBean folder = FolderBean.singleOf(cursor);
                    if (TextUtils.isEmpty(coverPath)) {
                        coverPath = folder.getCoverPath();
                        coverType = folder.getCoverType();
                    }
                    totalCount += folder.getCount();
                    int vCount = 0, aCount = 0;
                    if (data.hasVideo) {
                        vCount = cursor.getInt(cursor.getColumnIndex(SelectUtil.VIDEO_COUNT));
                        videoCount += vCount;
                        if (vCount > 0 && TextUtils.isEmpty(videoCover)) {
                            videoCover = cursor.getString(cursor.getColumnIndex(SelectUtil.VIDEO_COVER));
                        }
                    }
                    if (data.hasAudio) {
                        aCount = cursor.getInt(cursor.getColumnIndex(SelectUtil.AUDIO_COUNT));
                        audioCount += aCount;
                        if (aCount > 0 && TextUtils.isEmpty(audioCover)) {
                            audioCover = cursor.getString(cursor.getColumnIndex(SelectUtil.AUDIO_COVER));
                        }
                    }

                    int count = folder.getCount() - vCount - aCount;
                    if (data.hasImage && count > 0) {
                        imageCover = cursor.getString(cursor.getColumnIndex(SelectUtil.IMAGE_COVER));
                        list.add(FolderBean.valueOfTypeCount(cursor, imageCover, Constant.TYPE_IMAGE, count));
                    }
                }
                if (data.hasAudio) {
                    // 添加：所有音频
                    FolderBean allAudio = new FolderBean(audioCover, Constant.TYPE_AUDIO,
                            Constant.Folder_Id_All_Audio, Constant.Folder_Name_All_Audio, audioCount);
                    list.add(0, allAudio);
                }
                if (data.hasVideo) {
                    // 添加：所有视频
                    FolderBean allVideo = new FolderBean(videoCover, Constant.TYPE_VIDEO,
                            Constant.Folder_Id_All_Video, Constant.Folder_Name_All_Video, videoCount);
                    list.add(0, allVideo);
                }
                // 不添加所有图片！
                // 添加：所有文件
                FolderBean all = new FolderBean(coverPath, coverType,
                        Constant.Folder_Id_All, Constant.Folder_Name_All, totalCount);
                list.add(0, all);
            } else {
                // 单独类型查询
                String allId, allName;
                if (data.hasImage) {
                    // 图片查询
                    allId = Constant.Folder_Id_All_Image;
                    allName = Constant.Folder_Name_All_Image;
                    //coverType = Constant.TYPE_IMAGE;
                } else if (data.hasVideo) {
                    // 视频查询
                    allId = Constant.Folder_Id_All_Video;
                    allName = Constant.Folder_Name_All_Video;
                    //coverType = Constant.TYPE_VIDEO;
                } else if (data.hasAudio) {
                    // 音频查询
                    allId = Constant.Folder_Id_All_Audio;
                    allName = Constant.Folder_Name_All_Audio;
                    //coverType = Constant.TYPE_AUDIO;
                } else {
                    throw new IllegalArgumentException("unknown select type");
                }
                while (cursor.moveToNext()) {
                    FolderBean folder = FolderBean.singleOf(cursor);
                    totalCount += folder.getCount();
                    if (TextUtils.isEmpty(coverPath)) {
                        coverPath = folder.getCoverPath();
                        coverType = folder.getCoverType();
                    }
                    if (data.hasImage) {
                        list.add(folder);
                    }
                }
                FolderBean all = new FolderBean(coverPath, coverType, allId, allName, totalCount);
                list.add(0, all);
            }
        }
        mCallback.onFolderLoaderFinish(list);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if (mCallback != null) {
            mCallback.onFolderLoaderReset();
        }
    }

    /**
     * 当前选择
     * @return selection
     */
    int getCurrentSelection() {
        return mCurrentSelection;
    }

    void setStateCurrentSelection(int currentSelection) {
        mCurrentSelection = currentSelection;
    }

    void onRestoreInstanceState(Bundle bundle) {
        if (bundle == null) {
            return;
        }
        mCurrentSelection = bundle.getInt(Constant.STATE_CURRENT_SELECTION);
    }

    void onSaveInstanceState(Bundle outState) {
        outState.putInt(Constant.STATE_CURRENT_SELECTION, mCurrentSelection);
    }

    void onDestroy() {
        manager.destroyLoader(LOADER_ID);
        mCallback = null;
    }
}
