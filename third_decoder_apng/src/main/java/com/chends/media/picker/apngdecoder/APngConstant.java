package com.chends.media.picker.apngdecoder;

/**
 * @author chends create on 2019/9/20.
 */
class APngConstant {
    /**
     * png signature 长度
     */
    static final int LENGTH_SIGNATURE = 8;
    /**
     * chunk length长度
     */
    static final int CHUNK_LENGTH_LENGTH = 4;
    /**
     * chunk type长度
     */
    static final int CHUNK_TYPE_LENGTH = 4;
    /**
     * TOP长度
     */
    static final int CHUNK_TOP_LENGTH = CHUNK_LENGTH_LENGTH + CHUNK_TYPE_LENGTH;
    /**
     * IHDR 长度
     */
    static final int LENGTH_IHDR = 4 + 4 + 5;
    /**
     * CRC长度
     */
    static final int LENGTH_CRC = 4;
    /**
     * acTL长度
     */
    static final int LENGTH_acTL = 4 + 4;
    /**
     * acTL 块长度
     */
    static final int LENGTH_acTL_CHUNK = LENGTH_acTL + CHUNK_TOP_LENGTH + LENGTH_CRC;
    /**
     * fcTL 长度
     */
    static final int LENGTH_fcTL = 4 + 4 + 4 + 4 + 4 + 2 + 2 + 1 + 1;
    /**
     * fcTL 块长度
     */
    static final int LENGTH_fcTL_CHUNK = LENGTH_fcTL + CHUNK_TOP_LENGTH + LENGTH_CRC;

    /**
     * png signature 内容
     */
    static final byte[] BYTES_SIGNATURE = new byte[]{
            (byte) 0x89, 'P', 'N', 'G', (byte) 0x0D, (byte) 0x0A, (byte) 0x1A, (byte) 0x0A
    };
    static final int IHDR_VALUE = 'I' << 24 | 'H' << 16 | 'D' << 8 | 'R';
    static final int acTL_VALUE = 'a' << 24 | 'c' << 16 | 'T' << 8 | 'L';
    static final int IDAT_VALUE = 'I' << 24 | 'D' << 16 | 'A' << 8 | 'T';
    static final int fcTL_VALUE = 'f' << 24 | 'c' << 16 | 'T' << 8 | 'L';
    static final int fdAT_VALUE = 'f' << 24 | 'd' << 16 | 'A' << 8 | 'T';
    static final int PLTE_VALUE = 'P' << 24 | 'L' << 16 | 'T' << 8 | 'E';
    static final int IEND_VALUE = 'I' << 24 | 'E' << 16 | 'N' << 8 | 'D';
    static final int gAMA_VALUE = 'g' << 24 | 'A' << 16 | 'M' << 8 | 'A';
    static final int bKGD_VALUE = 'b' << 24 | 'K' << 16 | 'G' << 8 | 'D';
    static final int tRNS_VALUE = 't' << 24 | 'R' << 16 | 'N' << 8 | 'S';

    static final int tIME_VALUE = 't' << 24 | 'I' << 16 | 'M' << 8 | 'E';
    static final int iTXt_VALUE = 'i' << 24 | 'T' << 16 | 'X' << 8 | 't';
    static final int tEXt_VALUE = 't' << 24 | 'E' << 16 | 'X' << 8 | 't';
    static final int zTXt_VALUE = 'z' << 24 | 'T' << 16 | 'X' << 8 | 't';
}