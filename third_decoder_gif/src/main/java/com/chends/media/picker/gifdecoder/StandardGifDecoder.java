package com.chends.media.picker.gifdecoder;

/*
 * Copyright (c) 2013 Xcellent Creations, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import android.graphics.Bitmap;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.chends.media.picker.decoder.AnimDecoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Reads frame data from a GIF image source and decodes it into individual frames for animation
 * purposes.  Image data can be read from either and InputStream source or a byte[].
 *
 * <p>This class is optimized for running animations with the frames, there are no methods to get
 * individual frame images, only to decode the next frame in the animation sequence.  Instead, it
 * lowers its memory footprint by only housing the minimum data necessary to decode the next frame
 * in the animation sequence.
 *
 * <p>The animation must be manually moved forward using {@link #advance()} before requesting the
 * next frame.  This method must also be called before you request the first frame or an error
 * will occur.
 *
 * <p>Implementation adapted from sample code published in Lyons. (2004). <em>Java for
 * Programmers</em>, republished under the MIT Open Source License
 * @see <a href="https://www.w3.org/Graphics/GIF/spec-gif89a.txt">GIF 89a Specification</a>
 */
public class StandardGifDecoder implements AnimDecoder<GifHeader> {
    private static final String TAG = StandardGifDecoder.class.getSimpleName();

    /**
     * Maximum pixel stack size for decoding LZW compressed data.
     */
    private static final int MAX_STACK_SIZE = 4 * 1024;

    private static final int NULL_CODE = -1;

    private static final int MASK_INT_LOWEST_BYTE = 0x000000FF;

    @ColorInt
    private static final int COLOR_TRANSPARENT_BLACK = 0x00000000;

    // Global File Header values and parsing flags.
    /**
     * Active color table.
     * Maximum size is 256, see GifHeaderParser.readColorTable
     */
    @ColorInt
    private int[] act;
    /**
     * Private color table that can be modified if needed.
     */
    @ColorInt
    private final int[] pct = new int[256];

    /**
     * Raw GIF data from input source.
     */
    private ByteBuffer rawData;

    /**
     * Raw data read working array.
     */
    private byte[] block;

    private GifHeaderParser parser;

    // LZW decoder working arrays.
    private short[] prefix;
    private byte[] suffix;
    private byte[] pixelStack;
    private byte[] mainPixels;
    @ColorInt
    private int[] mainScratch;

    private int framePointer;
    private GifHeader header;
    private Bitmap previousImage;
    private boolean savePrevious;
    @AnimDecodeStatus
    private int status;
    private int w, h;
    @Nullable
    private Boolean isFirstFrameTransparent;
    @NonNull
    private Bitmap.Config bitmapConfig = Bitmap.Config.ARGB_8888;

    // Public API.
    @SuppressWarnings("unused")
    public StandardGifDecoder(GifHeader gifHeader, ByteBuffer rawData) {
        this();
        setData(gifHeader, rawData);
    }

    public StandardGifDecoder() {
        header = new GifHeader();
    }

    @Override
    public int getWidth() {
        return header.width;
    }

    @Override
    public int getHeight() {
        return header.height;
    }

