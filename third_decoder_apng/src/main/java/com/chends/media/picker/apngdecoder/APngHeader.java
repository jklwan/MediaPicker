package com.chends.media.picker.apngdecoder;

import android.support.annotation.NonNull;

import com.chends.media.picker.decoder.AnimHeader;

/**
 * @author cds created on 2019/9/19.
 */
public class APngHeader extends AnimHeader<APngFrame> {
    /**
     * The bitDepth is the number of bits for each <em>channel</em> of a given pixel.
     * A better name might be "bitsPerPixelChannel" but the name "bitDepth" is used
     * throughout the PNG specification.
     * <p>
     * A truecolour image with a bitDepth of 8 means that the red channel of a pixel
     * has 8 bits (so 256 levels of red), green has 8 bits (256 levels of green), and
     * blue has 8 bits (so 256 levels of green). That means the total bitsPerPixel
     * for that bitmap will be 8+8+8 = 24.
     * <p>
     * A truecolour <em>with alpha</em> image with bitDepth of 8 will be the same
     * except every alpha element of every pixel will have 8 bits (so 256 levels of
     * alpha transparency), meaning that the total bitsPerPixel for that bitmap will
     * be 8+8+8+8=32.
     * <p>
     * A truecolour with alpha image with <em>bitDepth of 16</em> means that each of
     * red, green blue and alpha have 16-bits respectively, meaning that the total
     * bitsPerPixel will be 16+16+16+16 = 64.
     * <p>
     * A greyscale image (no alpha) with bitDepth of 16 has only a grey channel for
     * each pixel, so the bitsPerPixel will also be 16.
     * <p>
     * But a greyscale image <em>with alpha</em> with a bitDepth of 16 has a grey
     * channel and an alpha channel, each with 16 bits so the bitsPerPixel will be
     * 16+16=32.
     * <p>
     * As for palette-based images...
     * <ul>
     * <li>A monochrome image or image with 2 colour palette has bitDepth=1.</li>
     * <li>An image with 4 colour palette has bitDepth=2.</li>
     * <li>An image with 8 colour palette has bitDepth=3.</li>
     * <li>An image with 16 colour palette has bitDepth=4.</li>
     * <li>A greyscale image with 16 levels of gray <em>and an alpha channel</em>
     * has bitDepth=4 and bitsPerPixel=8 because the gray and the alpha channel
     * each have 4 bits.</li>
     * </ul>
     * @see #bitsPerPixel
     */
    public byte bitDepth;
    /**
     * Every PNG image must be exactly one of the standard types as defined by the
     * PNG specification. Better names might have been "imageType" or "imageFormat"
     * but the name "colourType" is used throughout the PNG spec.
     */
    public PngColourType colourType;

    /**
     * Compression type of the file.
     * In practice this is redundant: it may be zip and nothing else.
     */
    public byte compressionMethod;

    /**
     * Filter method used by the file.
     * In practice this is redundant because the filter types are set in the
     * specification and have never been (and never will be) extended.
     */
    public byte filterMethod;

    /**
     * An image is either interlaced or not interlaced.
     * At the time of writing only non-interlaced is supported by this library.
     */
    public byte interlaceMethod;
    /**
     * The number of bits that comprise a single pixel in this bitmap (or every
     * frame if animated). This is distinct from bitDepth.
     * @see #bitDepth
     */
    public int bitsPerPixel;
    public int bytesPerRow;
    public int filterOffset;

    public int idatFirstPosition;
    public int idatLastPosition;
    public boolean hasFcTL;
    public int iendPosition;
    @NonNull
    @Override
    public String toString() {
        return "width:" + width +
                ",height:" + height +
                ",bitDepth:" + bitDepth +
                ",colourType:" + colourType +
                ",compressionMethod:" + compressionMethod +
                ",filterMethod:" + filterMethod +
                ",interlaceMethod:" + interlaceMethod;
    }
}
