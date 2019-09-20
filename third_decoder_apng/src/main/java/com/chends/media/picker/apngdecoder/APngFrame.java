package com.chends.media.picker.apngdecoder;

import android.support.annotation.IntDef;

import com.chends.media.picker.decoder.AnimFrame;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A PngFrameControl object contains data parsed from the ``fcTL`` chunk data
 * in an animated PNG File.
 * <p>
 * See https://wiki.mozilla.org/APNG_Specification#.60fcTL.60:_The_Frame_Control_Chunk
 * <pre>
 *     0    sequence_number       (unsigned int)   Sequence number of the animation chunk, starting from 0
 *     4    width                 (unsigned int)   Width of the following frame
 *     8    height                (unsigned int)   Height of the following frame
 *    12    x_offset              (unsigned int)   X position at which to render the following frame
 *    16    y_offset              (unsigned int)   Y position at which to render the following frame
 *    20    delay_num             (unsigned short) Frame delay fraction numerator
 *    22    delay_den             (unsigned short) Frame delay fraction denominator
 *    24    dispose_op            (byte)           Type of frame area disposal to be done after rendering this frame
 *    25    blend_op              (byte)           Type of frame area rendering for this frame
 * </pre>
 * <p>
 * Delay denominator: from spec, "if denominator is zero it should be treated as 100ths of second".
 * <pre>
 * dispose op:
 *    value
 *   0           APNG_DISPOSE_OP_NONE
 *   1           APNG_DISPOSE_OP_BACKGROUND
 *   2           APNG_DISPOSE_OP_PREVIOUS
 *
 * blend op:
 *  value
 *   0       APNG_BLEND_OP_SOURCE
 *   1       APNG_BLEND_OP_OVER
 * </pre>
 */
public class APngFrame extends AnimFrame {
    public int sequenceNumber;
    public int width;
    public int height;
    public int xOffset;
    public int yOffset;
    //public short delayNumerator;
    //public short delayDenominator;
    @AnimDisposalMethod
    public int disposeOp;
    @APngBlendMethod
    public int blendOp;

    public static final int APNG_BLEND_OP_SOURCE = 0;
    public static final int APNG_BLEND_OP_OVER = 1;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(value = {APNG_BLEND_OP_SOURCE, APNG_BLEND_OP_OVER})
    public @interface APngBlendMethod {
    }

    public void setDisposeOp(byte disposeOp) {
        // todo
        switch (disposeOp) {
            case 0:
                this.disposeOp = DISPOSAL_NONE;
                break;
            case 1:
                this.disposeOp = DISPOSAL_BACKGROUND;
                break;
            case 2:
                this.disposeOp = DISPOSAL_PREVIOUS;
                break;
            default:
                this.disposeOp = DISPOSAL_UNSPECIFIED;
                break;
        }
    }

    public void setBlendOp(byte blendOp) {
        // todo
        switch (blendOp) {
            case 0:
                this.blendOp = APNG_BLEND_OP_SOURCE;
                break;
            case 1:
                this.blendOp = APNG_BLEND_OP_OVER;
                break;
        }
    }

    public void setDelay(short delayNumerator, short delayDenominator) {
        if (delayDenominator == 1000) {
            delay = delayNumerator;
        } else {
            if (delayDenominator == 0) {
                delayDenominator = 100;
            }
            // if denom is 100 then need to multiple by 10
            delay = (int) ((delayNumerator * 1000f) / delayDenominator);
        }
    }

}
