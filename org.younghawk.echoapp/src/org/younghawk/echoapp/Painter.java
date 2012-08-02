package org.younghawk.echoapp;

import android.graphics.Canvas;

public interface Painter extends Runnable {
    public static enum TYPES {
        FULL, DIRTY
    };
    
    void onDraw(Canvas c);
}
