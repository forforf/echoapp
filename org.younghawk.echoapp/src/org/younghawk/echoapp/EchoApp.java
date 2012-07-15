package org.younghawk.echoapp;

import java.util.Arrays;

import org.json.JSONException;

import android.app.Activity;
import android.content.res.Resources;
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
			Log.v("pingButton", "Signal Generator created");
			Log.v("pingButton", "Signal: " + Arrays.toString( sig_gen.getSignal() ) );
			

			
		} catch (JSONException e) {
			Log.v("pingButton", "Failed to create Signal Generator");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
   

}