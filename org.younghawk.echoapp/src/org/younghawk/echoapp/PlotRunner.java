package org.younghawk.echoapp;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class PlotRunner implements Runnable{
    public static final String TAG = "EchoApp PlotRunner";
    public Handler handler;
    
    public static PlotRunner create(Handler thisHandler){
        return new PlotRunner(thisHandler);
    }
    
    private PlotRunner(Handler thisHandler){
        this.handler = thisHandler;
        
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        Log.d(TAG, "Running Plot");
        


    }

    //@Override
    public boolean handleMessage(Message msg) {
        Log.d(TAG, "Received message: " + msg.what);
        return false;
    }

}
