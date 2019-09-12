package com.chends.media.picker.sample.audio;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.chends.media.picker.sample.util.FileUtil;
import com.chends.media.picker.sample.util.LogUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author chends create on 2019/9/10.
 */
public class AudioCoverFetcher implements DataFetcher<InputStream> {

    private final AudioCoverModel model;
    private InputStream stream;
    private final Context context;

    AudioCoverFetcher(AudioCoverModel model, Context context) {
        this.model = model;
        this.context = context;
    }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataFetcher.DataCallback<? super InputStream> callback) {
        String thumbnail = FileUtil.getAudioThumbnail(context, model.getPath());
        if (!TextUtils.isEmpty(thumbnail)) {
            try {
                File file = new File(thumbnail);
                if (file.exists() && file.length() > 0) {
                    stream = new FileInputStream(thumbnail);
                    callback.onDataReady(stream);
                    return;
                }
            } catch (Exception e) {
                LogUtil.d("getAudioThumbnail", "path is not null:" + e.getMessage() + ", path:" + model.getPath());
            }
        }
        MediaMetadataRetriever retriever = null;
        try {
            retriever = new MediaMetadataRetriever();
            retriever.setDataSource(model.getPath());
            byte[] picture = retriever.getEmbeddedPicture();
            if (null != picture) {
                stream = new ByteArrayInputStream(picture);
                callback.onDataReady(stream);
            } else {
                callback.onLoadFailed(new FileNotFoundException());
                LogUtil.d("getEmbeddedPicture", "is null, path:" + model.getPath());
            }
        } catch (Exception e) {
            callback.onLoadFailed(e);
            LogUtil.d("onLoadFailed", e.getMessage() + ", path:" + model.getPath());
        } finally {
            if (retriever != null) {
                retriever.release();
            }
        }
    }

    @Override
    public void cleanup() {
        try {
            if (null != stream) {
                stream.close();
            }
        } catch (IOException ignore) {
        }
    }

    @Override
    public void cancel() {
        // cannot cancel
    }


    @NonNull
    @Override
    public Class<InputStream> getDataClass() {
        return InputStream.class;
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
        return DataSource.LOCAL;
    }

}
