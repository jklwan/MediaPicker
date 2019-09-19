package com.chends.media.picker.apngdecoder;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.chends.media.picker.decoder.AnimDecoder;

import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * @author cds created on 2019/9/19.
 */
public class StandardAPngDecoder implements AnimDecoder<APngHeader> {

    @Override
    public int getWidth() {
        return 0;
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
        return 0;
    }

    @Override
    public void advance() {

    }

    @Override
    public int getDelay(int n) {
        return 0;
    }

    @Override
    public int getNextDelay() {
        return 0;
    }

    @Override
    public int getFrameCount() {
        return 0;
    }

    @Override
    public int getCurrentFrameIndex() {
        return 0;
    }

    @Override
    public void resetFrameIndex() {

    }

    @Override
    public int getNetscapeLoopCount() {
        return 0;
    }

    @Override
    public int getTotalIterationCount() {
        return 0;
    }

    @Override
    public int getByteSize() {
        return 0;
    }

    @Nullable
    @Override
    public Bitmap getNextFrame() {
        return null;
    }

    @Override
    public int read(@Nullable InputStream is, int contentLength) {
        return 0;
    }

    @Override
    public void clear() {

    }

    @Override
    public void setData(@NonNull APngHeader header, @NonNull byte[] data) {

    }

    @Override
    public void setData(@NonNull APngHeader header, @NonNull ByteBuffer buffer) {

    }

    @Override
    public void setData(@NonNull APngHeader header, @NonNull ByteBuffer buffer, int sampleSize) {

    }

    @Override
    public int read(@Nullable byte[] data) {
        return 0;
    }

    @Override
    public void setDefaultBitmapConfig(@NonNull Bitmap.Config format) {

    }
}
