package org.younghawk.echoapp.deadcode;

import java.util.concurrent.BlockingQueue;

import org.younghawk.echoapp.PanelDrawer;
import org.younghawk.echoapp.handlerthreadfactory.HThread;
import org.younghawk.echoapp.handlerthreadfactory.HandlerThreadExecutor;

import android.graphics.Bitmap;

public class BitmapUpdateConsumerDeadCode implements Runnable{
    private static final HandlerThreadExecutor executor = new HandlerThreadExecutor("QConsumer");
    private final BlockingQueue<Bitmap> queue;
    private PanelDrawer drawer;
    //private final HThread consumerHandlerThread;
    
    
    public static BitmapUpdateConsumerDeadCode create(BlockingQueue<Bitmap> q, PanelDrawer d) {            
        return new BitmapUpdateConsumerDeadCode(q, d);
    }
    
    private BitmapUpdateConsumerDeadCode(BlockingQueue<Bitmap> q, PanelDrawer d){
        this.queue = q;
        this.drawer = d;
        executor.execute(this, "QChecker");
    }
    
    
    public void run() {
        HThread thisThread = (HThread) Thread.currentThread();
        while(!thisThread.isInterrupted() && thisThread.running){
            try {
                consume(queue.take());
            } catch (InterruptedException e) {
                executor.stopThreads();
            }
        }
    }
    public void consume(Bitmap bitmap){
        //drawer.onBitmapUpdate(bitmap);
    }

}
