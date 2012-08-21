package org.younghawk.echoapp;

import java.util.Timer;

import org.younghawk.echoapp.handlerthreadfactory.HThread;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;


public class PlotSupervisor{ 
    
    private static final String TAG = "EchoApp PlotSupervisor";
    
    //This class should be a singleton
    private static PlotSupervisor instance = null;
    
    private GlobalState gGlobal;
    
    //TODO: Migrate to executor and thread factory.
    public final HThread mPlotterThr;
    //public final Handler mPlotterHandler; //Handler for Plotter thread
    
    //public static Plotter mPlotter = Plotter.create();
    private Plotter mPlotter;
    
    public static Timer dwellTimer = new Timer();
    public boolean pauseQCheck = true;

    private Runnable checkingQ = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "Plotter Q has " + mPlotter.mScaledSamples.size() + "elements now");
            mPlotter.grabSamplesToPlot();
            
            if (!pauseQCheck){
                mPlotterThr.handler.postDelayed(checkingQ, (long) (Plotter.PX_DWELL_TIME * 1000) );
            }
        }
    };
    
    public static PlotSupervisor create() {
        //singleton
        if(instance!=null){
            return instance;
        } else {
                   
            //HandlerThread plotThr = new HandlerThread("Plotter");
            //plotThr.start();
            
            //Looper plotLooper = plotThr.getLooper();
            //Handler plotHandler = null;
            //if (plotLooper!=null) {
            //    plotHandler = new Handler(plotLooper);

            //} else {
            //    Log.e(TAG, "Plot Looper was null, was thread started?");
            //}
            
            //HThread plotThr = gGlobal.getHandlerThread("Plotter");
            
            //instance = new PlotSupervisor(plotThr, plotHandler);
            instance = new PlotSupervisor();
            return instance;
        }
    }

    //private PlotSupervisor(HandlerThread plotThr, Handler plotHandler) {
    private PlotSupervisor() {
        this.gGlobal = GlobalState.getGlobalInstance();
        this.mPlotterThr = gGlobal.getHandlerThread("Plotter");
        //this.mPlotterHandler = mPlotterThr.handler;
        
        this.mPlotter = gGlobal.getPlotter();
    }
    
    

    //IMPORTANT: In the current implementation this is called only once
    //since the buffer size = audio data size. Changing to be more flexible
    //will require this method to execute via a thread handler post, and
    //flushing and stitching buffers together would need to be handled.
    public void onBufferData(Object objBuffer){
    }
    
    
    public void startQCheck() {
        Log.d(TAG, "Starting Q Check");
        pauseQCheck = false;
        mPlotterThr.handler.postDelayed(checkingQ, 1000);
    }
    
    public void stopQCheck() {
        pauseQCheck = true;
    }

    
    public void checkQ() {
        Log.d(TAG, "Plotter Q has " + mPlotter.mScaledSamples.size() + "elements now");
        
    }
}
    