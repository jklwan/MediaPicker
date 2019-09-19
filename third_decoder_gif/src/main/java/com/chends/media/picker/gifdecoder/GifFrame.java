package com.chends.media.picker.gifdecoder;

import android.support.annotation.ColorInt;

import com.chends.media.picker.decoder.AnimFrame;

/**
 * Inner model class housing metadata for each frame.
 *
 * @see <a href="https://www.w3.org/Graphics/GIF/spec-gif89a.txt">GIF 89a Specification</a>
 */
class GifFrame extends AnimFrame {

  int ix, iy, iw, ih;
  /**
   * Control Flag.
   */
  boolean interlace;
  /**
   * Control Flag.
   */
  boolean transparency;
  /**
   * Transparency Index.
   */
  int transIndex;
  /**
   * Local Color Table.
   */
  @ColorInt
  int[] lct;
}
