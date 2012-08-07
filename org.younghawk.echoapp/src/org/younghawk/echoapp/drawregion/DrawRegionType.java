package org.younghawk.echoapp.drawregion;

import android.graphics.Rect;
import android.view.SurfaceHolder;

/**
 * A region must at a minimum describe the area that it will
 * use (getRect).
 */
public interface DrawRegionType {
    public Rect getRect();
    public void run(SurfaceHolder holder);
}
