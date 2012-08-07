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
    public static ConcurrentHashMap<DrawRegionNames, DrawRegionType> mDrawRegionAreas = new ConcurrentHashMap<DrawRegionNames, DrawRegionType>();
    
    //Each region gets its own dedicated handler for queuing draw requests
    public static ConcurrentHashMap<DrawRegionNames, HThread> mDrawRegionHThreads = new ConcurrentHashMap<DrawRegionNames, HThread>();
    
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
        this.mDrawRegionAreas.put(DrawRegionNames.RADAR, DrawRegionFactory.radarRegion(this));
        this.mDrawRegionAreas.put(DrawRegionNames.GRAPH, DrawRegionFactory.graphRegion(this));
        
        //TODO: See if you can fix this hack
        //We're setting PanelDrawer as the callback so the DrawRegionGraph can call
        //when it receives an update with the audio data
        DrawRegionGraph graphRegion = (DrawRegionGraph) this.mDrawRegionAreas.get(DrawRegionNames.GRAPH);
        //graphRegion.setCallback(this);
        
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
        
        
        mDrawRegionHThreads.get(DrawRegionNames.RADAR).handler.post(
                new Runnable(){
                    @Override
                    public void run() {
                        DrawRegionRadar radarData = (DrawRegionRadar) mDrawRegionAreas.get(DrawRegionNames.RADAR);
                        radarData.run(mSurfaceHolder);
                    };
                });

        
    }
    
    public void onSurfaceDestroyed() {
        Log.d(TAG, "Notified Surface Destroyed - shutdown handlers and clear data");
        mExecutor.stopThreads();    
        
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
                        //onBitmapUpdate(bitmap);
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

    
    /*
    public void onBitmapUpdate(Bitmap bitmap) {
        DrawRegionGraph graphData = (DrawRegionGraph) mDrawRegionAreas.get(DrawRegionNames.GRAPH);
        
        
        if(graphData!=null){
         
            graphData.onBitmapUpdate(bitmap);
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
            graphThread.handler.post( new Runnable(){
                @Override
                public void run() {
                    DrawRegionGraph graphData = (DrawRegionGraph) mDrawRegionAreas.get(DrawRegionNames.GRAPH);
                    graphData.run(mSurfaceHolder);

                };
            });
        }
    }
    */
}
