package org.younghawk.echoapp.handlerthreadfactory;

import java.util.concurrent.CopyOnWriteArrayList;

import android.util.Log;

public class HandlerThreadExecutor {
    private static final String TAG = "EchoApp HandlerThreadExecutor";
    private final String mPoolName;
    private static final int SPIN_UP_DELAY = 5; //ms - we want to wait for the thread to set the handler
    private static final int RETRIES = 10; //number of times to check whether handler is set
    private CopyOnWriteArrayList<HThread> mHThreads;  //TODO: evaluate performance
    
    public HandlerThreadExecutor(){
        this.mPoolName = "pool";
        this.mHThreads = new CopyOnWriteArrayList<HThread>();
    }

    public HandlerThreadExecutor(String pool_name){
        this.mPoolName = pool_name;
        this.mHThreads = new CopyOnWriteArrayList<HThread>();
    }
    
    public synchronized HThread execute(Runnable r){
        return execute(r, "anon");
    }
    
    public synchronized HThread execute(Runnable r, String thread_name) {
        //Log.d(TAG, "Trying to execute runnable: " + r);
        HandlerThreadFactory f = new HandlerThreadFactory(mPoolName);
        HThread t = f.newThread(r, thread_name); //new HThread(r);

            t.start();
        
        int retries = 0;
        while(t.handler==null && retries<RETRIES){
            //
            retries++;
            try {
                Thread.sleep(SPIN_UP_DELAY);
            } catch (InterruptedException e) {
                Log.w(TAG, "Thread interupted: " + e);
            }
            
        }
        //Log.d(TAG, "retries to catch handler: " + retries);
        mHThreads.add(t);
        return t;
    }
    
    public CopyOnWriteArrayList<HThread> getThreads(){
        return mHThreads;
    }
    
    public void stopThreads(){
        for (HThread t : mHThreads){
            if(t.isAlive() && !t.isInterrupted()){
                if(t.handler!=null){
                    t.handler.getLooper().quit();
                }
                t.interrupt();
                t.running = false;
            }
        }
        for (HThread t : mHThreads){
            int retries = 0;
            while(t.isAlive()&& retries<RETRIES){
                Log.d(TAG, "Thread's not dying: " + t.getName());
                retries++;
                try {
                    Thread.sleep(SPIN_UP_DELAY);
                } catch (InterruptedException e) {
                    Log.w(TAG, "Thread interupted: " + e);
                }
            }
        }

    }
}
