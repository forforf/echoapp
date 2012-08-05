package org.younghawk.echoapp.handlerthreadfactory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class HandlerThreadFactory implements ThreadFactory {
    public static final String TAG = "Scratch HandlerThreadFactory";
    private static final AtomicInteger mPoolNum = new AtomicInteger(1);
    private static final AtomicInteger mThrNum = new AtomicInteger(1);
    private final String mNamePrefix;
    
    public HandlerThreadFactory() {
        mNamePrefix = "hthread-pool-"+mPoolNum.getAndIncrement()+"-thread-";
    }

    public HThread newThread(Runnable r) {
        return new HThread(r, mNamePrefix+mThrNum.getAndIncrement());
    }
}
