package com.chends.media.picker.apngdecoder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.chends.media.picker.decoder.AnimDecoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.CRC32;

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
    private int w, h;
    /*private boolean savePrevious = false;
    private int sampleSize;
    private int downsampledWidth;
    private int downsampledHeight;
    private byte[] pixelStack;
    private byte[] mainPixels;
    @ColorInt
    private int[] mainScratch;*/
    @NonNull
    private Bitmap.Config bitmapConfig = Bitmap.Config.ARGB_8888;
    private Bitmap previousImage;

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
        return rawData.limit();
    }

    @Nullable
    @Override
    public Bitmap getNextFrame() {
        if (header.frameCount <= 0 || framePointer < 0) {
            status = STATUS_FORMAT_ERROR;
        }
        if (status == STATUS_FORMAT_ERROR || status == STATUS_OPEN_ERROR) {
            return null;
        }
        status = STATUS_OK;
        APngFrame currentFrame = header.frames.get(framePointer);
        APngFrame previousFrame = null;
        int previousIndex = framePointer - 1;
        if (previousIndex >= 0) {
            previousFrame = header.frames.get(previousIndex);
        }
        return build(currentFrame, previousFrame);
    }

    /**
     * build
     * @param currentFrame  currentFrame
     * @param previousFrame previousFrame
     * @return bitmap
     */
    private Bitmap build(APngFrame currentFrame, APngFrame previousFrame) {
        Bitmap current = decodeBitmap(currentFrame);
        Log.i(TAG, "decodeBitmap current");
        if (previousFrame == null) {
            if (previousImage != null && !previousImage.isRecycled()) {
                previousImage.recycle();
                Log.i(TAG, "recycle previousImage");
            }
            previousImage = null;
            previousImage = current.copy(bitmapConfig, true);
            Log.i(TAG, "copy to previousImage");
            return current;
        } else {
            Bitmap result = getNextBitmap();
            Canvas canvas = new Canvas(result);
            canvas.drawBitmap(previousImage, 0, 0, null);
            Log.i(TAG, "canvas drawBitmap previousImage");
            if (currentFrame.blendOp == APngFrame.APNG_BLEND_OP_SOURCE) {
                canvas.clipRect(currentFrame.xOffset, currentFrame.yOffset, currentFrame.xOffset + currentFrame.width,
                        currentFrame.yOffset + currentFrame.height);
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                canvas.clipRect(0, 0, w, h);
            }
            canvas.drawBitmap(current, currentFrame.xOffset, currentFrame.yOffset, null);
            Log.i(TAG, "canvas drawBitmap current");
            current.recycle();
            Log.i(TAG, "recycle current");
            switch (currentFrame.dispose) {
                case APngFrame.DISPOSAL_BACKGROUND:
                case APngFrame.DISPOSAL_NONE:
                case APngFrame.DISPOSAL_UNSPECIFIED:
                    // save previous
                    previousImage = result.copy(bitmapConfig, true);
                    Log.i(TAG, "copy to previousImage 2");
                    if (currentFrame.dispose == APngFrame.DISPOSAL_BACKGROUND) {
                        for (int x = currentFrame.xOffset; x < currentFrame.xOffset + currentFrame.width; x++) {
                            for (int y = currentFrame.yOffset; y < currentFrame.yOffset + currentFrame.height; y++) {
                                //clear
                                previousImage.setPixel(x, y, Color.TRANSPARENT);
                            }
                        }
                    /*Canvas tempCanvas = new Canvas(previousImage);
                    tempCanvas.clipRect(currentFrame.xOffset, currentFrame.yOffset, currentFrame.xOffset + currentFrame.width,
                            currentFrame.yOffset + currentFrame.height);
                    tempCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    tempCanvas.clipRect(0, 0, downsampledWidth, downsampledHeight);*/
                    }
                    break;
                case APngFrame.DISPOSAL_PREVIOUS:
                    /*APngFrame tempFrame;
                    if (framePointer > 1) {
                        for (int i = framePointer - 2; i >= 0; i--) {
                            tempFrame = header.frames.get(i);
                            if (tempFrame.dispose == AnimFrame.DISPOSAL_NONE || tempFrame.dispose == APngFrame.DISPOSAL_BACKGROUND) {
                                if (tempFrame.dispose == AnimFrame.DISPOSAL_NONE){

                                } else {

                                }
                                break;
                            }
                        }
                    }
                    break;*/
                    // do nothing
                    break;
            }
            return result;
        }
    }

    /**
     * decodeBitmap
     * @param frame frame
     * @return bitmap
     */
    private Bitmap decodeBitmap(APngFrame frame) {
        Bitmap bitmap = null;
        if (frame != null) {
            // 复制第一个IDAT之前的数据（除了acTL,fctl）
            int length = header.idatPosition - APngConstant.LENGTH_acTL_CHUNK -
                    (header.hasFcTL ? APngConstant.LENGTH_fcTL_CHUNK : 0);
            int frameLength = APngConstant.CHUNK_TOP_LENGTH + frame.length + APngConstant.LENGTH_CRC;
            length += frameLength + APngConstant.CHUNK_TOP_LENGTH + APngConstant.LENGTH_CRC;// add iend length
            byte[] raw = new byte[length];
            int index = 0;
            for (int i = 0; i < APngConstant.LENGTH_SIGNATURE; i++) {
                raw[index] = APngConstant.BYTES_SIGNATURE[i];
                index++;
            }
            rawData.position(index);
            int cLength, cType;
            while (rawData.position() <= header.idatPosition) {
                cLength = rawData.getInt(); // 长度
                cType = rawData.getInt(); // chunk type
                if (cType == APngConstant.acTL_VALUE || cType == APngConstant.fcTL_VALUE || cType == APngConstant.IDAT_VALUE) {
                    // skip
                    rawData.position(rawData.position() + cLength + APngConstant.LENGTH_CRC);
                } else {
                    // copy
                    rawData.position(rawData.position() - APngConstant.CHUNK_TOP_LENGTH);
                    rawData.get(raw, index, cLength + APngConstant.CHUNK_TOP_LENGTH + APngConstant.LENGTH_CRC);
                    boolean needUpdate = false;
                    if (cType == APngConstant.IHDR_VALUE) {
                        if (header.width != frame.width) {
                            // 修改wh 更新crc
                            writeInt4ToBytes(frame.width, raw, index + APngConstant.CHUNK_TOP_LENGTH);
                            needUpdate = true;
                        }
                        if (header.height != frame.height) {
                            writeInt4ToBytes(frame.height, raw, index + APngConstant.CHUNK_TOP_LENGTH + 4);
                            needUpdate = true;
                        }
                    }
                    if (needUpdate) {
                        // 更新crc
                        updateCRC(index + APngConstant.CHUNK_LENGTH_LENGTH, raw, APngConstant.LENGTH_IHDR);
                    }
                    index += cLength + APngConstant.CHUNK_TOP_LENGTH + APngConstant.LENGTH_CRC;
                }
            }

            if (frame.isFdAT) {
                // 修改length
                writeInt4ToBytes(frame.length, raw, index);
                index += APngConstant.CHUNK_LENGTH_LENGTH;
                // change fdAT to IDAT
                raw[index] = 'I';
                raw[index + 1] = 'D';
                raw[index + 2] = 'A';
                raw[index + 3] = 'T';
                index += APngConstant.CHUNK_TYPE_LENGTH;
                rawData.position(frame.bufferFrameStart);
                rawData.get(raw, index, frame.length);
                updateCRC(index - 4, raw, frame.length);
                /*CRC32 crc32 = new CRC32();
                crc32.update(raw, index - 4, 4);
                crc32.update(raw, index, frame.length);*/
                index += frame.length;
                //writeInt4ToBytes((int) crc32.getValue(), raw, index);
                index += APngConstant.LENGTH_CRC;
            } else {
                rawData.position(frame.bufferFrameStart - APngConstant.CHUNK_TOP_LENGTH);
                rawData.get(raw, index, APngConstant.CHUNK_TOP_LENGTH + frame.length + APngConstant.LENGTH_CRC);
                index += APngConstant.CHUNK_TOP_LENGTH + frame.length + APngConstant.LENGTH_CRC;
            }
            addIEND(raw, index);

            bitmap = BitmapFactory.decodeByteArray(raw, 0, raw.length);

            if (bitmap == null) {
                Log.w("getNextFrame error", "framePointer:" + framePointer + ",frame:" + frame);
            } else {
                Log.i("getNextFrame success", "framePointer:" + framePointer + ",frame:" + frame);
            }
        }
        return bitmap;
    }

    /**
     * 添加结尾
     * @param bytes  bytes
     * @param offset offset
     */
    private void addIEND(byte[] bytes, int offset) {
        if (header.iendPosition != 0) {
            rawData.position(header.iendPosition);
            rawData.get(bytes, offset, APngConstant.CHUNK_TOP_LENGTH + APngConstant.LENGTH_CRC);
        } else {
            byte[] iend = new byte[]{0, 0, 0, 0, 'I', 'E', 'N', 'D'};
            for (byte item : iend) {
                bytes[offset] = item;
                offset++;
            }
            updateCRC(offset - 4, bytes, 0);
            /*CRC32 crc32 = new CRC32();
            crc32.update(bytes, offset - 4, 4);
            writeInt4ToBytes((int) crc32.getValue(), bytes, offset);*/
        }
    }

    /**
     * 更新 crc
     * @param offset     offset
     * @param bytes      bytes
     * @param dataLength 数据长度
     */
    public static void updateCRC(int offset, byte[] bytes, int dataLength) {
        CRC32 crc32 = new CRC32();
        // update type
        crc32.update(bytes, offset, 4);
        if (dataLength > 0) {
            // update data
            crc32.update(bytes, offset + 4, dataLength);
        }
        writeInt4ToBytes((int) crc32.getValue(), bytes, offset + 4 + dataLength);
    }

    /**
     * 更新int到byte中
     * @param n      n
     * @param b      b
     * @param offset offset
     */
    public static void writeInt4ToBytes(int n, byte[] b, int offset) {
        b[offset] = (byte) (n >> 24 & 255);
        b[offset + 1] = (byte) (n >> 16 & 255);
        b[offset + 2] = (byte) (n >> 8 & 255);
        b[offset + 3] = (byte) (n & 255);
    }

    private Bitmap getNextBitmap() {
        Bitmap result = Bitmap.createBitmap(w, h, bitmapConfig);
        result.setHasAlpha(true);
        return result;
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
        if (previousImage != null) {
            if (!previousImage.isRecycled()) {
                previousImage.recycle();
            }
            previousImage = null;
        }
        header = null;
        if (rawData != null) {
            rawData = null;
        }
    }

    @Override
    public void setData(@NonNull APngHeader header, @NonNull byte[] data) {
        setData(header, ByteBuffer.wrap(data));
    }

    @Override
    public void setData(@NonNull APngHeader header, @NonNull ByteBuffer buffer) {
        /*if (sampleSize <= 0) {
            throw new IllegalArgumentException("Sample size must be >=0, not: " + sampleSize);
        }*/
        // Make sure sample size is a power of 2.
        //sampleSize = Integer.highestOneBit(sampleSize);
        this.status = STATUS_OK;
        this.header = header;
        framePointer = INITIAL_FRAME_POINTER;
        // Initialize the raw data buffer.
        rawData = buffer.asReadOnlyBuffer();
        rawData.position(0);
        rawData.order(ByteOrder.BIG_ENDIAN);

        w = header.width;
        h = header.height;
        // No point in specially saving an old frame if we're never going to use it.
        /*savePrevious = false;
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
        mainScratch = new int[downsampledWidth * downsampledHeight];*/
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
