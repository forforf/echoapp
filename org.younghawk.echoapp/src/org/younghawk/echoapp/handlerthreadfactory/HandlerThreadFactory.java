package org.younghawk.echoapp.handlerthreadfactory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class HandlerThreadFactory implements ThreadFactory {
    public static final String TAG = "Scratch HandlerThreadFactory";
    private static final AtomicInteger mPoolNum = new AtomicInteger(1);
    private static final AtomicInteger mThrNum = new AtomicInteger(1);
    private final String mNamePrefix;
    
    public HandlerThreadFactory(String pool_name) {
        mNamePrefix = pool_name+"-"+mPoolNum.getAndIncrement();
    }

    public HThread newThread(Runnable r){
        return newThread(r, "anon");
    }
    public HThread newThread(Runnable r, String thread_name) {
        return new HThread(r, mNamePrefix+"-"+thread_name+"-"+mThrNum.getAndIncrement());
    }
}
