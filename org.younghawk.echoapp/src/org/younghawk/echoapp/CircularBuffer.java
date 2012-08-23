package org.younghawk.echoapp;

import android.util.Log;

/**
 *  Bulk of the code is from http://www.cs.utsa.edu/~wagner/CS2213/queue/queue.html
 *  Copyright © 2011, Neal R. Wagner. Permission is granted to access, download, share, and distribute, as long as this notice remains
 */
public class CircularBuffer {
    private static final String TAG = "EchoApp CircularBuffer";

    private int qMaxSize;// max queue size
    private int fp = 0;  // front pointer
    private int rp = 0;  // rear pointer
    private int qs = 0;  // size of queue
    private int[] q;    // actual queue
    private int[] ary;   //an array matching queue positions


    public static CircularBuffer create(int size) {
        return new CircularBuffer(size);
    }

    private CircularBuffer(int size) {
        qMaxSize = size;
        fp = 0;
        rp = 0;
        qs = 0;
        q = new int[qMaxSize];
        ary = new int[qMaxSize];
    }

    public int delete() {
        if (!emptyq()) {
            qs--;
            fp = (fp + 1)%qMaxSize;
            return q[fp];
        }
        else {
            Log.w(TAG, "Underflow, returning default (0)");
            return 0;
        }
    }

    public void insert(int c) {
        if (fullq()) {
            delete();
        }
        if (!fullq()) {
            qs++;
            rp = (rp + 1)%qMaxSize;
            q[rp] = c;
        }
        else
            Log.w(TAG, "Overflow, last item in buffer dropped");
    }

    public boolean emptyq() {
        return qs == 0;
    }

    public boolean fullq() {
        return qs == qMaxSize;
    }

    public int[] toArray(){
        for (int i=0;i<qMaxSize;i++){
            ary[(fp+i+qMaxSize-1)%qMaxSize] = q[i];
        }
        return ary;
    }   

    public void logq() {
        int maxLogSize = 1000;
        if(maxLogSize > qMaxSize){
            maxLogSize = qMaxSize;
        }
        Log.d(TAG, "Size: " + qs + ", rp: " + rp + ", fp: " + fp + ", q: ");
        String cq_str = "";
        for (int i=0;i<maxLogSize;i++){
            cq_str = cq_str + q[i] + ",";
        }
        Log.d(TAG, "cq [" + cq_str + "]");
        
        String aq_str = "";
        
        for(int i=0;i<maxLogSize;i++){
            aq_str = aq_str + toArray()[i] + ",";
        }
        Log.d(TAG, "ar [" + aq_str + "]");
    }
}
