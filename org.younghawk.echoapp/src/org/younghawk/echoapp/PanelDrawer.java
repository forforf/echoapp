package org.younghawk.echoapp;

import java.util.concurrent.ConcurrentHashMap;

import org.younghawk.echoapp.drawregion.DrawRegionFactory;
import org.younghawk.echoapp.drawregion.DrawRegionGraph;
import org.younghawk.echoapp.drawregion.DrawRegionNames;
import org.younghawk.echoapp.drawregion.DrawRegionRadar;
import org.younghawk.echoapp.drawregion.DrawRegionType;
import org.younghawk.echoapp.handlerthreadfactory.HThread;
import org.younghawk.echoapp.handlerthreadfactory.HandlerThreadExecutor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;

//TODO: Rename to PanelDraw?
public class PanelDrawer {
    private static final String TAG = "EchoApp PanelDrawer";
    //This class should be a singleton
    private static PanelDrawer instance = null;
    
    //Reference back to the Panel View
    private Panel mPanel;
    
    //The Thread Executor we use. It can create looped handlers as well as execute runnables
    private static HandlerThreadExecutor mExecutor = new HandlerThreadExecutor("paneldrawer");
    
    //Surface references
    public SurfaceHolder mSurfaceHolder;
    public ImmutableRect mSurfaceRect;
    
    //will be used to define the regions we can use to draw on the surface
    //Depends on the surface being ready before we can add any regions
    public ConcurrentHashMap<DrawRegionNames, DrawRegionType> mDrawRegionAreas = new ConcurrentHashMap<DrawRegionNames, DrawRegionType>();
    
    //Each region gets its own dedicated handler for queuing draw requests
    public ConcurrentHashMap<DrawRegionNames, HThread> mDrawRegionHThreads = new ConcurrentHashMap<DrawRegionNames, HThread>();
    
    //Because we do our drawing operations in runnables, there's a java issue of access to local variables
    //while defining these runnables. We get around this by using instance variables. Not ideal, but I'm not sure of
    //a better way
    //TODO: Is there better way to handle scaling bitmaps??
    private Bitmap mOrigBitmap;
    private Bitmap mScaledBitmap;
    

    //Panel Manager should only be created after Panel
    //is done initializing
    public static PanelDrawer create(Panel panel) {
        //ensure singleton
        if(instance!=null){
            return instance;
        } else {

            //Setup Surface Holder
            SurfaceHolder panelSurfaceHolder = panel.getHolder();
            
            //Setup the callbacks on panel
            //We do it here because rather than the panel constructor
            //so that we can be sure panel is fully initialized.
            panel.getHolder().addCallback(panel);

            
            
            
            
            
            
            //set as the singleton
            instance = new PanelDrawer(panel);
            return instance;
        }
    }
    
    //Constructor
    private PanelDrawer(Panel panel) {
        Log.d(TAG, "Constructing PanelDrawer");
        this.mPanel = panel;
 
    }
    
