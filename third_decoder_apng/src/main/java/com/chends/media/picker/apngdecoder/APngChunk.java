package com.chends.media.picker.apngdecoder;

/**
 * @author chends create on 2019/9/24.
 */
public class APngChunk {
    public int totalLength; // 总长度，包含top，length，crc
    public int positionStart; // 起始点

    public APngChunk() {
    }

    public APngChunk(int totalLength, int positionStart) {
        this.totalLength = totalLength;
        this.positionStart = positionStart;
    }
}
