package com.chends.media.picker.sample;

import com.chends.media.picker.model.Constant;

/**
 * MediaSize
 */
class MediaSize {
    MediaSize() {
    }

    MediaSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    MediaSize(int width, int height, @Constant.ItemType int type) {
        this.width = width;
        this.height = height;
        this.type = type;
    }

    int width;
    int height;
    @Constant.ItemType
    int type;
}
