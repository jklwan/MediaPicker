package com.chends.media.picker.scaleview;

import android.graphics.PointF;

import java.io.Serializable;

import androidx.annotation.NonNull;

/**
 * Wraps the scale, center and orientation of a displayed image for easy restoration on screen rotate.
 */
public class ImageViewState implements Serializable {

    private final float scale;

    private final float centerX;

    private final float centerY;

    private final int orientation;

    public ImageViewState(float scale, @NonNull PointF center, int orientation) {
        this.scale = scale;
        this.centerX = center.x;
        this.centerY = center.y;
        this.orientation = orientation;
    }

    public float getScale() {
        return scale;
    }

    @NonNull
    public PointF getCenter() {
        return new PointF(centerX, centerY);
    }

    public int getOrientation() {
        return orientation;
    }

}
