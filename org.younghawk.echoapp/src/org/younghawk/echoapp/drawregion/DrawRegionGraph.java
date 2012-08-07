package org.younghawk.echoapp.drawregion;

import org.younghawk.echoapp.PanelDrawer;
import org.younghawk.echoapp.handlerthreadfactory.HThread;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
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
    
    
    public DrawRegionGraph(PanelDrawer panel_drawer, Rect rect){
        this.rect = rect;
        this.mPanelDrawer = panel_drawer;
        this.mSelf = this;
    }

    public Rect getRect(){
        return rect;
    }
    
    //Deprecated
    //TODO:If you can't get rid of this hack at least code to the interface (not the implementation)
    //public void setCallback(PanelDrawer panel_drawer){
    //    //mCallback = panel_drawer;
    //}

    @Override
    public void run(SurfaceHolder holder) {
        Rect dirty_rect =rect;
        Log.d(TAG, "Drawing Current Audio Region");

        Canvas c = holder.lockCanvas(dirty_rect);
        //TODO: See if there's a way to factor this out from the thread
        try {
            if (c!=null) {
                synchronized (holder) {
                    drawOnSurface(c);
                    //paint.setColor(Color.GRAY);
                    //c.drawRect(dirty_rect, paint);
                    //c.drawBitmap(mScaledBitmap, dirty_rect.left, dirty_rect.top, null);
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
    
    public void onBitmapUpdate(Bitmap bitmap) {
        mScaledBitmap = scaleBitmap(bitmap, rect);
    }
    
    //TODO: Feels like this belongs somewhere else
    //TODO: We're drawing in the main thread :((((
    public void onVectorUpdate(float[] canvas_pts) {
        //CollectionGrapher cg = CollectionGrapher.create(0,0, rect.width(), rect.height(), vector);
        Paint paint = new Paint();
        paint.setColor(Color.CYAN);
        if (mScaledBitmap==null){
            Log.d(TAG, "Creating new bitmap");
            mScaledBitmap = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888);
        }

        Log.d(TAG, "Drawing points to canvas(bitmap)");
        synchronized (mScaledBitmap) {  
            Canvas c = new Canvas(mScaledBitmap);
            //c.drawPoints(cg.mCanvasPts, paint);
            c.drawPoints(canvas_pts, paint);
        }

        //TODO: Make up your mind, are we passing bitmaps around or 
        //using a common reference?
        //Log.d(TAG, "Making callback to update surface");
        //if(mPanelDrawer!=null){
        //    Log.d(TAG, "Callback to Panel Drawer");
        //    mPanelDrawer.onBitmapUpdate(mScaledBitmap);
        //} else {
        //    Log.w(TAG, "Callback was Null!!");
        //}

        //Draw updated bitmap
        Log.d(TAG, "Attempting to send runner to graphThread to scale bitmap");

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