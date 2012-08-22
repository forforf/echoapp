package org.younghawk.echoapp;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * Supervises and handles callbacks for 
 * audio recording, playing and filtering threads
 */
public class AudioSupervisor implements Callback {
	private static final String TAG = "EchoApp AudioSupervisor";
	//This class should be a singleton
	private static AudioSupervisor instance = null;
	
	private GlobalState gGlobal;
	
	//TODO: Migrate to executor and thread factory.
	public HandlerThread mAudioRecordThr;
	private final Handler mAudioRecordHandler; //Handler for hw thread
	

	private final Handler mMainHandler; //Handler for this thread (main thread)
	//Audio Data
	private AudioRecorder mAudioRecorder; //Facade for AudioRecord

	private AudioFilterProxy mAudioFilter;
	
	private Plotter mPlotter;

	//TODO: Change to local variable naming convention instead of instance variable naming convention
	public static AudioSupervisor create() {
	    if(instance!=null){
	        return instance;
	    } else {
	    
	        HandlerThread mAudioRecordThr = new HandlerThread("Audio Recorder");
	        mAudioRecordThr.start();

	        AudioRecorder audioRecorder = AudioRecorder.create(
	                GlobalState.Audio.SAMPPERSEC, 
	                GlobalState.Audio.MAX_SAMPLE_TIME);

	        
	        Looper arLooper = mAudioRecordThr.getLooper();
	        Handler audioHandler = null;
	        if (arLooper!=null) {
	            audioHandler = new Handler(arLooper); 
	        } else {
	            Log.e(TAG, "Audio Looper was null, was thread started?");
	        }
	        
	        instance =  new AudioSupervisor(
	                mAudioRecordThr, 
	                audioHandler, 
	                audioRecorder);
	        
	        return instance;
	    }
	}
	private AudioSupervisor(HandlerThread audioRecThr, 
			Handler audioHandler, 
			AudioRecorder audioRecorder) {
		
		this.mAudioRecordThr = audioRecThr;
		this.mAudioRecordHandler = audioHandler;
		this.mMainHandler = new Handler(this);
		this.mAudioRecorder = audioRecorder;
		
		this.gGlobal = GlobalState.getGlobalInstance();
		this.mAudioFilter = gGlobal.getFilterProxy();
	}
	
	//TODO: Pass in filter as parameter
	public void startRecording() {
		Log.d(TAG,"Audio Supervisor starting recording, posting to Handler");
		mAudioRecordHandler.post(new Runnable(){
			@Override
			public void run() {
				Log.d(TAG, "Trying to start AudioRecord: " + mAudioRecorder + " on thread: " + Thread.currentThread().getName());
				mAudioRecorder.startRecording();
				mMainHandler.sendEmptyMessage(MsgIds.RECORD_READY);
				
				int iter = 0;
				while(iter<2000){
				    int samplesRead = mAudioRecorder.read(mAudioRecorder.mBuffer,  0, mAudioRecorder.mBufferSizeShorts);
				    Log.d(TAG, "Audior recorder read " + samplesRead + " audio samples");


				    //Message bufferMsg = Message.obtain(mMainHandler, MsgIds.BUFFER_DATA, mAudioRecorder.mBuffer);
				    //mMainHandler.sendMessage(bufferMsg);
				    
				    //Data is filtered here!!!
				    int[] rx_energy = mAudioFilter.filter(mAudioRecorder.mBuffer);
				    
				    
				    //CollectionGrapher audioPlot = CollectionGrapher.create(50,100,350,400, rx_energy);
				    //DebugData.setDebugArray(audioPlot);
				    
				    Message filterMsg = Message.obtain(mMainHandler, MsgIds.FILTER_DATA, rx_energy);
				    
				    mMainHandler.sendMessage(filterMsg);
				    iter++;
				}
			}
		});
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case MsgIds.RECORD_READY:
			onRecordReady();
			break;
		case MsgIds.BUFFER_DATA:
		    //Buffer Data is deprecated, since it's identical
		    //to Filter data with a null filter.
		    //mPlotSupervisor.onBufferData(msg.obj);
			break;
		case MsgIds.FILTER_DATA:
			onFilterData(msg.obj);
			break;
		}
		
		return false;
	}
	
	public void shutDown() {
	    //There may be more to do, but this at least
	    //cleans up the running threads.
	    Log.d(TAG, "Shutting threads down");
	    if (mAudioRecordThr!=null){
	        mAudioRecordThr.quit();
	        mAudioRecordThr = null;
	    }

        Log.d(TAG, "Threads shut down");
	}
	
	public void onRecordReady(){
		Log.d(TAG,"Main thread notified that Audio Recorder is Ready");
	}

	//Deprecated?
    public void onBufferData(Object objBuffer){
        //short[] buffer = (short[]) objBuffer;
        //Log.d(TAG, "AudioSupervisor (main thread) notified of buffer with " + buffer.length + " samples");
        //Debug graph
        //DebugData.setDebugArray( CollectionGrapher.create(100,100,250,40, buffer) );
    }
	
	public void onFilterData(Object objFilterData) {
		int[] filter_data = (int[]) objFilterData;
		Log.d(TAG,"Main thread notified with filter data with " + filter_data.length + " elements (samples).");
		
		//Lazy Load Plotter
		if(mPlotter==null){
		    mPlotter = gGlobal.getPlotter();
		}
		mPlotter.addToQ(filter_data);
		

		//Checking if Paused (default when started)
		//If its paused we start it .. this is not robust, since we can't
		//actually ever stop it now
		//Ummmm ... this should only be done when?
		//Log.d(TAG, "Plot Supervisor: " + mPlotSupervisor);
		if(mPlotter.pauseQCheck){
		    mPlotter.startQCheck();
		}
	}
}


