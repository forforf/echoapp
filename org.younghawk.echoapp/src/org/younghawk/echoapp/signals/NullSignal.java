package org.younghawk.echoapp.signals;

import android.util.Log;

public class NullSignal implements SignalType {
    //A Null signal has a value of 0 for its wave samples
    private static final int NullVal = 0;

    //integer array of singal vallues
    private int[] signal;

    //factory
    public static NullSignal create(int wave_samples) {
        if (wave_samples % 2 == 1) //i.e. duration is odd
        	Log.w("echoapp NullSignal", "wave samples was odd, adding additional sample to make it even, original samples: " + wave_samples);
            wave_samples += 1;

        //This is not the most efficient way, but it is
        //consistent with other signals.
        int dataStart = -(wave_samples/2);
        int dataEnd = wave_samples/2;
        int currentData = dataStart;
        int i = 0;
        int[] nullData = new int[wave_samples+1];
            
        while (currentData <= dataEnd) {
           nullData[i] = NullVal;
           i++;
           currentData++;
        } 
        
        return new NullSignal(nullData);
    }

    //initialization
    private NullSignal(int[] nullData) {
        signal = nullData;
    }
    
    public int[] getSignal() {
    	return signal;
    }
}