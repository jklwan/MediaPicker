package com.chends.media.picker.scaleview.apngdecoder.japng.argb8888;

import net.ellerton.japng.PngScanlineBuffer;
import net.ellerton.japng.chunks.PngAnimationControl;
import net.ellerton.japng.chunks.PngFrameControl;
import net.ellerton.japng.chunks.PngHeader;
import net.ellerton.japng.error.PngException;

/**
 * This will build a single bitmap: the default image within the PNG file.
 * Any animation data (or any other data) will not be processed.
 */
public class DefaultImageArgb8888Director extends BasicArgb8888Director<Argb8888Bitmap> {

    protected Argb8888Bitmap defaultImage;

    @Override
    public void receiveHeader(PngHeader header, PngScanlineBuffer buffer) throws PngException {
        defaultImage = new Argb8888Bitmap(header.width, header.height);
        scanlineProcessor = Argb8888Processors.from(header, buffer, defaultImage);
    }

    @Override
    public boolean wantDefaultImage() {
        return true;
    }

    @Override
    public boolean wantAnimationFrames() {
        return false;
    }

    @Override
    public Argb8888ScanlineProcessor beforeDefaultImage() {
        return scanlineProcessor;
    }

    @Override
    public void receiveDefaultImage(Argb8888Bitmap bitmap) {

    }

    @Override
    public void receiveAnimationControl(PngAnimationControl control) {

    }

    @Override
    public Argb8888ScanlineProcessor receiveFrameControl(PngFrameControl control) {
        return null;
    }

    @Override
    public void receiveFrameImage(Argb8888Bitmap bitmap) {

    }

    @Override
    public Argb8888Bitmap getResult() {
        return defaultImage;
    }
}
