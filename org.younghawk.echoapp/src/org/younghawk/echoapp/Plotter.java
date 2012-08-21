package org.younghawk.echoapp;

import java.util.ArrayDeque;
import java.util.Timer;

import org.younghawk.echoapp.drawregion.DrawRegionGraph;
import org.younghawk.echoapp.drawregion.DrawRegionNames;
import org.younghawk.echoapp.drawregion.ScrollingBitmap;
import org.younghawk.echoapp.handlerthreadfactory.HThread;

import android.util.Log;

public class Plotter {
    private static final String TAG = "EchoApp Plotter";
    
    //This class is a singleton
    private static Plotter instance = null;
    
    //See GlobalState for how its used
    private GlobalState mGlobal;
    
    //TODO: Move Constants to configuration preferences or paramaters
    public static final float PLOT_DWELL_TIME = (float) 10.0; //seconds (duration a pt will be on plot)
    public static final int PLOT_WIDTH = 200; //px
    public static final int PLOT_HEIGHT = 300; //px
    public static final float PX_DWELL_TIME = PLOT_DWELL_TIME / (float) PLOT_WIDTH;
    public static final int MAX_VAL = 32768;
    private static final float HT_SCALE_FACTOR = PLOT_HEIGHT/ (float) MAX_VAL;
    private static final float MAX_PLOT_HT = 100;
    private static final float MIN_PLOT_HT = -100;
    
    //TODO: Should come from preferences
    public static final int RAW_INPUT_RATE = 44100; //samples per second
   
    //TODO: Calculate in create?
    public static final int PTS_PER_PX = Math.round(PX_DWELL_TIME * RAW_INPUT_RATE);
    
    //Queue that holds samples to plot
    public ArrayDeque<Float> mScaledSamples = new ArrayDeque<Float>(2*PTS_PER_PX);
    
    private static ScrollingBitmap scr_bmp;
    
    //TODO: Make sure it works when there is no samples to plot for a give tick/pixel as well
    //In general there are multiple sample values to plot
    //per tick/pixel on the time axis. This array holds all samples for
    //that tick/pixel.
    private float[] mSamplesToPlot;
    private int mScaledSamplesSize;
    private DrawRegionGraph mGraphData;
    
    //From PlotSupervisor, refactoring into this class
    public final HThread mPlotterThr;
    public static Timer dwellTimer = new Timer();
    public boolean pauseQCheck = true;
    
    
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
        this.mPlotterThr = mGlobal.getHandlerThread("Plotter");
        this.mSamplesToPlot = new float[PTS_PER_PX];
        Log.d(TAG, "Constructed");
        
    }
    
    //TODO: Implement logscale option
    
    //DO NOT USE THESE POINTS FOR ANALYSIS
    //WE SKIP DATA TO KEEP QUEUES WELL BEHAVED
    public synchronized void grabSamplesToPlot(){
       
        //TODO: Fix for when the size is less than PTS_PER_PX
        mScaledSamplesSize = mScaledSamples.size();
        if (mScaledSamplesSize>PTS_PER_PX) {
            //IMPORTANT: The array must be completely overwritten since 
            //its been recycled and the previous values have not been cleared
            for(int i=0;i<PTS_PER_PX;i++){
                mSamplesToPlot[i] = mScaledSamples.removeFirst();
                
                //Crude mechanism for keeping the Q from always growing
                if(mScaledSamplesSize>2*PTS_PER_PX){
                    mScaledSamples.removeFirst();
                }
            }
            
            //Lazy Load some dependencies
            if(scr_bmp==null){
                scr_bmp = mGlobal.getScrollingBitmap();
            }
                        
            if(mGraphData==null){
                mGraphData = (DrawRegionGraph) mGlobal.getRegionArea(DrawRegionNames.GRAPH);
            }

            if(scr_bmp.mGraphDrawRegionCallback==null){
                scr_bmp.setDrawRegion(mGraphData);    
            }
            
            //update scrolling bitmap with samples to plot
            scr_bmp.onVectorUpate(mSamplesToPlot, (float) MAX_PLOT_HT, (float) MIN_PLOT_HT);

        }
    }
    
   public synchronized void addToQ(int[] data) {
       int sample_size = data.length;
       
       for(int i=0;i<sample_size;i++) {
           mScaledSamples.add( data[i] * HT_SCALE_FACTOR );
        }
    }
   
   
   private Runnable checkingQ(){
       return new Runnable() {
           @Override
           public void run() {
               Log.d(TAG, "Plotter Q has " + mScaledSamples.size() + "elements now");
               grabSamplesToPlot();
           
               if (!pauseQCheck){
                   mPlotterThr.handler.postDelayed(checkingQ(), (long) (Plotter.PX_DWELL_TIME * 1000) );
               }
           }
       };
   }
   
   public void startQCheck() {
       Log.d(TAG, "Starting Q Check");
       pauseQCheck = false;
       mPlotterThr.handler.postDelayed(checkingQ(), 1000);
   }
   
   public void stopQCheck() {
       pauseQCheck = true;
   }

   
   public void checkQ() {
       Log.d(TAG, "Plotter Q has " + mScaledSamples.size() + "elements now");
       
   }
}
