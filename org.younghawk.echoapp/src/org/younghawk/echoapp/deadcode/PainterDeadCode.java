package org.younghawk.echoapp.deadcode;

import android.graphics.Canvas;

public interface PainterDeadCode extends Runnable {
    public static enum TYPES {
        FULL, DIRTY
    };
    
    void onDraw(Canvas c);
}
