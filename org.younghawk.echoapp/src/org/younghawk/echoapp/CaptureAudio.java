package org.younghawk.echoapp;

import android.os.Handler;
import android.util.Log;

public class CaptureAudio {
    private static final String TAG = "EchoApp CaptureAudio";

    public static final double PING_CAPTURE_DURATION = 0.200; //seconds
    //TODO: These can be private I think
    public volatile boolean isOn;
    public volatile boolean isFinished;

    private CircularBuffer mCircularBuffer;
    private Handler mHandler;


    public static CaptureAudio create(Handler handler, double samp_per_sec) {
        int size = (int) Math.ceil(PING_CAPTURE_DURATION * samp_per_sec);
        CircularBuffer cir_buf = CircularBuffer.create(size);
        
        return new CaptureAudio(handler, cir_buf);
    }

    private CaptureAudio(Handler handler, CircularBuffer cir_buf) {
        mHandler = handler;
        mCircularBuffer = cir_buf;
    }

    public void input(int data){
        //if(isOn && !isFinished){
            mCircularBuffer.insert(data);
        //}
    }
    
    public void bulkInput(int[] data){
        //if(isOn && !isFinished){
            for(int i=0;i<data.length;i++){
                input( data[i] );
            }
        //}    
    }
    
    public void start(){
        isOn = true;
        Runnable runner = new Runnable(){

            @Override
            public void run() {
                isOn = false;
                isFinished = true;
            }
        };
        mHandler.postDelayed(runner, (long) PING_CAPTURE_DURATION*1000);
        runner = null;
    }
    
    public void reset(){
        isFinished = false;
    }
}
