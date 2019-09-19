package com.chends.media.picker.decoder;

import android.support.annotation.ColorInt;


import java.util.ArrayList;
import java.util.List;

/**
 * A header object containing the number of frames in an animated GIF image as well as basic
 * metadata like width and height that can be used to decode each individual frame of the GIF. Can
 * be shared by one or more {@link AnimDecoder}s to play the same
 * animated GIF in multiple views.
 *
 * @see <a href="https://www.w3.org/Graphics/GIF/spec-gif89a.txt">GIF 89a Specification</a>
 */
public class AnimHeader {

  /** 永远循环 */
  public static final int NETSCAPE_LOOP_COUNT_FOREVER = 0;
  /** 不存在循环 */
  public static final int NETSCAPE_LOOP_COUNT_DOES_NOT_EXIST = -1;

  @ColorInt
  int[] gct = null;
  @AnimDecoder.AnimDecodeStatus
  int status = AnimDecoder.STATUS_OK;
  int frameCount = 0;

  AnimFrame currentFrame;
  final List<AnimFrame> frames = new ArrayList<>();
  /** 宽度 */
  int width;
  /** 高度 */
  int height;

  // 1 : global color table flag.
  boolean gctFlag;
  /**
   * Size of Global Color Table.
   * The value is already computed to be a regular number, this field doesn't store the exponent.
   */
  int gctSize;
  /** Background color index into the Global/Local color table. */
  int bgIndex;
  /**
   * Pixel aspect ratio.
   * Factor used to compute an approximation of the aspect ratio of the pixel in the original image.
   */
  int pixelAspect;
  @ColorInt
  int bgColor;
  int loopCount = NETSCAPE_LOOP_COUNT_DOES_NOT_EXIST;

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
