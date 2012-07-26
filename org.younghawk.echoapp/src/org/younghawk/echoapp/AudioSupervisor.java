package org.younghawk.echoapp;

import android.media.AudioRecord;
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
	public HandlerThread mAudioRecordThr;
	public HandlerThread mAudioBufferThr;
	public HandlerThread mPingerThr;
	private Handler mAudioRecordHandler; //Handler for hw thread
	private Handler mAudioBufferHandler; //Handler for data thread
	private Handler mPingerHandler; //Handler for pinger thread
	private Handler mMainHandler; //Handler for this thread (main thread)
	//Audio Data
	private AudioRecordWrapper mAudioRecordWrapper;
	private static final int SAMPPERSEC = 44100; 
	private static final double MAX_SAMPLE_TIME = 1.0; //in seconds
	//public short[] mBuffer;  //TODO: Make sure this is uded correctly
	private AudioRecord mAudioRecord;
	private PingThread mPinger;
	private short[] mFilter;
	private AudioFilter mAudioFilter;
	
	
	//Message Definitions
	private static final int RECORD_READY = 0;
	private static final int BUFFER_DATA = 1;
	private static final int FILTER_DATA = 2;

	//TODO: Change to local variable naming convention instead of instance variable naming convention
	public static AudioSupervisor create(String instructions, int num_of_samples) {
		HandlerThread mAudioRecordThr = new HandlerThread("Audio Recorder");
		mAudioRecordThr.start();
        
        HandlerThread mAudioBufferThr = new HandlerThread("Audio Buffering");
        mAudioBufferThr.start();
        
        HandlerThread mPingerThr = new HandlerThread("Play Audio Ping");
        mPingerThr.start();
        
        AudioRecordWrapper audioRecordWrapper = AudioRecordWrapper.create(SAMPPERSEC, MAX_SAMPLE_TIME);
        //AudioRecord audioRecord = audioRecordWrapper.mAudioRecord;
        
        //TODO: This ins't a thread it's a runnable, rename
        //TODO: This should get spun up in its own handler thread like the others
        PingThread pinger = PingThread.create(instructions, num_of_samples);
        AudioFilter audioFilter = AudioFilter.create(pinger.mPcmFilterMask, audioRecordWrapper.mBufferSizeShorts);
        
        Looper arLooper = mAudioRecordThr.getLooper();
        Handler audioHandler = null;
        if (arLooper!=null) {
        	audioHandler = new Handler(arLooper); 
        } else {
        	Log.e(TAG, "Audio Looper was null, was thread started?");
        }
        
        Looper bufLooper = mAudioBufferThr.getLooper();
        Handler bufferHandler = new Handler(bufLooper);
        if (bufLooper!=null){
        	bufferHandler = new Handler(bufLooper);
        } else {
        	Log.e(TAG, "Buffer Looper was null, was thread started?");
        }
        
        Looper pingLooper = mPingerThr.getLooper();
        Handler pingerHandler = new Handler(pingLooper);
        if (pingLooper!=null){
        	pingerHandler = new Handler(pingLooper);
        } else {
        	Log.e(TAG, "Pinger Looper was null, was thread started?");
        }
        
		return new AudioSupervisor(
				mAudioRecordThr, 
				mAudioBufferThr,
				mPingerThr,
				audioHandler, 
				bufferHandler,
				pingerHandler,
				audioRecordWrapper,
				pinger,
				audioFilter);
	}
	
	private AudioSupervisor(HandlerThread audioRecThr, 
			HandlerThread audioBufThr,
			HandlerThread pingThr,
			Handler audioHandler, 
			Handler bufferHandler,
			Handler pingHandler,
			AudioRecordWrapper audioRecordWrapper,
			PingThread pinger,
			AudioFilter audioFilter) {
		
		this.mAudioRecordThr = audioRecThr;
		this.mAudioBufferThr = audioBufThr;
		this.mPingerThr = pingThr;
		this.mAudioRecordHandler = audioHandler;
		this.mAudioBufferHandler = bufferHandler;
		this.mPingerHandler = pingHandler;
		this.mMainHandler = new Handler(this);
		this.mAudioRecordWrapper = audioRecordWrapper;
		this.mAudioRecord = audioRecordWrapper.mAudioRecord;
		this.mPinger = pinger;
		this.mFilter = pinger.mPcmFilterMask;
		this.mAudioFilter = audioFilter;
	}
	
	//TODO: Pass in filter as parameter
	public void startRecording() {
		Log.d(TAG,"Audio Supervisor starting recording, posting to Handler");
		mAudioRecordHandler.post(new Runnable(){
			@Override
			public void run() {
				Log.d(TAG, "Trying to start AudioRecord: " + mAudioRecord + " on thread: " + Thread.currentThread().getName());
				mAudioRecord.startRecording();
				mMainHandler.sendEmptyMessage(RECORD_READY);
				int samplesRead = mAudioRecord.read(mAudioRecordWrapper.mBuffer,  0, mAudioRecordWrapper.mBufferSizeShorts);
				Log.d(TAG, "Audior recorder read " + samplesRead + " audio samples");
				Message bufferMsg = Message.obtain(mMainHandler, BUFFER_DATA, mAudioRecordWrapper.mBuffer);
				mMainHandler.sendMessage(bufferMsg);
				
				//Apply Filter
				//TODO: Requires refactoring, expensive operation here - perhaps its own thread?
				//TODO: Also class is inside deprecated package
				//AudioFilter rxEnergyFilter = AudioFilter.create(mAudioRecordWrapper.mBuffer, mFilter);
		    	int[] rx_energy = mAudioFilter.filter(mAudioRecordWrapper.mBuffer);
		    	Message filterMsg = Message.obtain(mMainHandler, FILTER_DATA, rx_energy);
		    	mMainHandler.sendMessage(filterMsg);
			}
		});
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case RECORD_READY:
			onRecordReady();
			break;
		case BUFFER_DATA:
			onBufferData(msg.obj);
			break;
		case FILTER_DATA:
			onFilterData(msg.obj);
			break;
		}
		
		return true;
	}
	
	public void onRecordReady(){
		Log.d(TAG,"Main thread notified that Audio Recorder is Ready");
		
		
		Log.d(TAG,"Audio Supervisor sending ping to Pinger Thread");
		mPingerHandler.post(mPinger);
		
		
		/*
		if (mPinger!=null && mPinger.isAlive() ) {
    		// let existing thread finish for now
    	} else {
    		//PingThread pThr = PingThread.create(mSignal_instructions, mWave_samples);
    		//mFilterMask = pThr.mPcmFilterMask;
    		//TODO: Spin up the thread first then send message to it to activate ping
    	    Thread pingThread = new Thread(mPinger, "PingThread");
    	    //Log.v("EchoApp", "Activity has filter mask: " + Arrays.toString(pThr.mPcmFilterMask));
    	    pingThread.start();
		*/
	}
	
	//IMPORTANT: In the current implementation this is called only once
	//since the buffer size = audio data size. Changing to be more flexible
	//will require this method to execute via a thread handler post, and
	//flushing and stitching buffers together would need to be handled.
	public void onBufferData(Object objBuffer){
		short[] buffer = (short[]) objBuffer;
		Log.d(TAG, "Main thread notified of buffer with " + buffer.length + " samples");
	}
	
	//IMPORTANT:In the current implementation this is called only once
	//since the buffer size = audio data size. Changing to be more flexible
	//will require this method to execute via a thread handler post, and
	//flushing and stitching buffers together would need to be handled.
	public void onFilterData(Object objFilterData) {
		int[] filter_data = (int[]) objFilterData;
		Log.d(TAG,"Main thread notified with filter data with " + filter_data.length + " elements (samples).");
		
    	//TODO: Remove test code
    	int zero_count = 0;
    	int small_pos_count = 0;
    	int small_neg_count = 0;
    	int big_pos_count = 0;
    	int big_neg_count = 0;
    	
    	for (int i=0;i<filter_data.length;i++){
    		if (filter_data[i]==0){
    			zero_count++;
    		} else if (filter_data[i]<32767 && filter_data[i]>0) {
    			small_pos_count++;
    		} else if (filter_data[i]>-32767 && filter_data[i]<0) {
    			small_neg_count++;
    		} else if (filter_data[i]>=32767) {
    			big_pos_count++;
    		} else if (filter_data[i]<=-32767) {
    			big_neg_count++;
    		}
    		//	tv.append(" " +buffer[i]);
    	}
    	
    	Log.d(TAG, "Zeros: " + zero_count);
    	Log.d(TAG, "Small Pos: " + small_pos_count);
    	Log.d(TAG, "Small Neg: " + small_neg_count);
    	Log.d(TAG, "Large Pos: " + big_pos_count);
    	Log.d(TAG, "Large Neg: " + big_neg_count);
	}
}


