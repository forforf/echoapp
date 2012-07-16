package org.younghawk.echoapp.signals;

/**
 * Copyright (c) 2012 David Martin
 */

/**
 * Concrete Factory for creating impulse waveforms
 */
public class NullSignalFactory implements AbstractSignalFactory {
	//Create NullSignal
	public SignalType createSignal(int wave_samples) {
		NullSignal signal = NullSignal.create(wave_samples);
		return signal;
	}
}
