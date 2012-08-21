package org.younghawk.echoapp;

import java.util.Timer;
import org.younghawk.echoapp.handlerthreadfactory.HThread;
import android.util.Log;


public class PlotSupervisor{ 
    
    private static final String TAG = "EchoApp PlotSupervisor";
    
    //This class should be a singleton
    private static PlotSupervisor instance = null;
    
    private GlobalState gGlobal;
    
    //TODO: Migrate to executor and thread factory.
    public final HThread mPlotterThr;

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
            return new PlotSupervisor();
        }
    }

    //private PlotSupervisor(HandlerThread plotThr, Handler plotHandler) {
    private PlotSupervisor() {
        this.gGlobal = GlobalState.getGlobalInstance();
        this.mPlotterThr = gGlobal.getHandlerThread("Plotter");
        this.mPlotter = gGlobal.getPlotter();
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
    