package org.younghawk.echoapp;

public interface AbstractSignalFactory {
	
	public SignalType createSignal(int wave_samples);

}
