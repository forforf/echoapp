package org.younghawk.echoapp.signals;

/**
 * Copyright (c) 2012 David Martin
 */

/**
 * Concrete Factory for creating impulse waveforms
 */
public class ImpulseFactory implements AbstractSignalFactory {

	/**
	 * Creates the impulse waveform object
	 */
	public SignalType createSignal(int wave_samples) {
		PcmImpulse signal = PcmImpulse.create(wave_samples);
		return signal;
	}
}
