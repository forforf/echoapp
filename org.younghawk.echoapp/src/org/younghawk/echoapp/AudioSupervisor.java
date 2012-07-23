package org.younghawk.echoapp;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * Supervises and handles callbacks for 
 * audio recording, playing and filtering threads
 *
 */
public class AudioSupervisor {
	public static AudioSupervisor create() {
		//kicks of the threads that we will be supervising
		//and provides them to the constructor
		return new AudioSupervisor();
	}
	
	private AudioSupervisor() { 
		
	}
}


