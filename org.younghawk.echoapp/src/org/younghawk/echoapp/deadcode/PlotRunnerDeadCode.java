package org.younghawk.echoapp.deadcode;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class PlotRunnerDeadCode implements Runnable{
    public static final String TAG = "EchoApp PlotRunnerDeadCode";
    public Handler handler;
    
    public static PlotRunnerDeadCode create(Handler thisHandler){
        return new PlotRunnerDeadCode(thisHandler);
    }
    
    private PlotRunnerDeadCode(Handler thisHandler){
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
