package com.chends.media.picker.sample.audio;

import android.text.TextUtils;

/**
 * @author chends create on 2019/9/10.
 */
public class AudioCoverModel {
    private final String path;

    public AudioCoverModel(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    @Override
    public int hashCode() {
        int code = 1;
        if (!TextUtils.isEmpty(path)){
            code += path.hashCode();
        }
        return code;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AudioCoverModel) {
            AudioCoverModel item = (AudioCoverModel) obj;
            return TextUtils.equals(item.path, path);
        }
        return false;
    }
}
