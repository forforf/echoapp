package org.younghawk.echoapp.signals;

import org.json.JSONException;
import org.younghawk.echoapp.R;

import android.content.res.Resources;
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
public class PingThread implements Runnable {
	private Thread mPingThread;
	private short[] mPcmSignal;
		
	/**
	 * Factory to initialize the waveform signal
	 * @param instructions
	 * @param num_of_samples
	 * @return
	 */
	public static PingThread create(String instructions, int num_of_samples) {
		SignalGenerator sig_gen = null;
		short[] pcm_signal = null;

		//TODO: Handle null
		sig_gen = SignalGenerator.create(instructions, num_of_samples);
		pcm_signal = sig_gen.getSignal();

		return new PingThread(pcm_signal);
	}
	
	/**
	 * Create the thread object with the waveform signal
	 * @param pcm_signal
	 */
	private PingThread(short[] pcm_signal) {
		this.mPcmSignal = pcm_signal;
		this.mPingThread = new Thread(this);
	}

	/**
	 * Play the waveform in a thread
	 */
	@Override
	public void run() {

		try { 
			AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, 
												44100, 
												AudioFormat.CHANNEL_OUT_MONO, 
												AudioFormat.ENCODING_PCM_16BIT, 
												mPcmSignal.length, 
												AudioTrack.MODE_STATIC );
			
			int result = track.write(mPcmSignal, 0, mPcmSignal.length);
			if (result == AudioTrack.ERROR_INVALID_OPERATION  || result != mPcmSignal.length/2) {
				Log.e("EchoApp AudioTrack","track.write returned " + result + ". " + mPcmSignal.length + " expected" );
			} else {
				//OPTIONAL - Better is to capture initial signal at receiver
				//TODO capture transmit time 
				//capture nano time here
				track.play();
				//and capture nano time here, and send them to be averaged
				//to determine send time (or better yet, perform math using sample rate)
			}
			
			
		} catch (IllegalArgumentException e) {
			Log.e("pingButton", "Unable to create audio track (Illegal Arguments)");
			e.printStackTrace();
		}
	}
}
