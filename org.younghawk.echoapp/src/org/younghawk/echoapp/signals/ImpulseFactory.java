package org.younghawk.echoapp.signals;

public class ImpulseFactory implements AbstractSignalFactory {

	//Create PcmImpulse Object
	public SignalType createSignal(int wave_samples) {
		PcmImpulse signal = PcmImpulse.create(wave_samples);
		return signal;
	}
}
