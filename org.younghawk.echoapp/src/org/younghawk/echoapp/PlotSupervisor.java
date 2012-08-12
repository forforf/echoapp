package org.younghawk.echoapp;

import java.util.ArrayDeque;
import java.util.Timer;

import org.younghawk.echoapp.handlerthreadfactory.HandlerThreadExecutor;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;


public class PlotSupervisor implements Callback {
    
    private static final String TAG = "EchoApp PlotSupervisor";
    //This class should be a singleton
    private static PlotSupervisor instance = null;
    
    //TODO: Migrate to executor and thread factory.
    public HandlerThread mPlotterThr;
    public final Handler mPlotterHandler; //Handler for Plotter thread
    
    public static Plotter mPlotter = Plotter.create();
    public static Timer dwellTimer = new Timer();
    public boolean pauseQCheck = true;
    
    //This gets set to a reference to the SurfaceView
    
    //to allow a local variable to go into an anonymous class
    //grrrr java
    private short[] mBuffer;
    
    //TODO FIX THIS HACK
    private Panel mPanel;

    private Runnable checkingQ = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "Plotter Q has " + mPlotter.mScaledSamples.size() + "elements now");
            Plotter.fillPlotQ();
            ArrayDeque<Float> last_pts = Plotter.PlotQ[0];
            if(last_pts!=null) {
                Log.d(TAG, "Num of pts in last position of PlotQ: " + last_pts.size());    
            }
            
            if (!pauseQCheck){
                mPlotterHandler.postDelayed(checkingQ, (long) (Plotter.PX_DWELL_TIME * 1000) );
            }
        }
    };
    
    public static PlotSupervisor create() {
        if(instance!=null){
            return instance;
        } else {
            
            
            
            HandlerThread plotThr = new HandlerThread("Plotter");
            plotThr.start();
            
            Looper plotLooper = plotThr.getLooper();
            Handler plotHandler = null;
            if (plotLooper!=null) {
                plotHandler = new Handler(plotLooper);
               // plotHandler = new Handler(plotLooper){
               //     public void handleMessage(Message msg) {
               //         Log.d(TAG, "Finally received a msg: " + msg.what);
               //         int[] audio_buffer = (int[]) msg.obj;
               //         Log.d(TAG, "audio_buffer size: " + audio_buffer.length);
               //         mPlotter.addToQ(audio_buffer);
               //         //Log.d(TAG, "Q Size: " + mPlotter.mScaledSamples.size());
               //     }
               // };
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
    
    
    //TODO: THIS IS A HACK
    public void setPanel(Panel panel){
        this.mPanel = panel;
    }
    
    //IMPORTANT: In the current implementation this is called only once
    //since the buffer size = audio data size. Changing to be more flexible
    //will require this method to execute via a thread handler post, and
    //flushing and stitching buffers together would need to be handled.
    public void onBufferData(Object objBuffer){
        /*
        short[] buffer = (short[]) objBuffer;
        mBuffer = buffer;
        Log.d(TAG, "PlotSupervisor (main thread) notified of buffer with " + buffer.length + " samples");
        Log.d(TAG, "Accessing AudioDataRegion Bitmap");
        
        
        //Thread to Update Bitmaps with audio graphin region with audio buffer data.
        if (Panel.mAudioDataRegion!=null){
            //map to region

            mPlotterHandler.post(new Runnable(){
                @Override
                public void run() {
                    Log.d(TAG, "Get audio region bitmap");
                    Rect adr_rect = Panel.mAudioDataRegion.getDirtyRect();
                    CollectionGrapher audioPlot = CollectionGrapher.create(adr_rect.left,adr_rect.top,adr_rect.width(),adr_rect.height(), mBuffer);
                    Paint paint = new Paint();
                    paint.setColor(Color.CYAN);
                    //Here we get the bitmap, update it and commit it.
                    synchronized(Panel.mAudioDataRegion) {
                        Canvas c = new Canvas(Panel.mAudioDataRegion.getUnsafeBitmap());
                        c.drawPoints(audioPlot.mCanvasPts, paint);
                        Panel.mAudioDataRegion.updateDrawRegion();
                    }
                } //end run()
            }); // end post
        }
                
        //Thread loop to graph any updated regions - Keep together with executor
        //TODO: Figure out if this belongs here or not.
        Runnable checkAudioUpdates = new Runnable(){
            @Override
            public void run() {
                boolean runThread = (!Thread.currentThread().isInterrupted() && !Panel.mStopRunningThreads);
                while(runThread){
                    if(!Panel.mAudioDataRegion.isConsumed){
                        SurfaceHolder holder = mPanel.getHolder();
                        Rect dirty_rect = Panel.mAudioDataRegion.getDirtyRect();
                        Canvas c = holder.lockCanvas(dirty_rect);
                        try{
                            //This is the point in the thread that we call the onDraw method in our Panel class
                            synchronized (holder) {
                                //draw here
                                c.drawBitmap(Panel.mAudioDataRegion.consumeBitmap(), dirty_rect.left, dirty_rect.top, null);
                            }
                        } finally {
                            // do this in a finally so that if an exception is thrown
                            // during the above, we don't leave the Surface in an
                            // inconsistent state
                            if (c != null) {
                                holder.unlockCanvasAndPost(c);
                            }
                        }    
                    }
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        if(Thread.currentThread().isInterrupted()){
                            Thread.currentThread().interrupt();
                        }
                    }
                }  
            }
            
        };
        
        //TODO: Temporary workaround while refactoring
        HandlerThreadExecutor temp = new HandlerThreadExecutor();
        temp.execute(checkAudioUpdates);
        //Panel.mSvHandler.post(checkAudioUpdates);
        //Panel.mExecutor.execute(checkAudioUpdates);
        //Keep the above with the runnable
        
        //int[] int_buffer = new int[buffer.length];
        //for(int i=0;i<buffer.length;i++){
        //    int_buffer[i] = (int) buffer[i];
        //}
        //Log.d(TAG, "PlotSupervisor (main thread) notified of buffer with " + buffer.length + " samples");
        //mPlotter.pushAudioData(int_buffer);
        //Log.d(TAG, "Attempting to send message to PlotRunnerDeadCode");
        //PlotRunnerDeadCode plotRunner = PlotRunnerDeadCode.create(mPlotterHandler);
        //mPlotterHandler.post(plotRunner);
        //Message bufferMsg = Message.obtain(mPlotterHandler, MsgIds.BUFFER_DATA, int_buffer);
        //Log.d(TAG, "Sending Message");
        //mPlotterHandler.dispatchMessage(bufferMsg);
        //plotRunner.handler.sendEmptyMessage(99);
        //Log.d(TAG, "Message sent");
       */ 
    }
    
    //public float[] getPlotLineData(){
    //    return mPlotter.getPlotData();
    //}
    
    public void startQCheck() {
        Log.d(TAG, "Starting Q Check");
        pauseQCheck = false;
        mPlotterHandler.postDelayed(checkingQ, 1000);

    }
    
    public void stopQCheck() {
        pauseQCheck = true;
    }

    
    public void checkQ() {
        Log.d(TAG, "Plotter Q has " + mPlotter.mScaledSamples.size() + "elements now");
        
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
    