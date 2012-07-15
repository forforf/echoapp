package org.younghawk.echoapp;

public class NullSignal implements SignalType {
    //All signal values
    public static final int NullVal = 0;

    //integer array of singal vallues
    public int[] signal;

    //factory
    public static NullSignal create(int duration) {
       //ToDo: Log a warning that the odd duration was incremented to be even
        if (duration % 2 == 1) //i.e. duration is odd
            duration += 1;

        //This is not the most efficient way, but it is
        //consistent with other signals.
        int dataStart = -(duration/2);
        int dataEnd = duration/2;
        int currentData = dataStart;
        int i = 0;
        int[] nullData = new int[duration+1];
            
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