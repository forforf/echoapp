package org.younghawk.echoapp.signals;

import android.util.Log;

public class Signal {
	private static final String IMPULSE="impulse";
	private static final String NULLSIGNAL="null";
	
	//Create the appropriate waveform
	public static SignalType create(String waveform_name, int wave_samples) {
		
		SignalType sig_type = null;
		AbstractSignalFactory signalFactory = null;
		
		if (Signal.IMPULSE.equals(waveform_name)) {
			ImpulseFactory impulseFactory = new ImpulseFactory();
			signalFactory = (AbstractSignalFactory) impulseFactory;
		
		} else if (Signal.NULLSIGNAL.equals(waveform_name)) {
			NullSignalFactory nullFactory = new NullSignalFactory();
			signalFactory = nullFactory;

		} else {
			String log_message = "Unknown signal type: " + waveform_name + "; using NullSignal instead";
			Log.w("SignalType", log_message);
			NullSignalFactory nullFactory = new NullSignalFactory();
			signalFactory = (AbstractSignalFactory) nullFactory;
		}
		
		if (signalFactory != null){
			sig_type = signalFactory.createSignal(wave_samples);
		}
		
		//NOTE: This can conceivably return null, but only under abnormal circumstances
		return sig_type;
	}

}
