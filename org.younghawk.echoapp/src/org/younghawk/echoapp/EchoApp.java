/*
 * Copyright(c) 2012 David Martin
 * 
 * License is TBD
 * 
 */

package org.younghawk.echoapp;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

//TODO: Convert from Callbacks (RecordAudioEvents) to Messages
/**
 * Main Activity for the Echo App
 */
public class EchoApp extends Activity implements AudioUpdates {
	private static final String TAG = "EchoApp";
    
	//Get a handle on the Panel View (not sure this is best approach)
	// see setPanel();
	private Panel mPanel = null;
    
	//Waveform data
	private String mSignal_instructions;
	private Resources mRes;
	private int mWave_samples;

	private AudioSupervisor audioSupervisor;
	
	//TODO: Think about creating audioSupervisor in onStart
	//If you do, see if that solved the thread duplication problem in and of itself.
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
       
        //TODO: Move to audio supervisor
        mSignal_instructions = getString(R.string.signal_instructions);
    	mRes = getResources();
    	mWave_samples = mRes.getInteger(R.integer.samples_per_wav);
    	
    	//Create AudioSupervisor to initiate threads
    	audioSupervisor = AudioSupervisor.create(mSignal_instructions, mWave_samples, this);	
    }
    
    public void onPause() {
        audioSupervisor.shutDown();
        audioSupervisor = null;
        super.onPause();
    }
    
    /**
     * Handles pingButton presses (click handler defined in layout)
     *   Retrieves the information needed to build a signal from 
     *   resource files and kicks off a thread that generates the
     *   signal that will be the echo-location "ping".
     * @param view
     */
    public void pingButton(View view) {
    	Log.d(TAG, "Ping Button Pressed");
    	Log.d(TAG, "audioSupervisor: " + audioSupervisor);
    	audioSupervisor.startRecording();

    }
      
    public void setPanel(Panel panel){
    	this.mPanel = panel;
    }
	
	//TODO: I don't know why I have to do it like this :(
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ( keyCode == KeyEvent.KEYCODE_MENU ) {
	        Log.v(TAG, "MENU pressed");
	        startActivity(new Intent(this, EchoAppPreferences.class));
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Log.i(TAG, "Options Item Selected");
    	//startActivity(new Intent(this, EchoAppPreferences.class));
    	return false;
    }
   
    public void updateFilterData(int[] filter_data){
        Log.d(TAG, "updateFilterData callback");
        if (mPanel != null) {
            mPanel.mRawGraphData = filter_data;
        } else {
            Log.e("EchoApp", "Cannot send data to Panel, Panel doesn't exist");
        }

    }
}