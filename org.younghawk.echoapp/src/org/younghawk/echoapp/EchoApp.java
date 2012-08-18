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
import android.view.Menu;
import android.view.View;


/**
 * Main Activity for the Echo App
 */
public class EchoApp extends Activity {
	private static final String TAG = "EchoApp";
    
	//App Controller
	private GlobalState gGlobal;
	//Used to notify panel when views are created
	private Panel mPanel;  
    
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
        
        
        //Log.d(TAG, "Views created, setup Panel");
        //mPanel.viewsReady();
        

        Log.d(TAG, "Views created, setup App");
        gGlobal = GlobalState.getGlobalInstance();
        gGlobal.onEchoAppReady(this);
        
        
        //Deprecated - DONT DELETE, SHOWS USAGE        
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
    	
    	//Deprecated
    	//Manages plotting
    	//TODO: Uncouple plotSuperviosr from audioSupervisor (maybe via Dispatcher?)
    	//plotSupervisor = PlotSupervisor.create();
    	//plotSupervisor.setPanel(mPanel);
    	//Create AudioSupervisor to initiate threads
    	audioSupervisor = AudioSupervisor.create(mSignal_instructions, mWave_samples, plotSupervisor); //, this);
    	    	
    	
    }
    
    public void onPause() {
        if (audioSupervisor!=null){
            audioSupervisor.shutDown();
            audioSupervisor = null;
        }
        super.onPause();
    }
    
    public void startButton(View view){
        Log.d(TAG, "Start Button Pressed");
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
    	if(audioSupervisor!=null){
    	    audioSupervisor.startRecording();
    	}
        //plotSupervisor.startQCheck();
    }
      
    public void setPanel(Panel panel){
    	this.mPanel = panel;
    }
	
	//TODO: I don't know why I have to do it like this :(
    /*
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ( keyCode == KeyEvent.KEYCODE_MENU ) {
	        Log.v(TAG, "MENU pressed");
	        startActivity(new Intent(this, EchoAppPreferences.class));
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.activity_echo_app_preferences, menu);
        startActivity(new Intent(this, EchoAppPreferences.class));
        return true;
    }
    
    //@Override
    //public boolean onOptionsItemSelected(MenuItem item) {
    //	Log.i(TAG, "Options Item Selected");
    //	//startActivity(new Intent(this, EchoAppPreferences.class));
    //	return false;
    //}
}