    public void onSurfaceReady() {
        Log.d(TAG, "Surface Ready tasks");
        this.mSurfaceHolder = mPanel.getHolder();
        //Get the surface dimensions
        this.mSurfaceRect = mPanel.mSurfaceRect;
        
        //Now that we know the surface dimensions we can create the drawing regions
        this.mDrawRegionAreas.put(DrawRegionNames.RADAR, DrawRegionFactory.radarRegion(mSurfaceRect));
        this.mDrawRegionAreas.put(DrawRegionNames.GRAPH, DrawRegionFactory.graphRegion(mSurfaceRect));
        
        //If the radar drawing thread doesn't exist create it
        if(this.mDrawRegionHThreads.containsKey(DrawRegionNames.RADAR)){ //contains key
            if(!this.mDrawRegionHThreads.get(DrawRegionNames.RADAR).isAlive()){ //but not alive
                this.mDrawRegionHThreads.put(DrawRegionNames.RADAR, mExecutor.execute(null, "radarHandler-reborn"));  //create thread  
            } //if it's alive then we're ok
        } else { //doesn't contain key (so can't be alive)      
            this.mDrawRegionHThreads.put(DrawRegionNames.RADAR, mExecutor.execute(null, "radarHandler")); 
        }
        
      //If the graph drawing thread doesn't exist create it
        if(this.mDrawRegionHThreads.containsKey(DrawRegionNames.GRAPH)){ //contains key
            if(!this.mDrawRegionHThreads.get(DrawRegionNames.GRAPH).isAlive()){ //but not alive
                this.mDrawRegionHThreads.put(DrawRegionNames.GRAPH, mExecutor.execute(null, "graphHandler-reborn"));  //create thread  
            } //if it's alive then we're ok
        } else { //doesn't contain key (so can't be alive)      
            this.mDrawRegionHThreads.put(DrawRegionNames.GRAPH, mExecutor.execute(null, "graphHandler")); 
        }
        

        /*
        //If thread exists but is not running  
        //HThread t1 = mExecutor.execute(null);
        //HThread t2 = mExecutor.execute(null);
        if(!this.mDrawRegionHandlers.containsKey(DrawRegionNames.RADAR) ||
                this.mDrawRegionHandlers.get(mDrawRegionNames.RADAR).isTerminated){
            this.mDrawRegionHandlers.put(DrawRegionNames.RADAR, mExecutor.execute(null));
        } else {
            String err_msg = "Unable to create radar thread handler";
            Log.e(TAG, err_msg);
            throw new Error(TAG + " - " + err_msg);
        }
        if(t2.handler!=null){
            this.mDrawRegionHandlers.put(DrawRegionNames.GRAPH, t2.handler);
        } else {
            Log.e(TAG, "Unable to create graph handler");
            throw new Error(TAG + "Unable to create graph handler");
        }
        */

        
        mDrawRegionHThreads.get(DrawRegionNames.RADAR).handler.post(
                new Runnable(){
                    @Override
                    public void run() {
                        Log.d(TAG, "Running radar draw in looping thread");
                        
                        DrawRegionRadar radarData = (DrawRegionRadar) mDrawRegionAreas.get(DrawRegionNames.RADAR);
                        //int location_iter = 0;
                        //int steps = 2;
                        //boolean go_right = true;
                        DrawRegionRadar.LocalData default_iter_data = radarData.new LocalData();
                        
                        //Put the data into a threadlocal
                        radarData.local_iter_data.set(default_iter_data);
                        
                        Rect dirty_rect = radarData.rect;
                        float r = radarData.blip_radius;
                        Paint paint = new Paint();
                        HThread thisThread = (HThread) Thread.currentThread();
                        while(thisThread.running && !thisThread.isInterrupted()){

                            SurfaceHolder holder = mSurfaceHolder;

                            //Rect dirty_rect = new Rect(0,top_pad, track_width ,top_pad + track_height);
                            Canvas c = holder.lockCanvas(dirty_rect);
                            //TODO: See if there's a way to factor this out from the thread
                            try {
                                if (c!=null) {
                                    synchronized (holder) {
                                        radarData.drawOnSurface(c);
                                        /*
                                        //threadlocal data accessible with thread
                                        DrawRegionRadar.LocalData local_data = radarData.local_iter_data.get();
                                        
                                        paint.setColor(Color.GRAY);
                                        c.drawRect(dirty_rect, paint);

                                        paint.setColor(Color.RED);
                                        c.drawCircle(local_data.location_iter, dirty_rect.top + r ,  r, paint);
                                        if(local_data.go_right){
                                            local_data.location_iter+=local_data.steps;
                                        } else {
                                            local_data.location_iter-=local_data.steps;
                                        }
                                        if (local_data.location_iter>dirty_rect.width()){
                                            local_data.go_right = false;
                                        }
                                        if (local_data.location_iter<0){
                                            local_data.go_right = true;
                                        }
                                        */
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
                            try {
                                Thread.sleep(40);
                            } catch (InterruptedException e) {
                                Log.d(TAG, "Thread was interupted, it should be caught on the next tick");
                                if (!thisThread.isInterrupted()) {
                                    thisThread.running = false;
                                    thisThread.interrupt();
                                }
                            }
                        }

                    };
                });

        
    }
    
    public void onSurfaceDestroyed() {
        Log.d(TAG, "Notified Surface Destroyed - shutdown handlers and clear data");
        mExecutor.stopThreads();
        //for (DrawRegionNames region : DrawRegionNames.values()){
        //    HThread thr = mDrawRegionHThreads.get(region);
        //    thr.handler.getLooper().quit();
        //    if(!thr.isInterrupted()){
        //        thr.interrupt();
        //    }
        //    Log.d(TAG, "Threads should quit");
        //    
        //}
        
        
        mDrawRegionHThreads.clear();
        mDrawRegionAreas.clear();
        
        mSurfaceHolder = null;
        mSurfaceRect = null;
        instance = null;
    }
   
    
    public void testTestBitmapDraw(){
        mExecutor.execute(new Runnable(){
            int[] myColors = new int[]{
                    Color.BLUE,
                    Color.CYAN,
                    Color.GREEN,
                    Color.YELLOW,
                    Color.RED,
                    Color.LTGRAY
            };
            Bitmap bitmap = Bitmap.createBitmap(100,100, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bitmap);
            @Override
            public void run(){
                HThread thisThread = (HThread) Thread.currentThread();
                for(int i=0;i<1000;i++){
                    if(!thisThread.running || thisThread.isInterrupted()){
                        break;
                    }
                    for(int color: myColors){
                        c.drawColor(color);
                        onBitmapUpdate(bitmap);
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            Log.e(TAG, "Test thread interupted: " + e);
                            if(!thisThread.isInterrupted()){
                                thisThread.interrupt();
                            }
                        }
                    }
                }
            }
        },"bitmap-tester");
        
        
    }

    private Bitmap scaleBitmap(Bitmap orig_bitmap, Rect scale_rect){
        Bitmap scaled_bitmap = orig_bitmap;
        if (scaled_bitmap.getWidth()!=scale_rect.width() && scaled_bitmap.getHeight()!= scale_rect.height()){
            scaled_bitmap = Bitmap.createScaledBitmap(orig_bitmap, scale_rect.width(), scale_rect.height(), false);
        }
        return scaled_bitmap;
    }
    
    public void onBitmapUpdate(Bitmap bitmap) {
        DrawRegionGraph graphData = (DrawRegionGraph) mDrawRegionAreas.get(DrawRegionNames.GRAPH);
        if(graphData!=null){
            Rect dirty_rect = graphData.rect;
            mOrigBitmap = bitmap;
            mScaledBitmap = scaleBitmap(bitmap, dirty_rect);
            drawScaledBitmap();
        } else {
            Log.e(TAG, "Graph Data seems to be null: " + graphData);
        }
    }
    
    private synchronized void drawScaledBitmap(){
        Log.d(TAG, "Attempting to send runner to graphThread to scale bitmap");
        HThread graphThread = mDrawRegionHThreads.get(DrawRegionNames.GRAPH);
        if (graphThread.isAlive() && graphThread.handler!=null){
            Log.d(TAG, "Attempting to draw scaled bitmap");
            //mDrawRegionHThreads.get(DrawRegionNames.GRAPH).handler.post( new Runnable(){
            graphThread.handler.post( new Runnable(){
                @Override
                public void run() {
                    SurfaceHolder holder = mSurfaceHolder;
                    DrawRegionGraph graphData = (DrawRegionGraph) mDrawRegionAreas.get(DrawRegionNames.GRAPH);
                    Rect dirty_rect = graphData.rect;
                    Log.d(TAG, "Drawing Current Audio Region");

                    Canvas c = holder.lockCanvas(dirty_rect);
                    //TODO: See if there's a way to factor this out from the thread
                    try {
                        if (c!=null) {
                            synchronized (holder) {
                                //paint.setColor(Color.GRAY);
                                //c.drawRect(dirty_rect, paint);
                                c.drawBitmap(mScaledBitmap, dirty_rect.left, dirty_rect.top, null);
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
                };
            });
        }
    }

    private void defaultBitmap(){
        //BitmapConfig conf = Bitmap.Config.ARGB_8888; // see other conf types
        //Bitmap bmp = Bitmap.createBitmap(w, h, conf); // this creates a MUTABLE bitmap
        //Canvas temp = new Canvas(bmp);
    }
    
}
