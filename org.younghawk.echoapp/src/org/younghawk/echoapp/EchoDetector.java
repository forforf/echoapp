package org.younghawk.echoapp;

import java.util.ArrayList;

import android.util.Log;

public class EchoDetector {
    private static final String TAG = "EchoApp EchoDetector";
    private static final int SPEED_OF_SOUND = 350;//meters per second
    private static final int RESOLUTION = 20;//cm
    private static final int CM_TO_M = 100;
    //TODO: noise factor should be parameter not a constant
    //This factor is applied to average calculations to find significant spikes
    private static final double NOISE_FACTOR = 1.5;
    static final double TIME_WINDOW = (double) ((RESOLUTION/(float) CM_TO_M)/(float) SPEED_OF_SOUND);
    private int mSampleWindow;
    
    
    public static EchoDetector create(int samps_per_sec){
        Log.d(TAG, "Time window: " + TIME_WINDOW);
        float full_window = Math.round(samps_per_sec * TIME_WINDOW);
        Log.d(TAG, "Full window: " + full_window);
        int half_window = (int) Math.round(full_window / 2);
        Log.d(TAG, "Half window: " + half_window);
        return new EchoDetector(half_window);
    }
    
    private EchoDetector(int sample_window){
        mSampleWindow = sample_window;
    }
    
    public ArrayList<EchoPt> findEchos(int[] echo_sig){
 
        int size = echo_sig.length;
        
        Log.d(TAG, "Echo Signal size: " + size + "  sample window: " + mSampleWindow);
        
        int start_idx = mSampleWindow;
        int end_size = size - mSampleWindow;
        ArrayList<EchoPt> echos = new ArrayList<EchoPt>();
        //Algorithm compare sig[i] with average of previous sig[i-]s in sample window
        //to the averagoe of the following sig[i+]s in sample winddow
        for (int i=start_idx;i<end_size;i++) {

            int pre_start = i - mSampleWindow;
            int pre_sum = 0;
            for (int pre=pre_start;pre<i;pre++){
                pre_sum += echo_sig[pre];
            }
            double pre_avg = pre_sum / (double) mSampleWindow;
            
            int post_start = i+1;
            int post_end = post_start + mSampleWindow;
            int post_sum = 0;
            for (int post=post_start;post<post_end;post++) {
                post_sum += echo_sig[post];
            }
            double post_avg = post_sum/ (double) mSampleWindow;
            //Log.d(TAG, "pre:" + pre_avg + " post:" + post_avg + "val:" + echo_sig[i]);
            
            if (echo_sig[i]> pre_avg*NOISE_FACTOR && echo_sig[i] > post_avg*NOISE_FACTOR) {
                EchoPt echo_pt = new EchoPt(i, echo_sig[i]);
                echos.add(echo_pt);
            }
        }
     
        return echos;
    }

}
