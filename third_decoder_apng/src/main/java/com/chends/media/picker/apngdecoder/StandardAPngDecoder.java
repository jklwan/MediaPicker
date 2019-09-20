package com.chends.media.picker.apngdecoder;

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

/**
 * @author cds created on 2019/9/19.
 */
public class StandardAPngDecoder implements AnimDecoder<APngHeader> {
    private static final String TAG = StandardAPngDecoder.class.getSimpleName();

    private ByteBuffer rawData;
    private APngHeaderParser parser;
    private APngHeader header;
    private int framePointer;
    @AnimDecodeStatus
    private int status;
    private boolean savePrevious = false;
    private int sampleSize;
    private int downsampledWidth;
    private int downsampledHeight;
    private byte[] pixelStack;
    private byte[] mainPixels;
    @ColorInt
    private int[] mainScratch;
    @NonNull
    private Bitmap.Config bitmapConfig = Bitmap.Config.ARGB_8888;

    public StandardAPngDecoder() {
        parser = new APngHeaderParser();
    }

    @Override
    public int getWidth() {
        return parser.parseHeader().width;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @NonNull
    @Override
    public ByteBuffer getData() {
        return null;
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

    @Override
    public int getNetscapeLoopCount() {
        return header.loopCount;
    }

    @Override
    public int getTotalIterationCount() {
        if (header.loopCount == APngHeader.NETSCAPE_LOOP_COUNT_DOES_NOT_EXIST) {
            return 1;
        }
        if (header.loopCount == APngHeader.NETSCAPE_LOOP_COUNT_FOREVER) {
            return TOTAL_ITERATION_COUNT_FOREVER;
        }
        return header.loopCount + 1;
    }

    @Override
    public int getByteSize() {
        return rawData.limit() + mainPixels.length + (mainScratch.length * BYTES_PER_INTEGER);
    }

    @Nullable
    @Override
    public Bitmap getNextFrame() {
        return null;
    }

    @Override
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
        if (parser != null) {
            parser.clear();
            parser = null;
        }
    }

    @Override
    public void setData(@NonNull APngHeader header, @NonNull byte[] data) {
        setData(header, ByteBuffer.wrap(data));
    }

    @Override
    public void setData(@NonNull APngHeader header, @NonNull ByteBuffer buffer) {
        setData(header, buffer, 1);
    }

    @Override
    public void setData(@NonNull APngHeader header, @NonNull ByteBuffer buffer, int sampleSize) {
        if (sampleSize <= 0) {
            throw new IllegalArgumentException("Sample size must be >=0, not: " + sampleSize);
        }
        // Make sure sample size is a power of 2.
        sampleSize = Integer.highestOneBit(sampleSize);
        this.status = STATUS_OK;
        this.header = header;
        framePointer = INITIAL_FRAME_POINTER;
        // Initialize the raw data buffer.
        rawData = buffer.asReadOnlyBuffer();
        rawData.position(0);
        rawData.order(ByteOrder.LITTLE_ENDIAN);

        // No point in specially saving an old frame if we're never going to use it.
        savePrevious = false;
        for (APngFrame frame : header.frames) {
            if (frame.dispose == APngFrame.DISPOSAL_PREVIOUS) {
                savePrevious = true;
                break;
            }
        }

        this.sampleSize = sampleSize;
        downsampledWidth = header.width / sampleSize;
        downsampledHeight = header.height / sampleSize;

        mainPixels = new byte[header.width * header.height];
        mainScratch = new int[downsampledWidth * downsampledHeight];
    }

    @NonNull
    private APngHeaderParser getHeaderParser() {
        if (parser == null) {
            parser = new APngHeaderParser();
        }
        return parser;
    }

    @Override
    @AnimDecodeStatus
    public int read(@Nullable byte[] data) {
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
}
