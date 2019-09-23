package com.chends.media.picker.apngdecoder;

import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.chends.media.picker.decoder.AnimDecoder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author cds created on 2019/9/19.
 */
public class APngHeaderParser {

    private ByteBuffer rawData;
    private APngHeader header;
    private int apngSequenceExpect = 0;

    public APngHeaderParser setData(@NonNull ByteBuffer data) {
        reset();
        rawData = data.asReadOnlyBuffer();
        rawData.position(0);
        rawData.order(ByteOrder.BIG_ENDIAN);
        return this;
    }

    public APngHeaderParser setData(@Nullable byte[] data) {
        if (data != null) {
            setData(ByteBuffer.wrap(data));
        } else {
            rawData = null;
            header.status = AnimDecoder.STATUS_OPEN_ERROR;
        }
        return this;
    }

    public void clear() {
        rawData = null;
        header = null;
    }

    private void reset() {
        rawData = null;
        header = new APngHeader();
    }

    @NonNull
    APngHeader parseHeader() {
        if (rawData == null) {
            throw new IllegalStateException("You must call setData() before parseHeader()");
        }
        if (err()) {
            return header;
        }
        long start = SystemClock.elapsedRealtime();
        Log.i("parseHeader", "start");
        readHeader();
        if (!err()) {
            readContents();
            if (header.frameCount < 0) {
                header.status = AnimDecoder.STATUS_FORMAT_ERROR;
            }
        }
        Log.i("parseHeader", "end use time: " + (SystemClock.elapsedRealtime() - start) + "ms");
        return header;
    }

    public boolean isAnimated() {
        readHeader();
        readContents(2);
        return header.frameCount > 1 && apngSequenceExpect > 1;
    }

    /**
     * <a href='https://www.w3.org/TR/PNG/#5PNG-file-signature'>signature</a>
     * @return check success
     */
    private boolean checkSignature() {
        for (int i = 0; i < APngConstant.LENGTH_SIGNATURE; i++) {
            byte b = readByte();
            if (b != APngConstant.BYTES_SIGNATURE[i]) {
                header.status = AnimDecoder.STATUS_FORMAT_ERROR;
                return false;
            }
        }
        return true;
    }

    /**
     * Reads APNG file header information.
     * <a href="https://www.w3.org/TR/PNG/#5DataRep">Datastream structure</a>
     */
    private void readHeader() {
        if (!checkSignature()) return;
        /*
         * If the default image is the first frame:<br/>
         *     Sequence number    Chunk
         *     (none)             `acTL`
         *     0                  `fcTL` first frame
         *     (none)             `IDAT` first frame / default image
         *     1                  `fcTL` second frame
         *     2                  first `fdAT` for second frame
         *     3                  second `fdAT` for second frame
         *     ....
         * If the default image is not part of the animation:<br/>
         *     Sequence number    Chunk
         *     (none)             `acTL`
         *     (none)             `IDAT` default image
         *     0                  `fcTL` first frame
         *     1                  first `fdAT` for first frame
         *     2                  second `fdAT` for first frame
         *     ....
         */
        int length, type;
        boolean done = false;
        while (!err() && !done) {
            length = readInt(); // 长度
            type = readInt(); // chunk type
            switch (type) {
                case APngConstant.IHDR_VALUE:
                    if (length != APngConstant.LENGTH_IHDR) {
                        header.status = AnimDecoder.STATUS_FORMAT_ERROR;
                        break;
                    }
                    readIHDR();
                    break;
                case APngConstant.acTL_VALUE:
                    done = true;
                    if (length != APngConstant.LENGTH_acTL) {
                        header.status = AnimDecoder.STATUS_FORMAT_ERROR;
                        break;
                    }
                    readAcTL();
                    break;
                default:
                    skip(length);
                    break;
            }
            readCRC();
        }
    }

    /**
     * 块数据检测
     * <a href = "https://www.w3.org/TR/2003/REC-PNG-20031110/#5DataRep">crc</a>
     */
    private void readCRC() {
        //readInt();
        rawData.position(Math.min(rawData.position() + 4, rawData.limit()));
    }

    /**
     * Image Head Chunk
     * Width	4 bytes<br/>
     * Height	4 bytes<br/>
     * Bit depth	1 byte<br/>
     * Colour type	1 byte<br/>
     * Compression method	1 byte<br/>
     * Filter method	1 byte<br/>
     * Interlace method	1 byte<br/>
     */
    private void readIHDR() {
        header.width = readInt();
        header.height = readInt();
        header.bitDepth = readByte();
        try {
            header.colourType = PngColourType.fromByte(readByte());
        } catch (IllegalArgumentException e) {
            header.status = AnimDecoder.STATUS_FORMAT_ERROR;
        }
        header.compressionMethod = readByte();
        header.filterMethod = readByte();
        header.interlaceMethod = readByte();
        Log.w("readIHDR", "header:" + header);
    }

    /**
     * Animation Control Chunk<br/>
     * num_frames：0~3字节表示该Apng总的播放帧数。<br/>
     * num_plays：4~7字节表示该Apng循环播放的次数。<br/>
     */
    private void readAcTL() {
        header.frameCount = readInt();
        header.loopCount = readInt();
    }

    /**
     * read The Animation Data Chunk
     */
    private void readIDAT(int length) {
        // 有多个idat存在的情况
        if (header.idatFirstPosition != 0) {
            // 已经有idat
            header.idatLastPosition = rawData.position() - APngConstant.CHUNK_TOP_LENGTH;
        } else {
            // 第一个idat
            header.idatFirstPosition = rawData.position() - APngConstant.CHUNK_TOP_LENGTH;
        }
        if (apngSequenceExpect > 0) {
            // fcTL在IDAT之前，当做第一帧
            header.hasFcTL = true;
            header.currentFrame.bufferFrameStart = rawData.position();
            header.currentFrame.length = Math.min(length, rawData.remaining());
        }
        skip(length);
    }

