package org.younghawk.echoapp.drawregion;

import java.util.Arrays;

import org.younghawk.echoapp.PanelDrawer;
import org.younghawk.echoapp.handlerthreadfactory.HThread;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;

/**
 * This class is instantiated through the DrawRegionFactory.
 */
public class DrawRegionGraph implements DrawRegionType {
    public static final String TAG="EchoApp DrawRegionGraph";
    public Rect rect; //the rectangle that bounds the object
    public Bitmap mScaledBitmap;

    private PanelDrawer mPanelDrawer;
    private DrawRegionGraph mSelf;
    

    
    //Constructor
    public DrawRegionGraph(PanelDrawer panel_drawer, Rect rect){
        this.rect = rect;
        this.mPanelDrawer = panel_drawer;
        this.mSelf = this;
    }

    //TODO: See if getRect() is ever used (may need to change interface def)
    public Rect getRect(){
        return rect;
    }

    public void onBitmapUpdate(Bitmap bitmap){
        Log.d(TAG, "scaling bitmap");
        
        mScaledBitmap = scaleBitmap(bitmap, rect);
        
        //TODO: Move DrawRegion HashMaps to a DrawRegion home
        HThread graphThread = mPanelDrawer.mDrawRegionHThreads.get(DrawRegionNames.GRAPH);
        if (graphThread.isAlive() && graphThread.handler!=null){
            Log.d(TAG, "Attempting to draw scaled bitmap");
            graphThread.handler.post( new Runnable(){
                @Override
                public void run() {
                    mSelf.run(mPanelDrawer.mSurfaceHolder);

                };
            });
        }
    }
    

    @Override
    public void run(SurfaceHolder holder) {
        Rect dirty_rect =rect;
        Log.d(TAG, "Drawing Current Audio Region");

        Canvas c = holder.lockCanvas(dirty_rect);

        try {
            if (c!=null) {
                synchronized (holder) {
                    drawOnSurface(c);
                }
            } //TODO: capture data on why canvas would be null

        } finally {
            // do this in a finally so that if an exception is thrown
            // during the above, we don't leave the Surface in an
            // inconsistent state
            if (c != null) {
                holder.unlockCanvasAndPost(c);
            }
        }
        
    }
    

    
    private void drawOnSurface(Canvas c){
        c.drawBitmap(mScaledBitmap, rect.left, rect.top, null);
    }
    
    private Bitmap scaleBitmap(Bitmap orig_bitmap, Rect scale_rect){
        Bitmap scaled_bitmap = orig_bitmap;
        if (scaled_bitmap.getWidth()!=scale_rect.width() && scaled_bitmap.getHeight()!= scale_rect.height()){
            scaled_bitmap = Bitmap.createScaledBitmap(orig_bitmap, scale_rect.width(), scale_rect.height(), false);
        }
        return scaled_bitmap;
    }

}