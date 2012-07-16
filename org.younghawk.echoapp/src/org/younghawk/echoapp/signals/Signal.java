package org.younghawk.echoapp.signals;

import android.util.Log;
/**
 * Concrete Factory that selects the appropriate class based
 * on the waveform name
 */
public class Signal {
	private static final String IMPULSE="impulse";
	private static final String NULLSIGNAL="null";
	
	/**
	 * Factory to create the appropriate wave form
	 * @param waveform_name
	 * @param num_of_samples
	 * @return
	 */
	public static SignalType create(String waveform_name, int num_of_samples) {
		
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
			sig_type = signalFactory.createSignal(num_of_samples);
		}
		
		//NOTE: This can conceivably return null, but only under abnormal circumstances
		//TODO: Handle a null return value prior to release
		return sig_type;
	}

}
