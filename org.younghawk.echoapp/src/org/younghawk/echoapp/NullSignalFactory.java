package org.younghawk.echoapp;

import android.util.Log;

public class NullSignalFactory implements AbstractSignalFactory {
	//Create NullSignal
	public SignalType createSignal(int wave_samples) {
		Log.v("EchoApp NullFactory", "Creating null signal object");
		NullSignal signal = NullSignal.create(wave_samples);
		return signal;
	}
}
