package org.younghawk.echoapp.drawregion;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

public class ScrollingBitmap {
    public static final String TAG="EchoApp ScrollingBitmap";
    public Bitmap mScrollingBitmap;
    
    //TODO: Defaults will be updated with scaled size from Draw Region
    private static final int DEFAULT_WIDTH = 400;
    private static final int DEFAULT_HEIGHT = 400;
    //TODO: Allow override of default width and height
    
    private  Bitmap mSliverBitmap;
    private  Rect mSliverSrcRect;
    private  Rect mSliverDstRect;
    private  Canvas mSliverCanvas;
    private Canvas mScrollingCanvas;
    private Bitmap mBufferBitmap;
    private Canvas mBufferCanvas;
    private int mRegionWidth;
    private int mRegionHeight;

    //Implemented as instance variables rather than local for performance reasons
    private Paint mPaint = new Paint();
    private Matrix mMatrix = new Matrix();

    //Factory
    public static ScrollingBitmap create(){
        Bitmap scroll_bmp = Bitmap.createBitmap(DEFAULT_WIDTH, DEFAULT_HEIGHT, Bitmap.Config.ARGB_8888);
        Bitmap buffer_bmp = Bitmap.createBitmap(DEFAULT_WIDTH, DEFAULT_HEIGHT, Bitmap.Config.ARGB_8888);
        Bitmap sliv_bmp   = Bitmap.createBitmap(1,DEFAULT_HEIGHT, Bitmap.Config.ARGB_8888);
        return new ScrollingBitmap(sliv_bmp, scroll_bmp, buffer_bmp);
    }
    
    
    //Constructor
    private ScrollingBitmap(Bitmap sliv_bmp, Bitmap scroll_bmp, Bitmap buffer_bmp){
        this.mRegionWidth = DEFAULT_WIDTH;
        this.mRegionHeight = DEFAULT_HEIGHT;
        setBitmaps(sliv_bmp, scroll_bmp, buffer_bmp, mRegionWidth, mRegionHeight);
        
        this.mMatrix.setTranslate(-1, 0);   //translate left by one
    }
    
    private void setBitmaps(Bitmap sliv_bmp, Bitmap scroll_bmp, Bitmap buffer_bmp, int height, int width){
        this.mRegionWidth = width;
        this.mRegionHeight = height;
        this.mSliverBitmap = sliv_bmp;
        this.mSliverSrcRect = new Rect(0, 0, 1, sliv_bmp.getHeight());
        this.mSliverDstRect = new Rect(
                mRegionWidth-sliv_bmp.getWidth(),
                0,
                mRegionWidth,
                sliv_bmp.getHeight());
        this.mSliverCanvas = new Canvas(sliv_bmp);
        this.mBufferBitmap = buffer_bmp;
        this.mBufferCanvas = new Canvas(buffer_bmp);
        this.mScrollingBitmap = scroll_bmp;
        this.mScrollingCanvas = new Canvas(scroll_bmp);
    }
    
    
    //TODO: Implement Observer (use a callback interface)
    private DrawRegionGraph mGraphDrawRegionCallback;
    
    
    public void onVectorUpate(float[] vector_pts, float max, float min){

        //Only do work if there's a callback to receive it
        if(mGraphDrawRegionCallback!=null){
            //convert vector to canvas
            float[] canvas_pts = new float[2*vector_pts.length];
            int i=0;
            while(i<canvas_pts.length){
                canvas_pts[i] = 0; //mRegionWidth-1;

                //If both positive and negative values
                float scale_factor = mRegionHeight/(max - min);
                canvas_pts[i+1] = (vector_pts[i/2]*scale_factor) +mRegionHeight/2;
                i+=2;
            }

            mPaint.setColor(Color.CYAN);
            mSliverCanvas.drawPoints(canvas_pts, mPaint);
            
            //copy the sliver onto the previous bitmap in the (should be empty) sliver spot
            //draw the previous bitmap to the canvas
            mBufferCanvas.drawBitmap(mSliverBitmap, mSliverSrcRect, mSliverDstRect, mPaint);

            //synchronized (mScrollingBitmap) {  
                mScrollingCanvas.drawBitmap(mBufferBitmap, 0, 0, mPaint);

                this.mGraphDrawRegionCallback.onBitmapUpdate(mScrollingBitmap);

                //Clear the existing sliver points
                mSliverCanvas.drawColor(Color.DKGRAY);

                //shift the full bitmap buffer down for the next sliver
                mBufferCanvas.drawBitmap(mBufferBitmap, mMatrix, mPaint);

        }

    }

    
    public void setDrawRegion(DrawRegionType draw_reg){
        mGraphDrawRegionCallback = (DrawRegionGraph) draw_reg;
        
        //Set the instance height and width to the scaled values
        mRegionHeight = mGraphDrawRegionCallback.rect.height();
        mRegionWidth =  mGraphDrawRegionCallback.rect.width();
    }
}
