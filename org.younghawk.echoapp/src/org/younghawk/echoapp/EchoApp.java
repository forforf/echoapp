/*
 * Copyright(c) 2012 David Martin
 * 
 * License is TBD
 * 
 */

package org.younghawk.echoapp;

import java.util.Arrays;

import org.younghawk.echoapp.listen.AudioEnergyFilter;
import org.younghawk.echoapp.listen.ListenThread;
import org.younghawk.echoapp.listen.RecordAudioEvents;
import org.younghawk.echoapp.signals.PingThread;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

//TODO: Convert from Callbacks (RecordAudioEvents) to Messages
/**
 * Main Activity for the Echo App
 */
public class EchoApp extends Activity implements RecordAudioEvents {
	//used to ensure only one audio resource thread is running at a time
	private Thread mPingThread = null;
	
	//Get a handle on the Panel View (not sure this is best approach)
	private Panel mPanel = null;
	
	//Waveform data
	private String mSignal_instructions;
	private Resources mRes;
	private int mWave_samples;
	
	//FilterMask for decoding echoes
	private short[] mFilterMask;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        //get a reference to the Panel view
        //
        //set variables with initial values

        mSignal_instructions = getString(R.string.signal_instructions);
    	mRes = getResources();
    	mWave_samples = mRes.getInteger(R.integer.samples_per_wav);
    }
    
    /**
     * Handles pingButton presses (click handler defined in layout)
     *   Retrieves the information needed to build a signal from 
     *   resource files and kicks off a thread that generates the
     *   signal that will be the echo-location "ping".
     * @param view
     */
    public void pingButton(View view) {
    	Log.v("EchoApp pingButton", "Ping Button Pressed");
    	Thread listenThread = new Thread(ListenThread.create(this), "ListenThread");
    	listenThread.start();
    }
    
    public void onRecordReady() {
    	Log.v("EchoApp", "Activity recevied onRecordReady callback");
    	
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
    	AudioEnergyFilter rxEnergyFilter = AudioEnergyFilter.create(buffer, mFilterMask);
    	int[] rx_energy = rxEnergyFilter.mAudioEnergy;
    	//Log.v("EchoApp", "Filtered Audio:\n" + Arrays.toString(rxEnergy));
    	
    	if (mPanel != null) {
             mPanel.mRawGraphData = rx_energy;
    	} else {
    		Log.e("EchoApp", "Cannot send data to Panel, Panel doesn't exist");
    	}
    	//TODO: Remove test code
    	int zero_count = 0;
    	int small_pos_count = 0;
    	int small_neg_count = 0;
    	int big_pos_count = 0;
    	int big_neg_count = 0;
    	for (int i=0;i<rx_energy.length;i++){
    		if (rx_energy[i]==0){
    			zero_count++;
    		} else if (rx_energy[i]<32767 && rx_energy[i]>0) {
    			small_pos_count++;
    		} else if (rx_energy[i]>-32767 && rx_energy[i]<0) {
    			small_neg_count++;
    		} else if (rx_energy[i]>=32767) {
    			big_pos_count++;
    		} else if (rx_energy[i]<=-32767) {
    			big_neg_count++;
    		}
    		//	tv.append(" " +buffer[i]);
    	}
    	Log.v("EchoApp", "Zeros: " + zero_count);
    	Log.v("EchoApp", "Small Pos: " + small_pos_count);
    	Log.v("EchoApp", "Small Neg: " + small_neg_count);
    	Log.v("EchoApp", "Large Pos: " + big_pos_count);
    	Log.v("EchoApp", "Large Neg: " + big_neg_count);
    }
    
    
    public void setPanel(Panel panel){
    	this.mPanel = panel;
    }
    
    //public void updatePanelWithRxData(int[] rx_energy){
    //	Context context = getActivityContext();
    //	_panel = (Panel) context;
    //}
}