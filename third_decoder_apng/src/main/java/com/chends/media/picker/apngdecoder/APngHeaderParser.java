package com.chends.media.picker.apngdecoder;

import com.chends.media.picker.decoder.AnimDecoder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author cds created on 2019/9/19.
 */
public class APngHeaderParser {

    private ByteBuffer rawData;
    private APngHeader header;
    private int apngSequenceExpect = 0;
    private List<APngChunk> chunks = new ArrayList<>();

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
        readHeaderContents();
        if (header.frameCount < 0) {
            header.status = AnimDecoder.STATUS_FORMAT_ERROR;
        }
        return header;
    }

    public boolean isAnimated() {
        readHeaderContents(2);
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
    private void readHeaderContents() {
        readHeaderContents(Integer.MAX_VALUE);
    }

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
    private void readHeaderContents(int maxFrames) {
        if (!checkSignature()) return;
        boolean done = false;
        int length, code;
        while (!err() && !done && (apngSequenceExpect < maxFrames)) {
            length = readInt();
            code = readInt();
            switch (code) {
                case APngConstant.IHDR_VALUE:
                    if (length != APngConstant.LENGTH_IHDR) {
                        header.status = AnimDecoder.STATUS_FORMAT_ERROR;
                        break;
                    }
                    readIHDR();
                    break;
                case APngConstant.acTL_VALUE:
                    if (length != APngConstant.LENGTH_acTL) {
                        header.status = AnimDecoder.STATUS_FORMAT_ERROR;
                        break;
                    }
                    readAcTL();
                    break;
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
                case APngConstant.tIME_VALUE:
                case APngConstant.iTXt_VALUE:
                case APngConstant.tEXt_VALUE:
                case APngConstant.zTXt_VALUE:
                    chunks.add(new APngChunk(length + APngConstant.CHUNK_TOP_LENGTH + APngConstant.LENGTH_CRC,
                            rawData.position() - APngConstant.CHUNK_TOP_LENGTH));
                    skip(length);
                    break;
                case APngConstant.gAMA_VALUE:
                case APngConstant.bKGD_VALUE:
                case APngConstant.tRNS_VALUE:
                default:
                    skip(length);
                    break;
            }
            readCRC();
            if (!done) {
                done = rawData.position() >= rawData.limit();
            }
        }
        if (!chunks.isEmpty()) {
            header.otherChunk = chunks.toArray(new APngChunk[0]);
        }
        if (header.frameCount != header.frames.size()) {
            //Log.w("apng decoder", "apng文件内容有误！");
            header.frameCount = header.frames.size();
        }
    }

    /**
     * 块数据检测
     * <a href = "https://www.w3.org/TR/2003/REC-PNG-20031110/#5DataRep">crc</a>
     */
    private void readCRC() {
        //readInt();
        skip(4);
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
        chunks.clear();
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
        if (rawData.position() + APngConstant.LENGTH_CRC < rawData.limit()){
            header.frames.add(frame);
        }/* else {
            // 后面再无数据则不添加
            Log.w("apng data error", "after fcTL have not fdAT!");
        }*/
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
        if (length > rawData.remaining()) {
            // 数据错误(数量不足)删除
            header.frames.remove(header.frames.size() - 1);
            //Log.w("apng data error", "fdAT data deficient!");
        } else {
            apngSequenceExpect++;
            header.currentFrame.bufferFrameStart = rawData.position();
            header.currentFrame.length = length - 4;
            header.currentFrame.isFdAT = true;
        }
        // 减去sequence占用的位置
        skip(length - 4);
    }

    /**
     * read palette<br/>
     * <a href = "https://www.w3.org/TR/PNG/#11PLTE">palette</a>
     */
    private void readPalette(int length) {
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
