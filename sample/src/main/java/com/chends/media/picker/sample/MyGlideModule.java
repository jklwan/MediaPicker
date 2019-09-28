package com.chends.media.picker.sample;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.chends.media.picker.sample.audio.AudioCoverLoader;
import com.chends.media.picker.sample.audio.AudioCoverModel;

import java.io.InputStream;

import androidx.annotation.NonNull;

/**
 * @author chends create on 2018/7/9.
 */
@GlideModule
public class MyGlideModule extends AppGlideModule {
    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        //builder.setLogLevel(Log.VERBOSE);
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        registry.append(AudioCoverModel.class, InputStream.class, new AudioCoverLoader.Factory(context));
    }

}
