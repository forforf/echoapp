package org.younghawk.echoapp;


import org.younghawk.echoapp.signals.SignalGenerator;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

/**
 * Create the signal generator with the instructions and sample size
 * Then run the thread that sends the signal to the speaker.
 * @author Dave2
 *
 */
public class PingRunner implements Runnable  {
	private static final String TAG = "EchoApp PingThread";
	
	private short[] mPcmSignal;
		
	/**
	 * Factory to initialize the waveform signal
	 * @param instructions
	 * @param num_of_samples
	 * @return
	 */
	public static PingRunner create() {
		
		SignalGenerator sig_gen;
		sig_gen = GlobalState.getGlobalInstance().getSigGen();
		
		return new PingRunner(sig_gen.mSignal);
	}
	
	/**
	 * Create the thread object with the waveform signal
	 * @param pcm_signal
	 */
	private PingRunner(short[] pcm_signal) {
		this.mPcmSignal = pcm_signal;
	}

	/**
	 * Play the waveform in a thread
	 * Implementation of Runnable
	 */
	@Override
	public void run() {
		Log.d(TAG, "Attempting ping on thread: " + Thread.currentThread().getName());
		try {
			Log.d(TAG, "Setting up Audio");
			AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, 
												44100, 
												AudioFormat.CHANNEL_OUT_MONO, 
												AudioFormat.ENCODING_PCM_16BIT, 
												mPcmSignal.length, 
												AudioTrack.MODE_STATIC );
			
			int result = track.write(mPcmSignal, 0, mPcmSignal.length);
			
			Log.d(TAG, "Audio Track result: " + result);
			if (result == AudioTrack.ERROR_INVALID_OPERATION  || 
					result == AudioTrack.ERROR_BAD_VALUE ||
					result != mPcmSignal.length/2) {
				
				Log.e(TAG,"track.write returned " + result + ". " + mPcmSignal.length + " expected" );
			} else {
				//Note: We detect the signal at the receiver as opposed
			    //to specifying it here (for now)
				track.play();
				Log.d(TAG, "Track should have played");
			}
			
			
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "Unable to create audio track (Illegal Arguments)");
			e.printStackTrace();
		} 
	}
}
