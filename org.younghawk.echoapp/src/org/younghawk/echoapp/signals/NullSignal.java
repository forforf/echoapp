package org.younghawk.echoapp.signals;

import android.util.Log;

/**
 * Container for a blank signal
 */
public class NullSignal implements SignalType {
    //A Null signal has a value of 0 for its wave samples
    private static final int NULL_SIGNAL_VALUE = 0;

    //integer array of signal vallues
    private int[] mSignal;

    /**
     * Factory for pre-initializing waveform data
     * @param wave_samples
     * @return
     */
    public static NullSignal create(int wave_samples) {
    	
    	//the number of wave samples should be even
        if (wave_samples % 2 == 1) //i.e. wave samples are odd
        	Log.w("echoapp NullSignal", "wave samples was odd, adding additional sample to make it even, original samples: " + wave_samples);
            wave_samples += 1;

        //This is not the most efficient way, but it is
        //consistent with other signals.
        int data_start = -(wave_samples/2);
        int data_end = wave_samples/2;
        int current_data = data_start;
        int i = 0;
        int[] null_data = new int[wave_samples+1];
            
        //Assign signal values
        while (current_data <= data_end) {
           null_data[i] = NULL_SIGNAL_VALUE;
           i++;
           current_data++;
        } 
        
        return new NullSignal(null_data);
    }

    //object initialization
    private NullSignal(int[] nullData) {
        mSignal = nullData;
    }
    
    public int[] getSignal() {
    	return mSignal;
    }
}