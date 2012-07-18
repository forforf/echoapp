/*
 * Copyright(c) 2012 David Martin
 * 
 * License is TBD
 * 
 */

package org.younghawk.echoapp;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.younghawk.echoapp.signals.PingThread;

/**
 * Main Activity for the Echo App
 */
public class EchoApp extends Activity{
	//Threads that may be active
	private Thread mPingThread = null;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
    /**
     * Handles pingButton presses (click handler defined in layout)
     *   Retrieves the information needed to build a signal from 
     *   resource files and kicks off a thread that generates the
     *   signal that will be the echo-location "ping".
     * @param view
     */
    public void pingButton(View view) {
    	Log.v("pingButton", "Ping Button Pressed");
    	String signal_instructions = getString(R.string.signal_instructions);
    	Resources res = getResources();
    	int wave_samples = res.getInteger(R.integer.samples_per_wav);
    	if (mPingThread!=null && mPingThread.isAlive() ) {
    		// let existing thread finish for now
    	} else {
    	    mPingThread = new Thread(PingThread.create(signal_instructions, wave_samples), "PingThread");
    	    mPingThread.start();
    	}
    }
}