package org.younghawk.echoapp;

import java.util.ArrayDeque;

import org.younghawk.echoapp.drawregion.DrawRegionGraph;
import org.younghawk.echoapp.drawregion.DrawRegionNames;
import org.younghawk.echoapp.drawregion.ScrollingBitmap;

import android.util.Log;

public class Plotter {
    private static final String TAG = "EchoApp Plotter";
    
    //This class should be a singleton
    private static Plotter instance = null;
    
    //TODO: Move Constants to parameters
    public static final float PLOT_DWELL_TIME = (float) 10.0; //seconds (duration a pt will be on plot)
    public static final int PLOT_WIDTH = 200; //px
    public static final int PLOT_HEIGHT = 300; //px
    public static final float PX_DWELL_TIME = PLOT_DWELL_TIME / (float) PLOT_WIDTH;
    public static final int MAX_VAL = 32768;
    
    //TODO: Should come from preferences
    public static final int RAW_INPUT_RATE = 44100; //samples per second
   
    //TODO: Calculate in create?
    public static final int PTS_PER_PX = Math.round(PX_DWELL_TIME * RAW_INPUT_RATE);
    
    //TODO: Figure out optimum instantiation size
    //TODO: Refactor so it isn't static?
    public static ArrayDeque<Float> mScaledSamples = new ArrayDeque<Float>();
    public static final ArrayDeque<Float>[] PlotQ = new ArrayDeque[PLOT_WIDTH];
    public static boolean plotReady = false;
    private static ScrollingBitmap scr_bmp;
    private static ArrayDeque<Float> mRecycledArrayDeque;
    
    //Recycle the ArrayDeque that falls off the queue to be used for
    //the next item added to the queue
    //Adds to the recycle
    //private static void setRecycleDeque(ArrayDeque<Float> fell_off){
    //    fell_off.clear();
    //    mRecycledArrayDeque = fell_off;
    //}
    //private static ArrayDeque<Float> getRecycleDeque(){
    //    if(mRecycledArrayDeque==null){
    //        Log.e(TAG, "No Array Deque was found to be recycled!!!");
    //        mRecycledArrayDeque = new ArrayDeque<Float>();
    //    }
    //    if(!mRecycledArrayDeque.isEmpty()){
    //        mRecycledArrayDeque.clear();
    //    }
    //    return mRecycledArrayDeque;
    //}
    
    //Refactor so this isn't static
    public static synchronized void fillPlotQ(){
        //TESTING WITHOUT PlotQ
        //shift PlotQ down 1
        //for(int i=1;i<PLOT_WIDTH;i++) {
            //ORIGINAL
            //PlotQ[i-1] = PlotQ[i];
            //TESTING
            //noop
        //}
    
        //Add new y values at end of PlotQ
        //PlotQ[PLOT_WIDTH-1] = getRecycleDeque();
        //if (PlotQ[PLOT_WIDTH-1]==null) {
        //    //Log.d(TAG, "Last element was null");
        //    PlotQ[PLOT_WIDTH-1] = new ArrayDeque();
        //} else {
        //    //Log.d(TAG, "Last element was not null");
        //    PlotQ[PLOT_WIDTH-1].clear();
        //}
             
        if (Plotter.mScaledSamples.size()>0) {

            float[] test_vector_pts = new float[PTS_PER_PX];
            for(int i=0;i<PTS_PER_PX;i++){
                //test_vector_pts[i] = Plotter.mScaledSamples.peekFirst();
                
                //PlotQ[PLOT_WIDTH-1].add( Plotter.mScaledSamples.removeFirst() ); 
                //PlotQ[PLOT_WIDTH-1].add( Plotter.mScaledSamples.peekFirst() );
                test_vector_pts[i] = Plotter.mScaledSamples.removeFirst();
            }
                       
            //Testing sending to drawer
            Log.d(TAG, "Trying to connect to drawer");
            if(scr_bmp==null){
                scr_bmp = ScrollingBitmap.create();
            }
            
            DrawRegionGraph graphData = (DrawRegionGraph) PanelDrawer.mDrawRegionAreas.get(DrawRegionNames.GRAPH);
            if(graphData!=null){    
                if(scr_bmp.mGraphDrawRegionCallback==null){
                    scr_bmp.setDrawRegion(graphData);    
                }
                scr_bmp.onVectorUpate(test_vector_pts, (float) 100, (float) -100);
            }
            //Log.d(TAG, "Next sample to grab: "+  Plotter.mScaledSamples.getFirst() );
        }
        plotReady = true;
         
    }
 
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
    }
    
    //TODO: Implement logscale option
    
   public synchronized void addToQ(int[] data) {
       for(int i=0;i<data.length;i++) {
           //scale data
           mScaledSamples.add( (data[i] * PLOT_HEIGHT) / (float) MAX_VAL );
        }
    }
}
