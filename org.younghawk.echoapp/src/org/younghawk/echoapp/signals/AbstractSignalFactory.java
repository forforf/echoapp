package org.younghawk.echoapp.signals;

/**
 * Copyright (c) 2012 David Martin
 * 
 * @author Dave2
 *
 */

/**
 * Abstract Class Factory Interface
 * Allows classes to be selected at runtime
 */
public interface AbstractSignalFactory {
	
    /**
     * Subclasses implement	to create the appropriate waveform
     * with the appropriate number of samples.
     * @param wave_samples
     * @return
     */
	public SignalType createSignal(int wave_samples);

}
