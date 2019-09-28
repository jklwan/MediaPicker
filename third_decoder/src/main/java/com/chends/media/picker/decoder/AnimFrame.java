package com.chends.media.picker.decoder;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

/**
 * Inner model class housing metadata for each frame.
 */
public class AnimFrame {
    /**
     * Anim Disposal Method meaning take no action.
     * <p><b>Anim</b>: <i>No disposal specified.
     * The decoder is not required to take any action.</i></p>
     */
    public static final int DISPOSAL_UNSPECIFIED = 0;
    /**
     * Anim Disposal Method meaning leave canvas from previous frame.
     * <p><b>Anim</b>: <i>Do not dispose.
     * The graphic is to be left in place.</i></p>
     */
    public static final int DISPOSAL_NONE = 1;
    /**
     * Anim Disposal Method meaning clear canvas to background color.
     * <p><b>Anim</b>: <i>Restore to background color.
     * The area used by the graphic must be restored to the background color.</i></p>
     */
    public static final int DISPOSAL_BACKGROUND = 2;
    /**
     * Anim Disposal Method meaning clear canvas to frame before last.
     * <p><b>Anim</b>: <i>Restore to previous.
     * The decoder is required to restore the area overwritten by the graphic
     * with what was there prior to rendering the graphic.</i></p>
     */
    public static final int DISPOSAL_PREVIOUS = 3;

    /**
     * <p><b>Anim</b>:
     * <i>Indicates the way in which the graphic is to be treated after being displayed.</i></p>
     * Disposal methods 0-3 are defined, 4-7 are reserved for future use.
     * @see #DISPOSAL_UNSPECIFIED
     * @see #DISPOSAL_NONE
     * @see #DISPOSAL_BACKGROUND
     * @see #DISPOSAL_PREVIOUS
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(value = {DISPOSAL_UNSPECIFIED, DISPOSAL_NONE, DISPOSAL_BACKGROUND, DISPOSAL_PREVIOUS})
    public @interface AnimDisposalMethod {
    }

    /**
     * Disposal Method.
     */
    @AnimDisposalMethod
    public int dispose;
    /**
     * Delay, in milliseconds, to next frame.
     */
    public int delay;
    /**
     * Index in the raw buffer where we need to start reading to decode.
     */
    public int bufferFrameStart;
}
