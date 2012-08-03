/*
 * Copyright(c) 2012 David Martin
 * 
 * License is TBD
 * 
 */

package org.younghawk.echoapp;

import java.util.Arrays;

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
	public Panel mPanel;  //change to private, since we should go through PanelManager
	public PanelManager mPanelManager;
    
	//Waveform data
	private String mSignal_instructions;
	private Resources mRes;
	private int mWave_samples;

	private AudioSupervisor audioSupervisor;
	public PlotSupervisor plotSupervisor;
	
	//TODO: Think about creating audioSupervisor in onStart
	//If you do, see if that solved the thread duplication problem in and of itself.
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //Reference to the surface view
        mPanel = (Panel) findViewById(R.id.panel);
        PanelManager mPanelManager = PanelManager.create(mPanel);

        
        //
        
        //float[] testDebug = new float[]{ 3.0f, 2.4f, 1.0f, -3.3f, 0.4f };
        //short[] testDebug = new short[]{3,2,1,5,-5};
        //Log.d(TAG, Arrays.toString(testDebug));
        //CollectionGrapher debugArray = CollectionGrapher.create(0, 100, 300, 40, testDebug);
        //Log.d(TAG, Arrays.toString(debugArray.mCanvasPts));
        //mPanel.setDebugArray(debugArray);
        
        
        //TODO: Move to audio supervisor
        mSignal_instructions = getString(R.string.signal_instructions);
    	mRes = getResources();
    	mWave_samples = mRes.getInteger(R.integer.samples_per_wav);
    	
    	//Manages plotting
    	//TODO: Uncouple plotSuperviosr from audioSupervisor (maybe via Dispatcher?)
    	plotSupervisor = PlotSupervisor.create();
    	plotSupervisor.setPanel(mPanel);
    	//Create AudioSupervisor to initiate threads
    	audioSupervisor = AudioSupervisor.create(mSignal_instructions, mWave_samples, plotSupervisor, this);
    	
    	
    	
    	
    	
    }
    
    public void onPause() {
        if (audioSupervisor!=null){
            audioSupervisor.shutDown();
            audioSupervisor = null;
        }
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
    	//audioSupervisor.startRecording();
        //plotSupervisor.startQCheck();
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
        Log.d(TAG, "updateFilterData callback - does nothing");
        //if (mPanel != null) {
        //    mPanel.mRawGraphData = filter_data;
        //} else {
        //    Log.e("EchoApp", "Cannot send data to Panel, Panel doesn't exist");
        //}

    }
    
    //Testing
    private void TimerMethod()
    {
        //This method is called directly by the timer
        //and runs in the same thread as the timer.

        //We call the method that will work with the UI
        //through the runOnUiThread method.
        this.runOnUiThread(Timer_Tick);
    }


    private Runnable Timer_Tick = new Runnable() {
        public void run() {
            Log.d(TAG, "Q Size: " + plotSupervisor.mPlotter.mScaledSamples.size());
        //This method runs in the same thread as the UI.               
        
        //Do something to the UI thread here
    
        }
    };
}