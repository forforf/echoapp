package org.younghawk.echoapp.drawregion;

import android.graphics.Rect;

/**
 * A region must at a minimum describe the area that it will
 * use (getRect).
 */
public interface DrawRegionType {
    public Rect getRect();
}
