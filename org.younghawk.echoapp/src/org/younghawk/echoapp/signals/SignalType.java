package org.younghawk.echoapp.signals;

/**
 * All waveforms should implement this interface
 */
//TODO: Add interface for getting filter mask
public interface SignalType {
    public int[] getSignal(); 
}
