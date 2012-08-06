package org.younghawk.echoapp.handlerthreadfactory;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * Acts as a normal thread if passed a runnable, otherwise
 * if runnable==null then create a thread with a handler.
 * Note that the handler isn't valid until after
 * the thread is ran (as should be expected).
 * 
 * Motivation: Launch threads with handlers via Executors
 */
public class HThread extends Thread {
    private static final String TAG = "Scratch HThread";
    //provides common flag
    public boolean running = true;
    public Handler handler;
    private Runnable runner;

    public HThread(Runnable r){
        super(r);
        if(r==null){
            this.setName(this.getName() + "-handler");
        } else {
            this.setName(this.getName() + "-normal");
        }
        this.runner = r;
    }
    
    public HThread(Runnable r, String name){
        super(r, name);
        if(r==null){
            this.setName(name + "-handler");
        } else {
            this.setName(name + "-normal");
        }
        this.runner = r;
    }

    @Override
    public void run(){
        if(runner==null){
            try {
                Looper.prepare();
                handler = new Handler();
                Looper.loop();
            } catch (Throwable throwable) {
                Log.e(TAG, "Thread error", throwable);
            }
        } else
            runner.run();
    }
}

