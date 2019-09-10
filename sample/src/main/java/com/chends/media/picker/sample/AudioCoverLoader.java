package com.chends.media.picker.sample;

import android.content.Context;
import android.support.annotation.NonNull;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.signature.ObjectKey;

import java.io.InputStream;

/**
 * @author chends create on 2019/9/10.
 */
public class AudioCoverLoader implements ModelLoader<AudioCoverModel, InputStream> {
    private final Context context;

    public AudioCoverLoader(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public LoadData<InputStream> buildLoadData(@NonNull AudioCoverModel coverModel, int width, int height, @NonNull Options options) {
        return new LoadData<>(new ObjectKey(coverModel), new AudioCoverFetcher(coverModel, context));
    }

    @Override
    public boolean handles(@NonNull AudioCoverModel AudioCoverModel) {
        return true;
    }

    static class Factory implements ModelLoaderFactory<AudioCoverModel, InputStream> {
        private final Context context;

        public Factory(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public ModelLoader<AudioCoverModel, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new AudioCoverLoader(context);
        }

        @Override
        public void teardown() {

        }
    }

}
