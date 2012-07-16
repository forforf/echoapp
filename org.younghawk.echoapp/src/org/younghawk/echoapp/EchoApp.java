package org.younghawk.echoapp;

import java.util.Arrays;

import org.json.JSONException;
import org.younghawk.echoapp.signals.PingThread;
import org.younghawk.echoapp.signals.SignalGenerator;

import android.app.Activity;
import android.content.res.Resources;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class EchoApp extends Activity{
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
    	Thread pingThread = new Thread(PingThread.create(signal_instructions, wave_samples));
    	pingThread.start();
    	
    	//Thread pingThread = new Thread(this);
    	//pingThread.start();
    	
    }
    

}