package org.younghawk.echoapp;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;

public class PanelManager {
    private static final String TAG = "EchoApp PanelManager";
  //This class should be a singleton
    private static PanelManager instance = null;
    
    public SurfaceHolder mSurfaceHolder;
    
    private Executor executor = Executors.newFixedThreadPool(2);
    private Paint paint = new Paint();

    //Panel Manager should only be created after Panel
    //is done initializing
    public static PanelManager create(Panel panel) {
        if(instance!=null){
            return instance;
        } else {
            //Setup Surface Holder
            SurfaceHolder panelSurfaceHolder = panel.getHolder();

            //Setup the callbacks on panel
            panel.getHolder().addCallback(panel);


            return new PanelManager(panelSurfaceHolder);
        }
    }
    private PanelManager(SurfaceHolder surfHold) {
        this.mSurfaceHolder = surfHold;
        initialDraw();
    }
    
    private void initialDraw() {
        executor.execute(new Runnable(){
            @Override
            public void run() {
                Log.d(TAG, "Running initial draw in thread");
                Canvas c = mSurfaceHolder.lockCanvas(null);
                try {
                    if (c!=null) {
                        synchronized (mSurfaceHolder) {
                            paint.setColor(Color.YELLOW);
                        }
                    } //TODO: capture data on why canvas would be null

                } finally {
                    // do this in a finally so that if an exception is thrown
                    // during the above, we don't leave the Surface in an
                    // inconsistent state
                    if (c != null) {
                        mSurfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }

        });
    }
    private void defaultBitmap(){
        //BitmapConfig conf = Bitmap.Config.ARGB_8888; // see other conf types
        //Bitmap bmp = Bitmap.createBitmap(w, h, conf); // this creates a MUTABLE bitmap
        //Canvas temp = new Canvas(bmp);
    }
}
