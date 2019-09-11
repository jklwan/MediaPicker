package com.chends.media.picker.preview.utils;

import com.chends.media.picker.utils.MediaLoader;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

/**
 * @author cds created on 2019/9/11.
 */
public abstract class PreviewMediaLoader extends MediaLoader {

    public void loadImageFull(SubsamplingScaleImageView view, String path, int width, int height, int type) {

    }

    public void loadVideoFull(SubsamplingScaleImageView view, String path, int width, int height) {

    }

    public void loadAudioFull(SubsamplingScaleImageView view, String path, int width, int height) {

    }
}
