package com.chends.media.picker.apngdecoder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.Log;

import com.chends.media.picker.decoder.AnimDecoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.CRC32;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
    private int[] mainPixels;
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
    private List<Bitmap> bitmapList;
    private Bitmap previousImage, bgImage;

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

    @Override
    public boolean recycle() {
        return false;
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
        return build();
    }

    /**
     * build
     * @return bitmap
     */
    private Bitmap build() {
        if (bitmapList.get(framePointer) == null) {
            APngFrame currentFrame = header.frames.get(framePointer);
            APngFrame previousFrame = null;
            int previousIndex = framePointer - 1;
            if (previousIndex >= 0) {
                previousFrame = header.frames.get(previousIndex);
            }
            Bitmap current = decodeBitmap(currentFrame);
            if (header.hasFcTL && previousFrame == null) {
                current.getPixels(mainPixels, 0, w, 0, 0, w, h);
                previousImage.setPixels(mainPixels, 0, w, 0, 0, w, h);
                bitmapList.add(framePointer, current);
            } else {
                Bitmap result = getNextBitmap();
                Canvas canvas = new Canvas(result);
                if (previousFrame == null) {
                    canvas.drawBitmap(bgImage, 0, 0, null);
                } else {
                    canvas.drawBitmap(previousImage, 0, 0, null);
                }
                if (currentFrame.blendOp == APngFrame.APNG_BLEND_OP_SOURCE) {
                    canvas.clipRect(currentFrame.xOffset, currentFrame.yOffset, currentFrame.xOffset + currentFrame.width,
                            currentFrame.yOffset + currentFrame.height);
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    canvas.clipRect(0, 0, w, h);
                }
                canvas.drawBitmap(current, currentFrame.xOffset, currentFrame.yOffset, null);
                canvas.setBitmap(null);
                current.recycle();
                switch (currentFrame.dispose) {
                    case APngFrame.DISPOSAL_BACKGROUND:
                    case APngFrame.DISPOSAL_NONE:
                    case APngFrame.DISPOSAL_UNSPECIFIED:
                        // save previous
                        result.getPixels(mainPixels, 0, w, 0, 0, w, h);
                        previousImage.setPixels(mainPixels, 0, w, 0, 0, w, h);
                        if (currentFrame.dispose == APngFrame.DISPOSAL_BACKGROUND) {
                            for (int x = currentFrame.xOffset; x < currentFrame.xOffset + currentFrame.width; x++) {
                                for (int y = currentFrame.yOffset; y < currentFrame.yOffset + currentFrame.height; y++) {
                                    //clear
                                    previousImage.setPixel(x, y, Color.TRANSPARENT);
                                }
                            }
                        }
                        break;
                    case APngFrame.DISPOSAL_PREVIOUS:
                        if (previousFrame == null) {
                            bgImage.getPixels(mainPixels, 0, w, 0, 0, w, h);
                            previousImage.setPixels(mainPixels, 0, w, 0, 0, w, h);
                        }
                        break;
                }
                bitmapList.add(framePointer, result);
            }
        }
        return bitmapList.get(framePointer);
    }

    /**
     * decodeBitmap
     * @param frame frame
     * @return bitmap
     */
    private Bitmap decodeBitmap(APngFrame frame) {
        // frame 为null表示要绘制背景
        int length, frameLength, end, cLength, cType;
        if (header.idatLastPosition > 0) {
            end = header.idatLastPosition;
        } else {
            end = header.idatFirstPosition;
        }
        if (frame == null || !frame.isFdAT) {
            length = header.idatFirstPosition - APngConstant.LENGTH_acTL_CHUNK;
            if (frame != null) {
                length -= APngConstant.LENGTH_fcTL_CHUNK;
            }
            rawData.position(header.idatFirstPosition);
            while (rawData.position() <= end) {
                cLength = rawData.getInt(); // 长度
                cType = rawData.getInt(); // chunk type
                if (cType != APngConstant.acTL_VALUE && cType != APngConstant.fcTL_VALUE) {
                    length += cLength + APngConstant.CHUNK_TOP_LENGTH + APngConstant.LENGTH_CRC;
                }
                rawData.position(rawData.position() + cLength + APngConstant.LENGTH_CRC);
            }
            length += APngConstant.CHUNK_TOP_LENGTH + APngConstant.LENGTH_CRC;
        } else {
            length = header.idatFirstPosition - APngConstant.LENGTH_acTL_CHUNK -
                    (header.hasFcTL ? APngConstant.LENGTH_fcTL_CHUNK : 0);
            frameLength = APngConstant.CHUNK_TOP_LENGTH + frame.length + APngConstant.LENGTH_CRC;
            length += frameLength + APngConstant.CHUNK_TOP_LENGTH + APngConstant.LENGTH_CRC;// add iend length
        }
        if (header.otherChunk != null && header.otherChunk.length > 0) {
            for (APngChunk chunk : header.otherChunk) {
                length += chunk.totalLength;
            }
        }
        byte[] raw = new byte[length];
        int index = 0;
        for (int i = 0; i < APngConstant.LENGTH_SIGNATURE; i++) {
            raw[index] = APngConstant.BYTES_SIGNATURE[i];
            index++;
        }
        rawData.position(index);
        if (frame == null || !frame.isFdAT) {
            while (rawData.position() <= end) {
                cLength = rawData.getInt(); // 长度
                cType = rawData.getInt(); // chunk type
                if (cType != APngConstant.acTL_VALUE && cType != APngConstant.fcTL_VALUE) {
                    rawData.position(rawData.position() - APngConstant.CHUNK_TOP_LENGTH);
                    rawData.get(raw, index, cLength + APngConstant.CHUNK_TOP_LENGTH + APngConstant.LENGTH_CRC);
                    index += cLength + APngConstant.CHUNK_TOP_LENGTH + APngConstant.LENGTH_CRC;
                } else {
                    rawData.position(rawData.position() + cLength + APngConstant.LENGTH_CRC);
                }
            }
        } else {
            while (rawData.position() < header.idatFirstPosition) {
                cLength = rawData.getInt(); // 长度
                cType = rawData.getInt(); // chunk type
                if (cType == APngConstant.acTL_VALUE || cType == APngConstant.fcTL_VALUE) {
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
        }
        // add other thunks
        if (header.otherChunk != null && header.otherChunk.length > 0) {
            for (APngChunk chunk : header.otherChunk) {
                rawData.position(chunk.positionStart);
                rawData.get(raw, index, chunk.totalLength);
                index += chunk.totalLength;
            }
        }
        addIEND(raw, index);
        return BitmapFactory.decodeByteArray(raw, 0, raw.length);
        /*Bitmap bitmap = BitmapFactory.decodeByteArray(raw, 0, raw.length);
        if (bitmap == null) {
            Log.w("getNextFrame error", "framePointer:" + framePointer + ",frame:" + frame);
        } else {
            Log.i("getNextFrame success", "framePointer:" + framePointer + ",frame:" + frame);
        }
        return bitmap;*/
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
        if (bitmapList != null) {
            for (Bitmap bitmap : bitmapList) {
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            }
            bitmapList.clear();
            bitmapList = null;
        }
        mainPixels = null;
        if (previousImage != null) {
            if (!previousImage.isRecycled()) {
                previousImage.recycle();
            }
            previousImage = null;
        }
        if (bgImage != null) {
            if (!bgImage.isRecycled()) {
                bgImage.recycle();
            }
            bgImage = null;
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
        this.status = STATUS_OK;
        this.header = header;
        framePointer = INITIAL_FRAME_POINTER;
        // Initialize the raw data buffer.
        rawData = buffer.asReadOnlyBuffer();
        rawData.position(0);
        rawData.order(ByteOrder.BIG_ENDIAN);

        w = header.width;
        h = header.height;
        mainPixels = new int[w * h];
        bitmapList = new ArrayList<>(Collections.nCopies(header.frameCount, (Bitmap) null));
        previousImage = getNextBitmap();
        if (!header.hasFcTL) {
            bgImage = decodeBitmap(null);
        }
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
