package com.joker.smartdengg_smatrzoom.util;

import android.graphics.Rect;

/**
 * Created by Joker on 2015/7/25.
 */
public class Utils {

  public static float calculateScale(Rect startBounds, Rect finalBounds) {

    float scale;
    if ((float) finalBounds.width() / finalBounds.height() > (float) startBounds.width() / startBounds.height()) {
      /* Extend start bounds horizontally*/
      scale = (float) startBounds.height() / finalBounds.height();
      float startWidth = scale * finalBounds.width();
      float deltaWidth = (startWidth - startBounds.width()) / 2;
      startBounds.left -= deltaWidth;
      startBounds.right += deltaWidth;
    } else {
      /* Extend start bounds vertically*/
      scale = (float) startBounds.width() / finalBounds.width();
      float startHeight = scale * finalBounds.height();
      float deltaHeight = (startHeight - startBounds.height()) / 2;
      startBounds.top -= deltaHeight;
      startBounds.bottom += deltaHeight;
    }

    return scale;
  }
}
