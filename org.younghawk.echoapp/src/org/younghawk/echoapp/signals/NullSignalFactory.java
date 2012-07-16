package org.younghawk.echoapp.signals;

public class NullSignalFactory implements AbstractSignalFactory {
	//Create NullSignal
	public SignalType createSignal(int wave_samples) {
		NullSignal signal = NullSignal.create(wave_samples);
		return signal;
	}
}
