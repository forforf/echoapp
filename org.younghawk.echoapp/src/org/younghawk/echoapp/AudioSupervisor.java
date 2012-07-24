package org.younghawk.echoapp;


/**
 * Supervises and handles callbacks for 
 * audio recording, playing and filtering threads
 *
 */
public class AudioSupervisor {
	public static AudioSupervisor create() {
		AudioRecordRunnable audioRecordRunnable = AudioRecordRunnable.create();
		Thread audioRecordThread = new Thread(audioRecordRunnable);
		audioRecordThread.start();
		//kicks of the threads that we will be supervising
		//and provides them to the constructor
		return new AudioSupervisor();
	}
	
	private AudioSupervisor() { 
		
	}
}


