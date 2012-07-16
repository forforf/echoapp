package org.younghawk.echoapp.signals;

public interface AbstractSignalFactory {
	
    //Concrete factories will create a signal	
	public SignalType createSignal(int wave_samples);

}
