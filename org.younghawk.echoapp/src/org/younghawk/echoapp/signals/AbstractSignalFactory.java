package org.younghawk.echoapp.signals;

public interface AbstractSignalFactory {
	
	public SignalType createSignal(int wave_samples);

}
