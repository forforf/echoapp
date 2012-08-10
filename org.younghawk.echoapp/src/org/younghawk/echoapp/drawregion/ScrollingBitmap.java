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
    
    //TODO: Develop strategy for bitmap height and width
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
    
    //Remove when not needed
    //private int testing;
    
    
    public static ScrollingBitmap create(){
        //single pixel rectangle
        //Rect sliv_rect = new Rect(0, 0, 1, DEFAULT_HEIGHT);
        Bitmap scroll_bmp = Bitmap.createBitmap(DEFAULT_WIDTH, DEFAULT_HEIGHT, Bitmap.Config.ARGB_8888);
        Bitmap buffer_bmp = Bitmap.createBitmap(DEFAULT_WIDTH, DEFAULT_HEIGHT, Bitmap.Config.ARGB_8888);
        Bitmap sliv_bmp   = Bitmap.createBitmap(1,DEFAULT_HEIGHT, Bitmap.Config.ARGB_8888);
        return new ScrollingBitmap(sliv_bmp, scroll_bmp, buffer_bmp);
    }
    
    private ScrollingBitmap(Bitmap sliv_bmp, Bitmap scroll_bmp, Bitmap buffer_bmp){
        this.mSliverBitmap = sliv_bmp;
        this.mSliverSrcRect = new Rect(0, 0, 1, sliv_bmp.getHeight());
        this.mSliverDstRect = new Rect(
                DEFAULT_WIDTH-sliv_bmp.getWidth(),
                0,
                DEFAULT_WIDTH,
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
        //Log.d(TAG, "Vector Update in Scrolling Bitmap called - callback is: " + mGraphDrawRegionCallback);
        //testing++;
        //Only do work if there's a callback to receive it
        if(mGraphDrawRegionCallback!=null){
            //convert vector to canvas
            float[] canvas_pts = new float[2*vector_pts.length];
            int i=0;
            while(i<canvas_pts.length){
                canvas_pts[i] = 0; //DEFAULT_WIDTH-1;

                //If both positive and negative values
                float scale_factor = DEFAULT_HEIGHT/(max - min);
                canvas_pts[i+1] = (vector_pts[i/2]*scale_factor) +DEFAULT_HEIGHT/2;
                i+=2;
            }

            Paint paint = new Paint();
            Matrix matrix = new Matrix();
            matrix.setTranslate(-1,0);

            paint.setColor(Color.CYAN);

            //I don't think we need here
            //if (mScrollingBitmap==null){
            //   Log.d(TAG, "Creating new bitmap");
            //   mScrollingBitmap = Bitmap.createBitmap(DEFAULT_WIDTH, DEFAULT_HEIGHT, Bitmap.Config.ARGB_8888);
            //}        

            //We set these up in the constructor
            //Log.d(TAG, "Creating full buffered bitmap");
            //mBufferBitmap = Bitmap.createBitmap(DEFAULT_WIDTH, DEFAULT_HEIGHT, Bitmap.Config.ARGB_8888);
            //mBufferCanvas = new Canvas(mBufferBitmap);


            //if(mSliverBitmap==null){
            //    Log.d(TAG, "Creating slivered buffered bitmap");
            //    mSliverBitmap = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888);
            //    mNewSliverCanvas = new Canvas(mNewSliverBitmap);
            //}

            //Log.d(TAG, "Drawing points to canvas(bitmap): " + Arrays.toString(canvas_pts));

            //Canvas c = new Canvas(mScaledBitmap);

            //Draw this vertical sliver of vectored points
            

            mSliverCanvas.drawPoints(canvas_pts, paint);
            
            //TESTING
            //float[] test_pts = new float[]{0,0,0,100,0,200,0,300,0,400};
            //mSliverCanvas.drawPoints(test_pts, paint);
            //TESTING
            //Canvas dbgcc = new Canvas(mSliverBitmap);
            //dbgcc.drawColor(Color.YELLOW);
            


            //copy the sliver onto the previous bitmap in the (should be empty) sliver spot
            //draw the previous bitmap to the canvas
            mBufferCanvas.drawBitmap(mSliverBitmap, mSliverSrcRect, mSliverDstRect, paint);

            //TESTING
            //String tx1 = "B" + testing + mBufferBitmap.isMutable();
            //if(testing<30){
            //    paint.setColor(Color.RED);
            //    mBufferCanvas.drawText(tx1,  100, 100, paint);
            //}


            //mScrollingCanvas.drawBitmap(mSliverBitmap, DEFAULT_WIDTH-50, 0, paint);

            //mBufferCanvas.drawBitmap(mBufferBitmap, matrix, paint);

            //Debug - works here
            //Canvas dbgcc = new Canvas(mSliverBitmap);
            //dbgcc.drawColor(Color.YELLOW);
            //Canvas dbgc = new Canvas(mScrollingBitmap);
            //dbgc.drawBitmap(mSliverBitmap,0,0,paint);

            //mScrollingCanvas.drawBitmap(mScrollingBitmap, matrix, paint);


            //synchronized (mScrollingBitmap) {  
                mScrollingCanvas.drawBitmap(mBufferBitmap, 0, 0, paint);

                //TESTING
                //String tx2 = "S" + testing + mScrollingBitmap.isMutable();
                //if(testing<30){
                //    paint.setColor(Color.CYAN);
                //    mScrollingCanvas.drawText(tx2,  40, 40, paint);
                //}
                
                
                //TESTING
                //String tx4 = "F" + testing;
                //paint.setColor(Color.CYAN);
                //mScrollingCanvas.drawText(tx2,  40+(float)testing, 40+(float)testing, paint);
               
              //callback is working
                //Log.d(TAG, "Callback for drawing: " + testing);
                
                //TESTING
                //testing++;
                
                //CALLBACK
                //With ADDITIONAL PARAMTER FOR TESTING
                this.mGraphDrawRegionCallback.onBitmapUpdate(mScrollingBitmap);

                Log.d(TAG, "Retrund from Callback for drawing: ");



                //Clear the existing sliver points
                mSliverCanvas.drawColor(Color.DKGRAY);




                //shift the full bitmap buffer down for the next sliver
                mBufferCanvas.drawBitmap(mBufferBitmap, matrix, paint);

                //TESTING
                //String tx3 = "S2" + testing;
                //if(testing<30){
                //    paint.setColor(Color.CYAN);
                //    mScrollingCanvas.drawText(tx3,  40, 60, paint);
                //}
                //c.drawColor(Color.DKGRAY);
                //c.drawPoints(canvas_pts, paint);

                //this.mGraphDrawRegionCallback.onBitmapUpdate(mBufferBitmap);




            //}//Synchronized

            /*
            //Draw updated bitmap
            Log.d(TAG, "Launching runner for graphing updated GRAPH Bitmap");

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
             */
        }

    }

    
    public void setDrawRegion(DrawRegionType draw_reg){
        mGraphDrawRegionCallback = (DrawRegionGraph) draw_reg;
    }
    
    private void lazyLoadMe(){
        
    }
    
    

}