    /**
     * read fcTL<br/>
     * byte  description<br/>
     * 0     sequence_number：控制帧的序号，从0开始。<br/>
     * 4     width：帧的宽度。<br/>
     * 8     /height：帧的高度。<br/>
     * 12    x_offset：在x方向的偏移。<br/>
     * 16    y_offset：在y方向的偏移。<br/>
     * 20    delay_num：帧动画时间间隙的分子<br/>
     * 22    delay_den：帧动画时间间隙的分母<br/>
     * 24    dispose_op：在显示该帧之前，需要对前面缓冲输出区域做何种处理。<br/>
     * 25    blend_op：具体显示该帧的方式。<br/>
     */
    private void readFcTL() {
        APngFrame frame = new APngFrame();
        header.currentFrame = frame;
        int sequence = readInt();
        if (sequence != apngSequenceExpect) {
            header.status = AnimDecoder.STATUS_FORMAT_ERROR;
            return;
        }
        apngSequenceExpect++;
        frame.sequenceNumber = sequence;
        frame.width = readInt();
        frame.height = readInt();
        frame.xOffset = readInt();
        frame.yOffset = readInt();
        //frame.delayNumerator = readUnsignedShort();
        //short delayDenominator = readUnsignedShort();
        //frame.delayDenominator = delayDenominator == 0 ? 100 : delayDenominator; // APNG spec says zero === 100.
        frame.setDelay(readUnsignedShort(), readUnsignedShort());
        frame.setDisposeOp(readByte());
        frame.setBlendOp(readByte());
        header.frames.add(frame);
    }

    /**
     * Main file parser. Reads GIF content blocks.
     */
    private void readContents() {
        readContents(Integer.MAX_VALUE /* maxFrames */);
    }

    /**
     * read content
     * @param maxFrames maxFrames
     */
    private void readContents(int maxFrames) {
        boolean done = false;
        int length, code;
        while (!err() && !done && (apngSequenceExpect < maxFrames)) {
            length = readInt();
            code = readInt();
            switch (code) {
                case APngConstant.PLTE_VALUE:
                    readPalette(length);
                    break;
                case APngConstant.fcTL_VALUE:
                    if (length != APngConstant.LENGTH_fcTL) {
                        header.status = AnimDecoder.STATUS_FORMAT_ERROR;
                        break;
                    }
                    readFcTL();
                    break;
                case APngConstant.IDAT_VALUE:
                    readIDAT(length);
                    break;
                case APngConstant.fdAT_VALUE:
                    readFdAT(length);
                    break;
                case APngConstant.IEND_VALUE:
                    header.iendPosition = rawData.position() - APngConstant.CHUNK_TOP_LENGTH;
                    done = true;
                    break;
                case APngConstant.gAMA_VALUE:
                    // todo
                case APngConstant.bKGD_VALUE:
                    // todo
                case APngConstant.tRNS_VALUE:
                    // todo
                default:
                    skip(length);
                    break;
            }
            readCRC();
            if (!done) {
                done = rawData.position() >= rawData.limit();
            }
        }
        if (header.frameCount != header.frames.size()) {
            Log.w("apng decoder", "apng文件内容有误！");
            header.frameCount = header.frames.size();
        }
    }

    /**
     * read fdAT<br/>
     * 0    sequence_number       (unsigned int)   Sequence number of the animation chunk, starting from 0<br/>
     * 4    frame_data            X bytes          Frame data for this frame
     */
    private void readFdAT(int length) {
        int sequence = readInt();
        if (sequence != apngSequenceExpect) {
            header.status = AnimDecoder.STATUS_FORMAT_ERROR;
            return;
        }
        apngSequenceExpect++;
        header.currentFrame.bufferFrameStart = rawData.position();
        // 长度修正
        header.currentFrame.length = Math.min(length - 4, rawData.remaining());
        header.currentFrame.isFdAT = true;
        // 减去sequence占用的位置
        skip(length - 4);
    }

    /**
     * read palette<br/>
     * <a href = "https://www.w3.org/TR/PNG/#11PLTE">palette</a>
     */
    private void readPalette(int length) {
        // todo
        if (length % 3 != 0) {
            header.status = AnimDecoder.STATUS_FORMAT_ERROR;
            return;
        }
        skip(length);
    }

    /**
     * skip
     */
    private void skip(int length) {
        int newPosition = Math.min(rawData.position() + length, rawData.limit());
        rawData.position(newPosition);
    }

    /**
     * Reads a single byte from the input stream.
     */
    /*private int read() {
        int currByte = 0;
        try {
            currByte = rawData.get() & MASK_INT_LOWEST_BYTE;
        } catch (Exception e) {
            header.status = AnimDecoder.STATUS_FORMAT_ERROR;
        }
        return currByte;
    }*/

    /**
     * read int
     * @return int
     */
    private int readInt() {
        return rawData.getInt();
    }

    /**
     * Reads next 16-bit value, LSB first.
     */
    private int readShort() {
        // Read 16-bit value.
        return rawData.getShort();
    }

    /**
     * read byte
     * @return byte
     */
    private byte readByte() {
        return rawData.get();
    }

    public short readUnsignedShort() {
        return (short) (rawData.getShort() & 0xffff);
    }

    private boolean err() {
        return header.status != AnimDecoder.STATUS_OK;
    }
}
