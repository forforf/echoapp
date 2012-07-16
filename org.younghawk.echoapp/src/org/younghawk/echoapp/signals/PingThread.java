package org.younghawk.echoapp.signals;

import org.json.JSONException;
import org.younghawk.echoapp.R;

import android.content.res.Resources;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class PingThread implements Runnable {
	private Thread pThread;
	private short[] pcm_signal;
		
	//factory
	public static PingThread create(String instructions, int wave_samples) {
		SignalGenerator sig_gen = null;
		short[] pcm_signal = null;

		//TODO: Handle null
		sig_gen = SignalGenerator.create(instructions, wave_samples);
		pcm_signal = sig_gen.getSignal();
		  

		
		return new PingThread(pcm_signal);
	}

	private PingThread(short[] pcm_signal) {
		this.pcm_signal = pcm_signal;
		this.pThread = new Thread(this);
	}

	@Override
	public void run() {

		try { 
			AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, 
												44100, 
												AudioFormat.CHANNEL_OUT_MONO, 
												AudioFormat.ENCODING_PCM_16BIT, 
												pcm_signal.length, 
												AudioTrack.MODE_STATIC );
			
			int result = track.write(pcm_signal, 0, pcm_signal.length);
			if (result == AudioTrack.ERROR_INVALID_OPERATION  || result != pcm_signal.length/2) {
				Log.e("EchoApp AudioTrack","track.write returned " + result + ". " + pcm_signal.length + " expected" );
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
