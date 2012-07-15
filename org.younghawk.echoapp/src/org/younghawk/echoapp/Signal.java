package org.younghawk.echoapp;

import android.util.Log;

public class Signal {
	
	
    //TODO: Move hardcoded strings of the signal class names out
	public static SignalType create(String waveform_name, int wave_samples) {
		Log.v("EchoApp SignalType", "Creating signal");
		
		SignalType sig_type;
		AbstractSignalFactory signalFactory = null;
		
		if ("impulse".equals(waveform_name)) {
			Log.v("EchoApp SignalType", "Creating impulse signal");
			ImpulseFactory impulseFactory = new ImpulseFactory();
			signalFactory = (AbstractSignalFactory) impulseFactory;
		} else if ("null".equals(waveform_name)) {
			Log.v("EchoApp SignalType", "Creating null signal");
			NullSignalFactory nullFactory = new NullSignalFactory();
			Log.v("EchoApp SignalType", "Created null signal factory");
			signalFactory = nullFactory;
			Log.v("EchoApp SignalType", "Assigned null signal factory to signal factory");
		} else {
			String log_message = "Unknown signal type: " + waveform_name + "; using NullSignal instead";
			Log.w("SignalType", log_message);
			NullSignalFactory nullFactory = new NullSignalFactory();
			signalFactory = (AbstractSignalFactory) nullFactory;
		}
		
		Log.v("EchoApp SignalType", "Now creating signal");
		sig_type = signalFactory.createSignal(wave_samples);
		
		return sig_type;
	}


}
