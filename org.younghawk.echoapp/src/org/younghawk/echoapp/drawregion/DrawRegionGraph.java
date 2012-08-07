package org.younghawk.echoapp.drawregion;

import android.graphics.Bitmap;
import android.graphics.Canvas;
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

    public DrawRegionGraph(Rect rect){
        this.rect = rect;
    }

    public Rect getRect(){
        return rect;
    }

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