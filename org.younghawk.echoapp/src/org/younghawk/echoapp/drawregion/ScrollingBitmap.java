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
    private float[] mCanvasPts;
    private int mCanvasPtsSize;
    private int mPreviousCanvasPtsSize;

    //Implemented as instance variables rather than local for performance reasons
    private Paint mPaint = new Paint();
    private Matrix mMatrix = new Matrix();
    
    //Debug 
    private long counter=0;
    private long now=0;
    private long start_time=0;
    private long elapsed_time=0;

    //Factory
    public static ScrollingBitmap create(){
        Bitmap scroll_bmp = Bitmap.createBitmap(DEFAULT_WIDTH, DEFAULT_HEIGHT, Bitmap.Config.ARGB_8888);
        //Bitmap sliv_bmp   = Bitmap.createBitmap(1,DEFAULT_HEIGHT, Bitmap.Config.ARGB_8888);
        return new ScrollingBitmap(scroll_bmp);
    }
    
    
    //Constructor
    private ScrollingBitmap(Bitmap scroll_bmp){
        this.mRegionWidth = DEFAULT_WIDTH;
        this.mRegionHeight = DEFAULT_HEIGHT;
        setBitmaps(scroll_bmp, mRegionWidth, mRegionHeight);
        
        this.mMatrix.setTranslate(-1, 0);   //translate left by one
    }
    
    private void setBitmaps(Bitmap scroll_bmp, int height, int width){
        this.mRegionWidth = width;
        this.mRegionHeight = height;
        this.mSliverBitmap = Bitmap.createBitmap(1,height, Bitmap.Config.ARGB_8888);;
        this.mSliverSrcRect = new Rect(0, 0, 1, height);
        this.mSliverDstRect = new Rect(
                mRegionWidth-mSliverBitmap.getWidth(),
                0,
                mRegionWidth,
                mSliverBitmap.getHeight());
        this.mSliverCanvas = new Canvas(mSliverBitmap);
        this.mScrollingBitmap = scroll_bmp;
        this.mScrollingCanvas = new Canvas(scroll_bmp);
    }
    
    
    //TODO: Implement Observer (use a callback interface)
    public DrawRegionGraph mGraphDrawRegionCallback;
    
    //TODO: Move max and min to constructor (or even better a setter);
    public void onVectorUpate(float[] vector_pts, float max, float min){
        
        //TEMPORARY DEBUG CODE
        //double vups = 0.0;
        now = System.currentTimeMillis();
        if(start_time==0){
            start_time=now;
        }
        elapsed_time = now - start_time;
        counter++;
        if(elapsed_time>5*1000){
            double vups = (1000 * counter/(double) (elapsed_time));
            Log.d(TAG, "Benchmark Vector Updates per second: " + vups + "Time: "+ elapsed_time/1000 + "s");
            start_time = 0;
            now = 0;
            counter=0;
            elapsed_time=0;
        }
        
            
        

        //Only do work if there's a callback to receive it
        if(mGraphDrawRegionCallback!=null){
            //convert vector to canvas
            mCanvasPtsSize = 2*vector_pts.length;
            if(mCanvasPts==null){
                mCanvasPts = new float[mCanvasPtsSize];
            }
            if(mCanvasPtsSize!=mPreviousCanvasPtsSize){
                mCanvasPts = new float[mCanvasPtsSize];
            }
            //float[] canvas_pts = new float[2*vector_pts.length];
            int i=0;
            while(i<mCanvasPtsSize){
                mCanvasPts[i] = 0; //mRegionWidth-1;

                //TODO: optimize
                float scale_factor = mRegionHeight/(max - min);
                mCanvasPts[i+1] = (vector_pts[i/2]*scale_factor) +mRegionHeight/2;
                i+=2;
            }

            mPaint.setColor(Color.CYAN);
            mSliverCanvas.drawPoints(mCanvasPts, mPaint);
            
            //synchronized (mScrollingBitmap) {  
                //copy the sliver onto the scrolling bitmap (which should now have an empty slot for sliver)
                mScrollingCanvas.drawBitmap(mSliverBitmap, mSliverSrcRect, mSliverDstRect, mPaint);


                this.mGraphDrawRegionCallback.onBitmapUpdate(mScrollingBitmap);

                //Clear the existing sliver points
                mSliverCanvas.drawColor(Color.DKGRAY);

                //shift the full bitmap buffer down for the next sliver
                mScrollingCanvas.drawBitmap(mScrollingBitmap, mMatrix, mPaint);
                
          //BE SURE THIS IS CALLED AFTER ANY USE OF CANVAS PTS
          mPreviousCanvasPtsSize = mCanvasPtsSize;
        }

    }

    
    public void setDrawRegion(DrawRegionType draw_reg){
        mGraphDrawRegionCallback = (DrawRegionGraph) draw_reg;
        
        //Set the instance height and width to the scaled values
        int new_h = mGraphDrawRegionCallback.rect.height();
        int new_w =  mGraphDrawRegionCallback.rect.width();
        //Bitmap scroll_bmp = mGraphDrawRegionCallback.mScaledBitmap;
        Bitmap scroll_bmp = Bitmap.createScaledBitmap(mScrollingBitmap, new_w, new_h, false);
        //Bitmap sliv_bmp   = Bitmap.createScaledBitmap(mSliverBitmap, 1, new_h, false);
        setBitmaps(scroll_bmp, new_h, new_w);
    }
}
