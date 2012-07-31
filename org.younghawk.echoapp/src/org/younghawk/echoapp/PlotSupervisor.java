package org.younghawk.echoapp;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;


public class PlotSupervisor implements Callback {
    private static final String TAG = "EchoApp PlotSupervisor";
    //This class should be a singleton
    private static PlotSupervisor instance = null;
    
    //TODO: Migrate to executor and thread factory.
    public HandlerThread mPlotterThr;
    public final Handler mPlotterHandler; //Handler for Plotter thread
    
    public static Plotter mPlotter = Plotter.create();
    
    
    
    public static PlotSupervisor create() {
        if(instance!=null){
            return instance;
        } else {
            
            HandlerThread plotThr = new HandlerThread("Plotter");
            plotThr.start();
            
            Looper plotLooper = plotThr.getLooper();
            Handler plotHandler = null;
            if (plotLooper!=null) {
                plotHandler = new Handler(plotLooper){
                    public void handleMessage(Message msg) {
                        Log.d(TAG, "Finally received a msg: " + msg.what);
                        int[] audio_buffer = (int[]) msg.obj;
                        Log.d(TAG, "audio_buffer size: " + audio_buffer.length);
                        mPlotter.addToQ(audio_buffer);
                        Log.d(TAG, "Q Size: " + mPlotter.mScaledSamples.size());
                    }
                };
            } else {
                Log.e(TAG, "Plot Looper was null, was thread started?");
            }
            
            Plotter plotter = Plotter.create();
            
            instance = new PlotSupervisor(plotThr, plotHandler, plotter);
            return instance;
        }
    }

    private PlotSupervisor(HandlerThread plotThr, Handler plotHandler, Plotter plotter) {
        this.mPlotterThr = plotThr;
        this.mPlotterHandler = plotHandler;
        this.mPlotter = plotter;
    }
    

    
    //IMPORTANT: In the current implementation this is called only once
    //since the buffer size = audio data size. Changing to be more flexible
    //will require this method to execute via a thread handler post, and
    //flushing and stitching buffers together would need to be handled.
    public void onBufferData(Object objBuffer){
        short[] buffer = (short[]) objBuffer;
        int[] int_buffer = new int[buffer.length];
        for(int i=0;i<buffer.length;i++){
            int_buffer[i] = (int) buffer[i];
        }
        Log.d(TAG, "PlotSupervisor (main thread) notified of buffer with " + buffer.length + " samples");
        //mPlotter.pushAudioData(int_buffer);
        Log.d(TAG, "Attempting to send message to PlotRunner");
        PlotRunner plotRunner = PlotRunner.create(mPlotterHandler);
        mPlotterHandler.post(plotRunner);
        Message bufferMsg = Message.obtain(mPlotterHandler, MsgIds.BUFFER_DATA, int_buffer);
        Log.d(TAG, "Sending Message");
        mPlotterHandler.dispatchMessage(bufferMsg);
        //plotRunner.handler.sendEmptyMessage(99);
        Log.d(TAG, "Message sent");
        
    }
    
    public float[] getPlotLineData(){
        return mPlotter.getPlotData();
    }
    
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
        case MsgIds.BUFFER_DATA:
          onBufferData(msg.obj);
          break;
        //case MsgIds.FILTER_DATA:
        //    onFilterData(msg.obj);
        //    break;
        }
        return false;
    }
}
    