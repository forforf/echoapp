package org.younghawk.echoapp;

import org.json.JSONException;

import android.app.Activity;
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
    	try {
			SignalGenerator sig_gen = SignalGenerator.create(signal_instructions);
		} catch (JSONException e) {
			Log.v("pingButton", "Failed to create Signal Generator");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
   

}