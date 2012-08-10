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
    //Refactor candidates
    private Bitmap mPreviousBitmap;
    private Canvas mPreviousCanvas;
    private Bitmap mNewSliverBitmap;
    private Canvas mNewSliverCanvas;
    private Rect mSliverRect;
    
    private int testing;

    private PanelDrawer mPanelDrawer;
    private DrawRegionGraph mSelf;
    //private ArrayList<Float> testpts = new ArrayList<Float>();
    //private float[] testpts = new float[]{0,0};

    
    //Constructor
    public DrawRegionGraph(PanelDrawer panel_drawer, Rect rect){
        this.rect = rect;
        if(rect!=null){
            mSliverRect = new Rect(rect.width()-1,0,rect.width(),rect.height());
        }
        this.mPanelDrawer = panel_drawer;
        this.mSelf = this;
    }

    //TODO: See if getRect() is ever used (may need to change interface def)
    public Rect getRect(){
        return rect;
    }

    public void onBitmapUpdate(Bitmap bitmap){
        Log.d(TAG, "scaling bitmap");
        
      //TESTING
        //testing++;
        //Canvas c = new Canvas(bitmap);
        //Paint paint = new Paint();
        //paint.setColor(Color.GREEN);
        //c.drawText("G"+testing, 20+(float)xxtesting, 20+(float)xxtesting, paint);
        
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
    
    //TODO: Feels like this belongs somewhere else
    //TODO: We're drawing in the main thread :((((
    public void onVectorUpdate(float[] vert_pts) {

        //convert vector to canvas
        float[] canvas_pts = new float[2*vert_pts.length];
        int i=0;
        while(i<canvas_pts.length){
            canvas_pts[i] = rect.width()-1;
            
            //If both positive and negative values 
            canvas_pts[i+1] = vert_pts[i/2]+rect.height()/2;
            i+=2;
        }
        //CollectionGrapher cg = CollectionGrapher.create(0,0, rect.width(), rect.height(), vector);
        Paint paint = new Paint();
        Matrix matrix = new Matrix();
        matrix.setTranslate(-1,0);
        paint.setColor(Color.CYAN);
        if (mScaledBitmap==null){
            Log.d(TAG, "Creating new bitmap");
            mScaledBitmap = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888);
        }        
        if(mPreviousBitmap==null){
            Log.d(TAG, "Creating full buffered bitmap");
            mPreviousBitmap = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888);
            mPreviousCanvas = new Canvas(mPreviousBitmap);
        }
        if(mNewSliverBitmap==null){
            Log.d(TAG, "Creating slivered buffered bitmap");
            mNewSliverBitmap = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888);
            mNewSliverCanvas = new Canvas(mNewSliverBitmap);
        }

        //Log.d(TAG, "Drawing points to canvas(bitmap): " + Arrays.toString(canvas_pts));
        synchronized (mScaledBitmap) {  
            Canvas c = new Canvas(mScaledBitmap);

            //Draw this vertical sliver of vectored points
            mNewSliverCanvas.drawPoints(canvas_pts, paint);
            //copy the sliver onto the previous bitmap in the (should be empty) sliver spot
            //draw the previous bitmap to the canvas
            mPreviousCanvas.drawBitmap(mNewSliverBitmap, mSliverRect, mSliverRect, paint);
            
            //Draw to the actual canvas
            c.drawBitmap(mPreviousBitmap, 0, 0, paint);
            
            //Clear the existing sliver points
            //mNewSliverCanvas.drawColor(Color.DKGRAY);
            
            //shift the full bitmap buffer down for the next sliver
            mPreviousCanvas.drawBitmap(mPreviousBitmap, matrix, paint);
            //c.drawColor(Color.DKGRAY);
            //c.drawPoints(canvas_pts, paint);
        }

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
    }
    
    //public void tempInitPtAdder(float x, float y){
    //    testpts.add(x);
    //    testpts.add(y);
    //}
    
    public void test(){
        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        if (mScaledBitmap==null){
            Log.d(TAG, "Creating new bitmap");
            mScaledBitmap = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888); 
        }
        if(mPreviousBitmap==null){
            Log.d(TAG, "Creating buffered bitmap");
            mPreviousBitmap = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888);
            mPreviousCanvas = new Canvas(mPreviousBitmap);
        }
        if(mNewSliverBitmap==null){
            Log.d(TAG, "Creating buffered bitmap");
            mNewSliverBitmap = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888);
            mNewSliverCanvas = new Canvas(mNewSliverBitmap);
        }
        //A single pixel sliver rectangle
        Rect sliver_rect = new Rect(rect.width()-1,0,rect.width(),rect.height());
        //Rect sliver_dst_rect = new Rect(rect.width()-2,0,rect.width()-1,rect.height());
        Matrix matrix = new Matrix();
        matrix.setTranslate(-1,0);

        Log.d(TAG, "Drawing TEST points to canvas(bitmap)");
        synchronized (mScaledBitmap) {  
            Canvas c = new Canvas(mScaledBitmap);
            //c.drawColor(Color.DKGRAY);
            
            //10 random heights
            float[] vert_pts = new float[10];
            for(int i=0;i<vert_pts.length;i++){
                vert_pts[i] = (float) Math.random()*rect.height();
            }
            //assign the pts to the last sliver of the canvas
            float[] canvas_pts = new float[2*vert_pts.length];
            int i=0;
            while(i<canvas_pts.length){
                //if this doesn't work try width-1
                canvas_pts[i] = rect.width()-1;
                canvas_pts[i+1] = vert_pts[i/2];
                i+=2;
            }
            
            //Bitmap temp_bmp = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888);
            //Canvas temp_c = new Canvas(temp_bmp);
            //mNewSliverCanvas.drawColor(Color.YELLOW);
            mNewSliverCanvas.drawPoints(canvas_pts, paint);
            //working -> mPreviousCanvas.drawBitmap(mNewSliverBitmap, matrix, paint);
     
            //copy the sliver onto the previous bitmap in the (should be empty) sliver spot
            //draw the previous bitmap to the canvas
            mPreviousCanvas.drawBitmap(mNewSliverBitmap, sliver_rect, sliver_rect, paint);
            
            
            
            
            //Draw the previous sliver bitmap onto  
            //mPreviousCanvas.drawBitmap(current_bitmap, rect, mShiftedRect, paint);  
            
            //mPreviousCanvas.drawPoints(canvas_pts, paint);
            //mPreviousCanvas.translate(-1, 0);
            //c.drawBitmap(mNewSliverBitmap, 0 ,0, null);
            
            c.drawBitmap(mPreviousBitmap, 0, 0, paint);
            mNewSliverCanvas.drawColor(Color.DKGRAY);
            
          //shift the previous bitmap down leaving an empty spot for the next update
            mPreviousCanvas.drawBitmap(mPreviousBitmap, matrix, paint);
            
        }

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