    @NonNull
    @Override
    public ByteBuffer getData() {
        return rawData;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public void advance() {
        framePointer = (framePointer + 1) % header.frameCount;
    }

    @Override
    public int getDelay(int n) {
        int delay = -1;
        if ((n >= 0) && (n < header.frameCount)) {
            delay = header.frames.get(n).delay;
        }
        return delay;
    }

    @Override
    public int getNextDelay() {
        if (header.frameCount <= 0 || framePointer < 0) {
            return 0;
        }
        return getDelay(framePointer);
    }

    @Override
    public int getFrameCount() {
        return header.frameCount;
    }

    @Override
    public int getCurrentFrameIndex() {
        return framePointer;
    }

    @Override
    public void resetFrameIndex() {
        framePointer = INITIAL_FRAME_POINTER;
    }

    @Deprecated
    public int getLoopCount() {
        if (header.loopCount == GifHeader.NETSCAPE_LOOP_COUNT_DOES_NOT_EXIST) {
            return 1;
        }
        return header.loopCount;
    }

    @Override
    public int getNetscapeLoopCount() {
        return header.loopCount;
    }

    @Override
    public int getTotalIterationCount() {
        if (header.loopCount == GifHeader.NETSCAPE_LOOP_COUNT_DOES_NOT_EXIST) {
            return 1;
        }
        if (header.loopCount == GifHeader.NETSCAPE_LOOP_COUNT_FOREVER) {
            return TOTAL_ITERATION_COUNT_FOREVER;
        }
        return header.loopCount + 1;
    }

    @Override
    public int getByteSize() {
        return rawData.limit() + mainPixels.length + (mainScratch.length * BYTES_PER_INTEGER);
    }

    @Override
    public boolean recycle() {
        return true;
    }

    @Nullable
    @Override
    public synchronized Bitmap getNextFrame() {
        if (header.frameCount <= 0 || framePointer < 0) {
            status = STATUS_FORMAT_ERROR;
        }
        if (status == STATUS_FORMAT_ERROR || status == STATUS_OPEN_ERROR) {
            return null;
        }
        status = STATUS_OK;

        if (block == null) {
            block = new byte[255];
        }

        GifFrame currentFrame = header.frames.get(framePointer);
        GifFrame previousFrame = null;
        int previousIndex = framePointer - 1;
        if (previousIndex >= 0) {
            previousFrame = header.frames.get(previousIndex);
        }

        // Set the appropriate color table.
        act = currentFrame.lct != null ? currentFrame.lct : header.gct;
        if (act == null) {
            // No color table defined.
            status = STATUS_FORMAT_ERROR;
            return null;
        }

        // Reset the transparent pixel in the color table
        if (currentFrame.transparency) {
            // Prepare local copy of color table ("pct = act"), see #1068
            System.arraycopy(act, 0, pct, 0, act.length);
            // Forget about act reference from shared header object, use copied version
            act = pct;
            // Set transparent color if specified.
            act[currentFrame.transIndex] = COLOR_TRANSPARENT_BLACK;
        }

        // Transfer pixel data to image.
        return setPixels(currentFrame, previousFrame);
    }

    @Override
    @AnimDecodeStatus
    public int read(@Nullable InputStream is, int contentLength) {
        if (is != null) {
            try {
                int capacity = (contentLength > 0) ? (contentLength + 4 * 1024) : 16 * 1024;
                ByteArrayOutputStream buffer = new ByteArrayOutputStream(capacity);
                int nRead;
                byte[] data = new byte[16 * 1024];
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                buffer.flush();

                read(buffer.toByteArray());
            } catch (IOException e) {
                Log.w(TAG, "Error reading data from stream", e);
            }
        } else {
            status = STATUS_OPEN_ERROR;
        }

        try {
            if (is != null) {
                is.close();
            }
        } catch (IOException e) {
            Log.w(TAG, "Error closing stream", e);
        }
        return status;
    }

    @Override
    public void clear() {
        prefix = null;
        suffix = null;
        pixelStack = null;
        header = null;
        if (mainPixels != null) {
            mainPixels = null;
        }
        if (mainScratch != null) {
            mainScratch = null;
        }
        if (previousImage != null) {
            if (!previousImage.isRecycled()) {
                previousImage.recycle();
            }
            previousImage = null;
        }
        rawData = null;
        isFirstFrameTransparent = null;
        if (block != null) {
            block = null;
        }
        if (parser != null) {
            parser.clear();
            parser = null;
        }
        act = null;
    }

    @Override
    public void setData(@NonNull GifHeader header, @NonNull byte[] data) {
        setData(header, ByteBuffer.wrap(data));
    }

    @Override
    public synchronized void setData(@NonNull GifHeader header, @NonNull ByteBuffer buffer) {
        this.status = STATUS_OK;
        this.header = header;
        framePointer = INITIAL_FRAME_POINTER;
        // Initialize the raw data buffer.
        rawData = buffer.asReadOnlyBuffer();
        rawData.position(0);
        rawData.order(ByteOrder.LITTLE_ENDIAN);

        // No point in specially saving an old frame if we're never going to use it.
        savePrevious = false;
        for (GifFrame frame : header.frames) {
            if (frame.dispose == GifFrame.DISPOSAL_PREVIOUS) {
                savePrevious = true;
                break;
            }
        }

        w = header.width;
        h = header.height;
        // Now that we know the size, init scratch arrays.
        // TODO Find a way to avoid this entirely or at least downsample it (either should be possible).
        mainPixels = new byte[w * h];
        mainScratch = new int[w * h];
    }


    @NonNull
    private GifHeaderParser getHeaderParser() {
        if (parser == null) {
            parser = new GifHeaderParser();
        }
        return parser;
    }

    @Override
    @AnimDecodeStatus
    public synchronized int read(@Nullable byte[] data) {
        this.header = getHeaderParser().setData(data).parseHeader();
        if (data != null) {
            setData(header, data);
        }

        return status;
    }

    @Override
    public void setDefaultBitmapConfig(@NonNull Bitmap.Config config) {
        if (config != Bitmap.Config.ARGB_8888 && config != Bitmap.Config.RGB_565) {
            throw new IllegalArgumentException("Unsupported format: " + config
                    + ", must be one of " + Bitmap.Config.ARGB_8888 + " or " + Bitmap.Config.RGB_565);
        }

        bitmapConfig = config;
    }

    /**
     * Creates new frame image from current data (and previous frames as specified by their
     * disposition codes).
     */
    private Bitmap setPixels(GifFrame currentFrame, GifFrame previousFrame) {
        // Final location of blended pixels.
        final int[] dest = mainScratch;

        // clear all pixels when meet first frame and drop prev image from last loop
        if (previousFrame == null) {
            if (previousImage != null) {
                if (!previousImage.isRecycled()) {
                    previousImage.recycle();
                }
            }
            previousImage = null;
            Arrays.fill(dest, COLOR_TRANSPARENT_BLACK);
        }

        // clear all pixels when dispose is 3 but previousImage is null.
        // When DISPOSAL_PREVIOUS and previousImage didn't be set, new frame should draw on
        // a empty image
        if (previousFrame != null && previousFrame.dispose == GifFrame.DISPOSAL_PREVIOUS
                && previousImage == null) {
            Arrays.fill(dest, COLOR_TRANSPARENT_BLACK);
        }

        // fill in starting image contents based on last image's dispose code
        if (previousFrame != null && previousFrame.dispose > GifFrame.DISPOSAL_UNSPECIFIED) {
            // We don't need to do anything for DISPOSAL_NONE, if it has the correct pixels so will our
            // mainScratch and therefore so will our dest array.
            if (previousFrame.dispose == GifFrame.DISPOSAL_BACKGROUND) {
                // Start with a canvas filled with the background color
                @ColorInt int c = COLOR_TRANSPARENT_BLACK;
                if (!currentFrame.transparency) {
                    c = header.bgColor;
                    if (currentFrame.lct != null && header.bgIndex == currentFrame.transIndex) {
                        c = COLOR_TRANSPARENT_BLACK;
                    }
                } else if (framePointer == 0) {
                    // TODO: We should check and see if all individual pixels are replaced. If they are, the
                    // first frame isn't actually transparent. For now, it's simpler and safer to assume
                    // drawing a transparent background means the GIF contains transparency.
                    isFirstFrameTransparent = true;
                }
                // The area used by the graphic must be restored to the background color.
                int downsampledIH = previousFrame.ih;
                int downsampledIY = previousFrame.iy;
                int downsampledIW = previousFrame.iw;
                int downsampledIX = previousFrame.ix;
                int topLeft = downsampledIY * w + downsampledIX;
                int bottomLeft = topLeft + downsampledIH * w;
                for (int left = topLeft; left < bottomLeft; left += w) {
                    int right = left + downsampledIW;
                    for (int pointer = left; pointer < right; pointer++) {
                        dest[pointer] = c;
                    }
                }
            } else if (previousFrame.dispose == GifFrame.DISPOSAL_PREVIOUS && previousImage != null) {
                // Start with the previous frame
                previousImage.getPixels(dest, 0, w, 0, 0, w,
                        h);
            }
        }

        // Decode pixels for this frame into the global pixels[] scratch.
        decodeBitmapData(currentFrame);

        if (currentFrame.interlace) {
            copyCopyIntoScratchRobust(currentFrame);
        } else {
            copyIntoScratchFast(currentFrame);
        }

        // Copy pixels into previous image
        if (savePrevious && (currentFrame.dispose == GifFrame.DISPOSAL_UNSPECIFIED
                || currentFrame.dispose == GifFrame.DISPOSAL_NONE)) {
            if (previousImage == null) {
                previousImage = getNextBitmap();
            }
            previousImage.setPixels(dest, 0, w, 0, 0, w,
                    h);
        }

        // Set pixels for current image.
        Bitmap result = getNextBitmap();
        result.setPixels(dest, 0, w, 0, 0, w, h);
        return result;
    }

    private void copyIntoScratchFast(GifFrame currentFrame) {
        int[] dest = mainScratch;
        int downsampledIH = currentFrame.ih;
        int downsampledIY = currentFrame.iy;
        int downsampledIW = currentFrame.iw;
        int downsampledIX = currentFrame.ix;
        // Copy each source line to the appropriate place in the destination.
        boolean isFirstFrame = framePointer == 0;
        int width = this.w;
        byte[] mainPixels = this.mainPixels;
        int[] act = this.act;
        byte transparentColorIndex = -1;
        for (int i = 0; i < downsampledIH; i++) {
            int line = i + downsampledIY;
            int k = line * width;
            // Start of line in dest.
            int dx = k + downsampledIX;
            // End of dest line.
            int dlim = dx + downsampledIW;
            if (k + width < dlim) {
                // Past dest edge.
                dlim = k + width;
            }
            // Start of line in source.
            int sx = i * currentFrame.iw;

            while (dx < dlim) {
                byte byteCurrentColorIndex = mainPixels[sx];
                int currentColorIndex = ((int) byteCurrentColorIndex) & MASK_INT_LOWEST_BYTE;
                if (currentColorIndex != transparentColorIndex) {
                    int color = act[currentColorIndex];
                    if (color != COLOR_TRANSPARENT_BLACK) {
                        dest[dx] = color;
                    } else {
                        transparentColorIndex = byteCurrentColorIndex;
                    }
                }
                ++sx;
                ++dx;
            }
        }

        isFirstFrameTransparent =
                isFirstFrameTransparent == null && isFirstFrame && transparentColorIndex != -1;
    }

    private void copyCopyIntoScratchRobust(GifFrame currentFrame) {
        int[] dest = mainScratch;
        int downsampledIH = currentFrame.ih;
        int downsampledIY = currentFrame.iy;
        int downsampledIW = currentFrame.iw;
        int downsampledIX = currentFrame.ix;
        // Copy each source line to the appropriate place in the destination.
        int pass = 1;
        int inc = 8;
        int iline = 0;
        boolean isFirstFrame = framePointer == 0;
        int sampleSize = 1;
        int downsampledWidth = this.w;
        int downsampledHeight = this.h;
        byte[] mainPixels = this.mainPixels;
        int[] act = this.act;
        @Nullable
        Boolean isFirstFrameTransparent = this.isFirstFrameTransparent;
        for (int i = 0; i < downsampledIH; i++) {
            int line = i;
            if (currentFrame.interlace) {
                if (iline >= downsampledIH) {
                    pass++;
                    switch (pass) {
                        case 2:
                            iline = 4;
                            break;
                        case 3:
                            iline = 2;
                            inc = 4;
                            break;
                        case 4:
                            iline = 1;
                            inc = 2;
                            break;
                        default:
                            break;
                    }
                }
                line = iline;
                iline += inc;
            }
            line += downsampledIY;
            if (line < downsampledHeight) {
                int k = line * downsampledWidth;
                // Start of line in dest.
                int dx = k + downsampledIX;
                // End of dest line.
                int dlim = dx + downsampledIW;
                if (k + downsampledWidth < dlim) {
                    // Past dest edge.
                    dlim = k + downsampledWidth;
                }
                // Start of line in source.
                int sx = i * sampleSize * currentFrame.iw;
                int averageColor;
                while (dx < dlim) {
                    int currentColorIndex = ((int) mainPixels[sx]) & MASK_INT_LOWEST_BYTE;
                    averageColor = act[currentColorIndex];
                    if (averageColor != COLOR_TRANSPARENT_BLACK) {
                        dest[dx] = averageColor;
                    } else if (isFirstFrame && isFirstFrameTransparent == null) {
                        isFirstFrameTransparent = true;
                    }
                    sx += sampleSize;
                    dx++;
                }
            }
        }

        if (this.isFirstFrameTransparent == null) {
            this.isFirstFrameTransparent = isFirstFrameTransparent == null
                    ? false : isFirstFrameTransparent;
        }
    }

    /*@ColorInt
    private int averageColorsNear(int positionInMainPixels, int maxPositionInMainPixels,
                                  int currentFrameIw) {
        int alphaSum = 0;
        int redSum = 0;
        int greenSum = 0;
        int blueSum = 0;

        int totalAdded = 0;
        // Find the pixels in the current row.
        for (int i = positionInMainPixels;
             i < positionInMainPixels + 1 && i < mainPixels.length
                     && i < maxPositionInMainPixels; i++) {
            int currentColorIndex = ((int) mainPixels[i]) & MASK_INT_LOWEST_BYTE;
            int currentColor = act[currentColorIndex];
            if (currentColor != 0) {
                alphaSum += currentColor >> 24 & MASK_INT_LOWEST_BYTE;
                redSum += currentColor >> 16 & MASK_INT_LOWEST_BYTE;
                greenSum += currentColor >> 8 & MASK_INT_LOWEST_BYTE;
                blueSum += currentColor & MASK_INT_LOWEST_BYTE;
                totalAdded++;
            }
        }
        // Find the pixels in the next row.
        for (int i = positionInMainPixels + currentFrameIw;
             i < positionInMainPixels + currentFrameIw + 1 && i < mainPixels.length
                     && i < maxPositionInMainPixels; i++) {
            int currentColorIndex = ((int) mainPixels[i]) & MASK_INT_LOWEST_BYTE;
            int currentColor = act[currentColorIndex];
            if (currentColor != 0) {
                alphaSum += currentColor >> 24 & MASK_INT_LOWEST_BYTE;
                redSum += currentColor >> 16 & MASK_INT_LOWEST_BYTE;
                greenSum += currentColor >> 8 & MASK_INT_LOWEST_BYTE;
                blueSum += currentColor & MASK_INT_LOWEST_BYTE;
                totalAdded++;
            }
        }
        if (totalAdded == 0) {
            return COLOR_TRANSPARENT_BLACK;
        } else {
            return ((alphaSum / totalAdded) << 24)
                    | ((redSum / totalAdded) << 16)
                    | ((greenSum / totalAdded) << 8)
                    | (blueSum / totalAdded);
        }
    }*/

    /**
     * Decodes LZW image data into pixel array. Adapted from John Cristy's BitmapMagick.
     */
    private void decodeBitmapData(GifFrame frame) {
        if (frame != null) {
            // Jump to the frame start position.
            rawData.position(frame.bufferFrameStart);
        }

        int npix = (frame == null) ? header.width * header.height : frame.iw * frame.ih;
        int available, clear, codeMask, codeSize, endOfInformation, inCode, oldCode, bits, code, count,
                i, datum, dataSize, first, top, bi, pi;

        if (mainPixels == null || mainPixels.length < npix) {
            // Allocate new pixel array.
            mainPixels = new byte[npix];
        }
        byte[] mainPixels = this.mainPixels;
        if (prefix == null) {
            prefix = new short[MAX_STACK_SIZE];
        }
        short[] prefix = this.prefix;
        if (suffix == null) {
            suffix = new byte[MAX_STACK_SIZE];
        }
        byte[] suffix = this.suffix;
        if (pixelStack == null) {
            pixelStack = new byte[MAX_STACK_SIZE + 1];
        }
        byte[] pixelStack = this.pixelStack;

        // Initialize GIF data stream decoder.
        dataSize = readByte();
        clear = 1 << dataSize;
        endOfInformation = clear + 1;
        available = clear + 2;
        oldCode = NULL_CODE;
        codeSize = dataSize + 1;
        codeMask = (1 << codeSize) - 1;

        for (code = 0; code < clear; code++) {
            // XXX ArrayIndexOutOfBoundsException.
            prefix[code] = 0;
            suffix[code] = (byte) code;
        }
        byte[] block = this.block;
        // Decode GIF pixel stream.
        i = datum = bits = count = first = top = pi = bi = 0;
        while (i < npix) {
            // Read a new data block.
            if (count == 0) {
                count = readBlock();
                if (count <= 0) {
                    status = STATUS_PARTIAL_DECODE;
                    break;
                }
                bi = 0;
            }

            datum += (((int) block[bi]) & MASK_INT_LOWEST_BYTE) << bits;
            bits += 8;
            ++bi;
            --count;

            while (bits >= codeSize) {
                // Get the next code.
                code = datum & codeMask;
                datum >>= codeSize;
                bits -= codeSize;

                // Interpret the code.
                if (code == clear) {
                    // Reset decoder.
                    codeSize = dataSize + 1;
                    codeMask = (1 << codeSize) - 1;
                    available = clear + 2;
                    oldCode = NULL_CODE;
                    continue;
                } else if (code == endOfInformation) {
                    break;
                } else if (oldCode == NULL_CODE) {
                    mainPixels[pi] = suffix[code];
                    ++pi;
                    ++i;
                    oldCode = code;
                    first = code;
                    continue;
                }

                inCode = code;
                if (code >= available) {
                    pixelStack[top] = (byte) first;
                    ++top;
                    code = oldCode;
                }

                while (code >= clear) {
                    pixelStack[top] = suffix[code];
                    ++top;
                    code = prefix[code];
                }
                first = ((int) suffix[code]) & MASK_INT_LOWEST_BYTE;

                mainPixels[pi] = (byte) first;
                ++pi;
                ++i;

                while (top > 0) {
                    // Pop a pixel off the pixel stack.
                    mainPixels[pi] = pixelStack[--top];
                    ++pi;
                    ++i;
                }

                // Add a new string to the string table.
                if (available < MAX_STACK_SIZE) {
                    prefix[available] = (short) oldCode;
                    suffix[available] = (byte) first;
                    ++available;
                    if (((available & codeMask) == 0) && (available < MAX_STACK_SIZE)) {
                        ++codeSize;
                        codeMask += available;
                    }
                }
                oldCode = inCode;
            }
        }

        // Clear missing pixels.
        Arrays.fill(mainPixels, pi, npix, (byte) COLOR_TRANSPARENT_BLACK);
    }

    /**
     * Reads a single byte from the input stream.
     */
    private int readByte() {
        return rawData.get() & MASK_INT_LOWEST_BYTE;
    }

    /**
     * Reads next variable length block from input.
     * @return number of bytes stored in "buffer".
     */
    private int readBlock() {
        int blockSize = readByte();
        if (blockSize <= 0) {
            return blockSize;
        }
        rawData.get(block, 0, Math.min(blockSize, rawData.remaining()));
        return blockSize;
    }

    private Bitmap getNextBitmap() {
        Bitmap.Config config = isFirstFrameTransparent == null || isFirstFrameTransparent
                ? Bitmap.Config.ARGB_8888 : bitmapConfig;
        Bitmap result = Bitmap.createBitmap(w, h, config);
        result.setHasAlpha(true);
        return result;
    }
}

