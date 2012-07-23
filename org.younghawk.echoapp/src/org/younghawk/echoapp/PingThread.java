package org.younghawk.echoapp;

import java.util.Arrays;

import org.json.JSONException;
import org.younghawk.echoapp.R;
import org.younghawk.echoapp.signals.SignalGenerator;

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
public class PingThread implements Runnable  {
	//private Thread mPingThread; //used if we want/need to reference self as thread
	private short[] mPcmSignal;
	public short[] mPcmFilterMask;
		
	/**
	 * Factory to initialize the waveform signal
	 * @param instructions
	 * @param num_of_samples
	 * @return
	 */
	public static PingThread create(String instructions, int num_of_samples) {
		
		//TODO: Can SignalGenerator be moved out of the thread for better performance?
		SignalGenerator sig_gen = null;
		short[] pcm_signal = null;
		short[] filter_mask = null;

		
		sig_gen = SignalGenerator.create(instructions, num_of_samples);
		if (sig_gen!=null) {
			Log.i("EchoApp","Created Signal Generator");
		    pcm_signal = sig_gen.mSignal;
		    filter_mask = sig_gen.mFilterMask;
		    //Log.v("EchoApp", "FilterMask: " + Arrays.toString(filter_mask));
		} else {
			return null;
		}
		return new PingThread(pcm_signal, filter_mask);
	}
	
	/**
	 * Create the thread object with the waveform signal
	 * @param pcm_signal
	 */
	private PingThread(short[] pcm_signal, short[] filter_mask) {
		this.mPcmSignal = pcm_signal;
		this.mPcmFilterMask = filter_mask;
		//this.mPingThread = new Thread(this);  //this thread object
	}

	/**
	 * Play the waveform in a thread
	 * Implementation of Runnable
	 */
	@Override
	public void run() {
        Log.i("EchoApp", "Inside waveform thread - attempting to play audio");        
		try {
			Log.i("EchoApp", "Setting up Audio");
			AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, 
												44100, 
												AudioFormat.CHANNEL_OUT_MONO, 
												AudioFormat.ENCODING_PCM_16BIT, 
												mPcmSignal.length, 
												AudioTrack.MODE_STATIC );
			
			int result = track.write(mPcmSignal, 0, mPcmSignal.length);
			
			Log.v("EchoApp", "Audio Track result: " + result);
			if (result == AudioTrack.ERROR_INVALID_OPERATION  || 
					result == AudioTrack.ERROR_BAD_VALUE ||
					result != mPcmSignal.length/2) {
				
				Log.e("EchoApp AudioTrack","track.write returned " + result + ". " + mPcmSignal.length + " expected" );
			} else {
				//OPTIONAL - Better is to capture initial signal at receiver
				//TODO capture transmit time 
				//capture nano time here
				track.play();
				Log.v("EchoApp AudioTrack", "Track should have played");
				//and capture nano time here, and send them to be averaged
				//to determine send time (or better yet, perform math using sample rate)
			}
			
			
		} catch (IllegalArgumentException e) {
			Log.e("pingButton", "Unable to create audio track (Illegal Arguments)");
			e.printStackTrace();
		} 
	}
}
