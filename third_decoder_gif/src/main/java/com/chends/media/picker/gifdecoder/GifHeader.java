package com.chends.media.picker.gifdecoder;

import android.support.annotation.ColorInt;

import com.chends.media.picker.decoder.AnimDecoder;
import com.chends.media.picker.decoder.AnimHeader;

/**
 * A header object containing the number of frames in an animated GIF image as well as basic
 * metadata like width and height that can be used to decode each individual frame of the GIF. Can
 * be shared by one or more {@link AnimDecoder}s to play the same
 * animated GIF in multiple views.
 *
 * @see <a href="https://www.w3.org/Graphics/GIF/spec-gif89a.txt">GIF 89a Specification</a>
 */
public class GifHeader extends AnimHeader<GifFrame> {

  @ColorInt
  int[] gct = null;

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

}
