package org.younghawk.echoapp;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;

public abstract class FullPainter implements Painter {
    public static final String TAG = "EchoApp FullPainter";
    public static Painter.TYPES type = TYPES.FULL;
    
    private SurfaceHolder mHolder;
    
    public FullPainter(SurfaceHolder holder){
        this.mHolder = holder;
    }

    public void safeDraw(Canvas c, Paint paint){
        //Abstract
    }
    
    @Override
    public void run() {
        Log.d(TAG, "Running initial draw in thread");
        Canvas c = mHolder.lockCanvas(null);
        //TODO: See if there's a way to factor this out from the thread
        try {
            if (c!=null) {
                synchronized (mHolder) {
                    safeDraw(c, new Paint());
                }
            } //TODO: capture data on why canvas would be null

        } finally {
            // do this in a finally so that if an exception is thrown
            // during the above, we don't leave the Surface in an
            // inconsistent state
            if (c != null) {
                mHolder.unlockCanvasAndPost(c);
            }
        }
    }


    @Override
    public void onDraw(Canvas c) {
  
    }
}
