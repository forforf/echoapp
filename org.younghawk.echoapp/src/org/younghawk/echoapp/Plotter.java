package org.younghawk.echoapp;

import java.util.ArrayDeque;
import java.util.Arrays;

import org.younghawk.echoapp.drawregion.DrawRegionGraph;
import org.younghawk.echoapp.drawregion.DrawRegionNames;
import org.younghawk.echoapp.drawregion.ScrollingBitmap;
import org.younghawk.echoapp.handlerthreadfactory.HThread;

import android.util.Log;

public class Plotter {
    private static final String TAG = "EchoApp Plotter";
    private static final String GRAB_THR_NAME = "GrabSamples";
    
    //This class should be a singleton
    private static Plotter instance = null;
    
    //Global State
    private GlobalState mGlobal;
    
    //Thread for grabbing samples to plot
    //private HThread mGrabThr;
    
    //TODO: Move Constants to parameters
    public static final float PLOT_DWELL_TIME = (float) 10.0; //seconds (duration a pt will be on plot)
    public static final int PLOT_WIDTH = 200; //px
    public static final int PLOT_HEIGHT = 300; //px
    public static final float PX_DWELL_TIME = PLOT_DWELL_TIME / (float) PLOT_WIDTH;
    public static final int MAX_VAL = 32768;
    private static final float HT_SCALE_FACTOR = PLOT_HEIGHT/ (float) MAX_VAL;
    
    //TODO: Should come from preferences
    public static final int RAW_INPUT_RATE = 44100; //samples per second
   
    //TODO: Calculate in create?
    public static final int PTS_PER_PX = Math.round(PX_DWELL_TIME * RAW_INPUT_RATE);
    
    //TODO: Figure out optimum instantiation size
    //TODO: Refactor so it isn't static?
    public static ArrayDeque<Float> mScaledSamples = new ArrayDeque<Float>();
    public static boolean plotReady = false;
    private static ScrollingBitmap scr_bmp;
    private float[] mSamplesToPlot;
 
    //Factory
    public static Plotter create() {
        if(instance!=null){
            return instance;
        } else {
            instance = new Plotter();
            return instance;
        }
 
    }
    
    //Constructor
    private Plotter() {
        //Use Global
        if(mGlobal==null){
            mGlobal = GlobalState.getGlobalInstance();
        }
        this.mSamplesToPlot = new float[PTS_PER_PX];
        //if(mGrabThr==null){
        //    mGrabThr = mGlobal.getHandlerThread(GRAB_THR_NAME);
        //}
    }
    
    //TODO: Implement logscale option
    
    //Refactor so this isn't static
    public synchronized void grabSamplesToPlot(){
        
        //TODO: Fix for when the size is less than PTS_PER_PX
        if (Plotter.mScaledSamples.size()>PTS_PER_PX) {
            //float[] test_vector_pts = new float[PTS_PER_PX];
            //IMPORTANT: The array must be completely overwritten since the previous values have not been cleared
            
            for(int i=0;i<PTS_PER_PX;i++){
                mSamplesToPlot[i] = Plotter.mScaledSamples.removeFirst();
            }
                       
            //Testing sending to drawer
            Log.d(TAG, "Trying to connect to drawer");
            if(scr_bmp==null){
                scr_bmp = ScrollingBitmap.create();
            }
            //Use Global
            DrawRegionGraph graphData = (DrawRegionGraph) mGlobal.getRegionArea(DrawRegionNames.GRAPH);
            //DrawRegionGraph graphData = (DrawRegionGraph) PanelDrawer.mDrawRegionAreas.get(DrawRegionNames.GRAPH);
            if(graphData!=null){    
                if(scr_bmp.mGraphDrawRegionCallback==null){
                    scr_bmp.setDrawRegion(graphData);    
                }
                scr_bmp.onVectorUpate(mSamplesToPlot, (float) 100, (float) -100);
            }
        }
        
                     
        
        //plotReady = true;
         
    }
    
   public synchronized void addToQ(int[] data) {
       int sample_size = data.length;
       for(int i=0;i<sample_size;i++) {
           mScaledSamples.add( data[i] * HT_SCALE_FACTOR );
        }

       Log.d(TAG, "Q Sample Size: " + mScaledSamples.size());
    }
}
