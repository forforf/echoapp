package org.younghawk.echoapp;

import android.util.Log;

public class ImpulseFactory implements AbstractSignalFactory {

	//Create PcmImpulse
	public SignalType createSignal(int wave_samples) {
		Log.v("EchoApp ImpulseFactory", "Creating impulse signal object");
		PcmImpulse signal = PcmImpulse.create(wave_samples);
		return signal;
	}
}
