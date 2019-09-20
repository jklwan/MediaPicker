package com.chends.media.picker.decoder;

import android.graphics.Bitmap;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.ByteBuffer;

/**
 * 动画解析
 * @author chends create on 2019/9/19.
 */
public interface AnimDecoder<T extends AnimHeader> {
    int INITIAL_FRAME_POINTER = -1;
    int BYTES_PER_INTEGER = Integer.SIZE / 8;
    /**
     * 正常状态
     */
    int STATUS_OK = 0;
    /**
     * 解码失败
     */
    int STATUS_FORMAT_ERROR = 1;
    /**
     * 打开文件失败
     */
    int STATUS_OPEN_ERROR = 2;
    /**
     * 无法解码当前帧
     */
    int STATUS_PARTIAL_DECODE = 3;
    /**
     * 无限期重复
     */
    int TOTAL_ITERATION_COUNT_FOREVER = 0;

    /**
     * 动画状态
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(value = {STATUS_OK, STATUS_FORMAT_ERROR, STATUS_OPEN_ERROR, STATUS_PARTIAL_DECODE})
    @interface AnimDecodeStatus {
    }

    /**
     * 宽度
     * @return width
     */
    int getWidth();

    /**
     * 高度
     * @return height
     */
    int getHeight();

    /**
     * 数据
     * @return data
     */
    @NonNull
    ByteBuffer getData();

    /**
     * 状态
     * 没帧都进行更新
     * @return status
     */
    @AnimDecodeStatus
    int getStatus();

    /**
     * 移动帧
     */
    void advance();

    /**
     * 获取帧时长
     * @param n 帧下标
     * @return 帧时长：毫秒
     */
    int getDelay(int n);

    /**
     * 显示下一帧的等待时长
     */
    int getNextDelay();

    /**
     * 帧总数
     * @return frame count.
     */
    int getFrameCount();

    /**
     * 当前帧下标，-1表示为开始
     * @return frame index.
     */
    int getCurrentFrameIndex();

    /**
     * 重置帧下标
     */
    void resetFrameIndex();

    /**
     * Gets the "Netscape" loop count, if any.
     * A count of 0 ({@link AnimHeader#NETSCAPE_LOOP_COUNT_FOREVER}) means repeat indefinitely.
     * It must not be a negative value.
     * <br>
     * Use {@link #getTotalIterationCount()}
     * to know how many times the animation sequence should be displayed.
     * @return loop count if one was specified,
     * else -1 ({@link AnimHeader#NETSCAPE_LOOP_COUNT_DOES_NOT_EXIST}).
     */
    int getNetscapeLoopCount();

    /**
     * Gets the total count
     * which represents how many times the animation sequence should be displayed.
     * A count of 0 ({@link #TOTAL_ITERATION_COUNT_FOREVER}) means repeat indefinitely.
     * It must not be a negative value.
     * <p>
     * The total count is calculated as follows by using {@link #getNetscapeLoopCount()}.
     * This behavior is the same as most web browsers.
     * <table border='1'>
     * <tr class='tableSubHeadingColor'><th>{@code getNetscapeLoopCount()}</th>
     * <th>The total count</th></tr>
     * <tr><td>{@link AnimHeader#NETSCAPE_LOOP_COUNT_FOREVER}</td>
     * <td>{@link #TOTAL_ITERATION_COUNT_FOREVER}</td></tr>
     * <tr><td>{@link AnimHeader#NETSCAPE_LOOP_COUNT_DOES_NOT_EXIST}</td>
     * <td>{@code 1}</td></tr>
     * <tr><td>{@code n (n > 0)}</td>
     * <td>{@code n + 1}</td></tr>
     * </table>
     * </p>
     * @return total iteration count calculated from "Netscape" loop count.
     */
    int getTotalIterationCount();

    /**
     * Returns an estimated byte size for this decoder based on the data provided to {@link
     * #setData(AnimHeader, byte[])}, as well as internal buffers.
     */
    int getByteSize();

    /**
     * Get the next frame in the animation sequence.
     * @return Bitmap representation of frame.
     */
    @Nullable
    Bitmap getNextFrame();

    /**
     * Reads Anim image from stream.
     * @param is containing Anim file.
     * @return read status code (0 = no errors).
     */
    @AnimDecodeStatus
    int read(@Nullable InputStream is, int contentLength);

    void clear();

    void setData(@NonNull T header, @NonNull byte[] data);

    void setData(@NonNull T header, @NonNull ByteBuffer buffer);

    void setData(@NonNull T header, @NonNull ByteBuffer buffer, int sampleSize);

    /**
     * Reads Anim image from byte array.
     * @param data containing Anim file.
     * @return read status code (0 = no errors).
     */
    @AnimDecodeStatus
    int read(@Nullable byte[] data);

    /**
     * Sets the default {@link Bitmap.Config} to use when decoding frames of a Anim.
     *
     * <p>Valid options are {@link Bitmap.Config#ARGB_8888} and
     * {@link Bitmap.Config#RGB_565}.
     * {@link Bitmap.Config#ARGB_8888} will produce higher quality frames, but will
     * also use 2x the memory of {@link Bitmap.Config#RGB_565}.
     *
     * <p>Defaults to {@link Bitmap.Config#ARGB_8888}
     *
     * <p>This value is not a guarantee. For example if set to
     * {@link Bitmap.Config#RGB_565} and the Anim contains transparent pixels,
     * {@link Bitmap.Config#ARGB_8888} will be used anyway to support the
     * transparency.
     */
    void setDefaultBitmapConfig(@NonNull Bitmap.Config config);
}
