package org.younghawk.echoapp;

import java.util.Arrays;

import org.json.JSONException;
import org.younghawk.echoapp.signals.SignalGenerator;

import android.app.Activity;
import android.content.res.Resources;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class EchoApp extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
    public void pingButton(View view) {
    	Log.v("pingButton", "Ping Button Pressed");
    	String signal_instructions = getString(R.string.signal_instructions);
    	Resources res = getResources();
    	int wave_samples = res.getInteger(R.integer.samples_per_wav);
    	try {
			SignalGenerator sig_gen = SignalGenerator.create(signal_instructions, wave_samples);
			
			short[] pcm_signal = sig_gen.getSignal();
			
			try { 
				AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, pcm_signal.length, AudioTrack.MODE_STATIC );
				int result = track.write(pcm_signal, 0, pcm_signal.length);
				if (result == AudioTrack.ERROR_INVALID_OPERATION  || result != pcm_signal.length/2)
					Log.e("EchoApp AudioTrack","track.write returned " + result + ". " + pcm_signal.length + " expected" );
		            //throw new Exception("track.write returned " + result + ". " + pcm_signal.length + " expected");
				track.play();
			} catch (IllegalArgumentException e) {
				Log.e("pingButton", "Unable to create audio track");
				e.printStackTrace();
			}

			
		} catch (JSONException e) {
			Log.e("pingButton", "Failed to create Signal Generator");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
   

}