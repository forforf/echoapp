package org.younghawk.echoapp.signals;

public interface AbstractSignalFactory {
	
    //Concrete factories must have a createSignal method	
	public SignalType createSignal(int wave_samples);

}
