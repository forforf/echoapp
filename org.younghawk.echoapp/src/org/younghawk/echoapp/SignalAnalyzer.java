package org.younghawk.echoapp;

import java.util.ArrayList;
import java.util.Arrays;

import android.util.Log;

public class SignalAnalyzer {
    private static final String TAG = "EchoApp SignalAnalyzer";
    private static final int MAX_ECHO_DELAY = 50; //milliseconds
    private static final double MS_TO_SEC = 0.001;
    //Threshold should be greater than any background noise or echo peaks
    private static final int PRIMARY_SIGNAL_THRESHOLD = 8000;  //swag 
    //public int[] mOrigSignal;
    ////Technically we want the power signal, but 
    ////a power signal results in the need for Longs rather than ints
    ////so we use the abs signal
    ////public int[] mAbsSignal;
    //private mMaxSampleSize;
    public int[]mEchoSignal;
    
    public static SignalAnalyzer create(int[] sig_data, int sample_rate) {
        int size = sig_data.length;
        //Helpful for analysis and debugging but expensive
        //TODO: Remove if/when not needed
        SignalStats orig_stat = SignalStats.create(sig_data);
        Log.d(TAG, "ORIG: " + orig_stat.toString());
        
        
        int[] abs_signal = new int[size];
        for(int i=0;i<size;i++) {
            abs_signal[i] = Math.abs(sig_data[i]);
        }
        SignalStats abs_stat = SignalStats.create(abs_signal);
        Log.d(TAG, "ABS: " + abs_stat.toString());
        Log.d(TAG, "Abs Size: " + abs_signal.length);

        SignalSplitter sigs = SignalSplitter.create(abs_signal,PRIMARY_SIGNAL_THRESHOLD);
        
        int[] raw_echo_signal = sigs.echoSignal;
        
        int echo_sample_size =(int) Math.round(sample_rate * MAX_ECHO_DELAY * MS_TO_SEC);
              
        int[] echo_signal = new int[echo_sample_size];
        Log.d(TAG, "Raw Echo Sig: " + raw_echo_signal.length);
        Log.d(TAG, "Echo Sig:" + echo_signal.length);
        Log.d(TAG, "Echo sample size: " + echo_sample_size);
        if (echo_sample_size <= raw_echo_signal.length) {
            System.arraycopy(raw_echo_signal, 0, echo_signal, 0, echo_sample_size);    
        } else {
            Log.w(TAG, "Echo data is less than desired sample size");
            //TODO: Use copyOf() here (requires min SDK of 11)
            for (int i=0;i<echo_sample_size;i++) {
                if (i<raw_echo_signal.length) {
                    echo_signal[i] = raw_echo_signal[i];
                } else {
                    echo_signal[i] = 0;
                }
            }
            
        }
        
        SignalStats base_stat = SignalStats.create(sigs.baseSignal);
        SignalStats sig_stat = SignalStats.create(sigs.primarySignal);
        SignalStats raw_echo_stat = SignalStats.create(raw_echo_signal);
        SignalStats echo_stat = SignalStats.create(echo_signal);
        Log.d(TAG, "BASE: " + base_stat.toString());
        Log.d(TAG, "Base Size: " + sigs.baseSignal.length);
        Log.d(TAG, "SIGNAL: " + sig_stat.toString());
        Log.d(TAG, "Signal Size: " + sigs.primarySignal.length);
        Log.d(TAG, "Raw ECHO: " + raw_echo_stat.toString());
        Log.d(TAG, "Raw Echo Size: " + raw_echo_signal.length);
        Log.d(TAG, "ECHO: " + echo_stat.toString());
        Log.d(TAG, "Echo Size: " + echo_sample_size);
        
        EchoDetector echo_detector = EchoDetector.create(sample_rate);
        ArrayList<EchoPt> echos = echo_detector.findEchos(echo_signal);
        Log.d(TAG, "Number of Echos: " + echos.size());
        for (EchoPt e: echos) {
            Log.d(TAG, e.toString());
        }
        
        return new SignalAnalyzer(echo_signal);
    }
    
    private SignalAnalyzer(int[] echo_signal){
        mEchoSignal = echo_signal;
    }

}
