package com.chends.media.picker.decoder;

import java.util.ArrayList;
import java.util.List;

/**
 * A header object containing the number of frames in an animated Anim image as well as basic
 * metadata like width and height that can be used to decode each individual frame of the Anim. Can
 * be shared by one or more {@link AnimDecoder}s to play the same
 * animated Anim in multiple views.
 *
 */
public class AnimHeader<T extends AnimFrame> {

  /** 永远循环 */
  public static final int NETSCAPE_LOOP_COUNT_FOREVER = 0;
  /** 不存在循环 */
  public static final int NETSCAPE_LOOP_COUNT_DOES_NOT_EXIST = -1;

  @AnimDecoder.AnimDecodeStatus
  public int status = AnimDecoder.STATUS_OK;
  public int frameCount = 0;

  public T currentFrame;
  public final List<T> frames = new ArrayList<>();
  /** 宽度 */
  public int width;
  /** 高度 */
  public int height;

  public int loopCount = NETSCAPE_LOOP_COUNT_DOES_NOT_EXIST;

  public int getHeight() {
    return height;
  }

  public int getWidth() {
    return width;
  }

  public int getNumFrames() {
    return frameCount;
  }

  /**
   * 获取状态
   */
  @AnimDecoder.AnimDecodeStatus
  public int getStatus() {
    return status;
  }
}
