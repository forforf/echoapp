/*
 * Copyright(c) 2012 David Martin
 * 
 * License is TBD
 * 
 */

package org.younghawk.echoapp;

import org.younghawk.echoapp.listen.ListenThread;
import org.younghawk.echoapp.listen.RecordAudioEvents;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

//TODO: Convert from Callbacks (RecordAudioEvents) to Messages
/**
 * Main Activity for the Echo App
 */
public class EchoApp extends Activity implements RecordAudioEvents, SonarThreadListener {
	private static final String TAG = "EchoApp";
	
    //private SonarThread mSonarThread;
    //private Handler mMainHandler;
    
    
    //Prexisting Variables
    private Thread mPingThread = null;
    
	//Get a handle on the Panel View (not sure this is best approach)
	private Panel mPanel = null;
    
	//Waveform data
	private String mSignal_instructions;
	private Resources mRes;
	private int mWave_samples;

	//FilterMask for decoding echoes
	private short[] mFilterMask;
	
	private AudioSupervisor audioSupervisor;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Create and launch the sonar thread
        //mSonarThread = new SonarThread(this);
        //mSonarThread.start();

        //create the handler for the UI thread
        //mMainHandler = new Handler();
        

        //TODO: Move to audio supervisor
        mSignal_instructions = getString(R.string.signal_instructions);
    	mRes = getResources();
    	mWave_samples = mRes.getInteger(R.integer.samples_per_wav);
    	
    	
    	//Create AudioSupervisor to initiate threads
    	audioSupervisor = AudioSupervisor.create(mSignal_instructions, mWave_samples);
    	

    }
     
    /**
     * Handles pingButton presses (click handler defined in layout)
     *   Retrieves the information needed to build a signal from 
     *   resource files and kicks off a thread that generates the
     *   signal that will be the echo-location "ping".
     * @param view
     */
    public void pingButton(View view) {
    	Log.d("EchoApp pingButton", "Ping Button Pressed");
        //mSonarThread.ping();
    	Log.d(TAG, "audioSupervisor: " + audioSupervisor);
    	audioSupervisor.startRecording();
    	
    	
    	//Thread listenThread = new Thread(ListenThread.create(this), "ListenThread");
    	//listenThread.start();
    }
    
    public void onRecordReady() {
    	Log.d("EchoApp", "Activity recevied onRecordReady callback");
    	
    	if (mPingThread!=null && mPingThread.isAlive() ) {
    		// let existing thread finish for now
    	} else {
    		PingThread pThr = PingThread.create(mSignal_instructions, mWave_samples);
    		mFilterMask = pThr.mPcmFilterMask;
    	    mPingThread = new Thread(pThr, "PingThread");
    	    //Log.v("EchoApp", "Activity has filter mask: " + Arrays.toString(pThr.mPcmFilterMask));
    	    mPingThread.start();
    	}
    }
    
    public void onRecordDone(short[] buffer) {
    	Log.v("EchoApp", "Activity recevied onRecordDone callback");
    	//AudioFilter rxEnergyFilter = AudioFilter.create(buffer, mFilterMask);
    	//int[] rx_energy = rxEnergyFilter.mAudioEnergy;
    	//Log.v("EchoApp", "Filtered Audio:\n" + Arrays.toString(rxEnergy));
    	
    	if (mPanel != null) {
             //mPanel.mRawGraphData = rx_energy;
    	} else {
    		Log.e("EchoApp", "Cannot send data to Panel, Panel doesn't exist");
    	}

    }
    
    
    public void setPanel(Panel panel){
    	this.mPanel = panel;
    }

	@Override
	public void handleSonarUpdate() {
		Log.i(TAG,"Echo App handling sonar update");
	}
    
    //public void updatePanelWithRxData(int[] rx_energy){
    //	Context context = getActivityContext();
    //	_panel = (Panel) context;
    //}
	
	
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
        //switch (item.getItemId()) {
         //   case android.R.id.listPref:
    	//        Log.i(TAG, "Trying to start EchoAppPreference Activity");
         //       startActivity(new Intent(this, EchoAppPreferences.class));
          //      return false;
         //       break;
        //}
        //return super.onOptionsItemSelected(item);
    }
   
}