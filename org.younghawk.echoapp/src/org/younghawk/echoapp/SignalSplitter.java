package org.younghawk.echoapp;

public class SignalSplitter {
    public int[] origSignal;
    public int[] baseSignal;
    public int[] primarySignal;
    public int[] echoSignal;
    
    
    public static SignalSplitter create(int[] orig_signal, int primary_signal_threshold) {
        int[] base_signal;
        int[] echo_signal;
        int[] primary_signal;
        int[] signal_idxs = new int[2];
        signal_idxs[0] = -1;
        signal_idxs[1] = -1;
        
        int orig_size = orig_signal.length;
        for (int i=0;i<orig_size;i++) {
            if (orig_signal[i]>=primary_signal_threshold){
                if(signal_idxs[0]<=0){
                    signal_idxs[0] = i;
                } else {
                    signal_idxs[1] = i;
                }
            }
        }
        
        int sigstart = signal_idxs[0];
        int sigend = signal_idxs[1];
        
        //check if there's detectable signal
        if (sigstart<1 || sigend<2) {
            base_signal = orig_signal;
            primary_signal = new int[0];
            echo_signal = orig_signal;
            
        } else {   // there's a detectable primary signal
            
            int base_signal_size = sigstart - 1;
            base_signal = new int[base_signal_size];
            System.arraycopy(orig_signal, 0, base_signal, 0, base_signal_size);
            
            int primary_signal_size = sigend - sigstart;
            primary_signal = new int[primary_signal_size];
            System.arraycopy(orig_signal, sigstart, primary_signal, 0, primary_signal_size);
            
            int echo_signal_size = orig_size - sigend - 1;
            echo_signal = new int[echo_signal_size];
            System.arraycopy(orig_signal, sigend + 1, echo_signal, 0, echo_signal_size);
        }
        
        return new SignalSplitter(orig_signal, base_signal, primary_signal, echo_signal);
    }
    
    private SignalSplitter(int[] orig_signal, int[] base_signal, int[] primary_signal, int[] echo_signal) {
        origSignal = orig_signal;
        baseSignal = base_signal;
        primarySignal = primary_signal;
        echoSignal = echo_signal;
    }

}
