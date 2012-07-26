package org.younghawk.echoapp;

import java.util.ArrayList;
import java.util.Arrays;

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
	private AudioUpdates mCallback;

	
	
	//Message Definitions
	//private static final int RECORD_READY = 0;
	//private static final int BUFFER_DATA = 1;
	//private static final int FILTER_DATA = 2;

	//TODO: Change to local variable naming convention instead of instance variable naming convention
	public static AudioSupervisor create(String instructions, int num_of_samples, AudioUpdates callback) {
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
				audioFilter,
				callback);
	}
	
	private AudioSupervisor(HandlerThread audioRecThr, 
			HandlerThread audioBufThr,
			HandlerThread pingThr,
			Handler audioHandler, 
			Handler bufferHandler,
			Handler pingHandler,
			AudioRecordWrapper audioRecordWrapper,
			PingThread pinger,
			AudioFilter audioFilter,
			AudioUpdates callback) {
		
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
		this.mCallback = callback;
	}
	
	//TODO: Pass in filter as parameter
	public void startRecording() {
		Log.d(TAG,"Audio Supervisor starting recording, posting to Handler");
		mAudioRecordHandler.post(new Runnable(){
			@Override
			public void run() {
				Log.d(TAG, "Trying to start AudioRecord: " + mAudioRecord + " on thread: " + Thread.currentThread().getName());
				mAudioRecord.startRecording();
				mMainHandler.sendEmptyMessage(MsgIds.RECORD_READY);
				int samplesRead = mAudioRecord.read(mAudioRecordWrapper.mBuffer,  0, mAudioRecordWrapper.mBufferSizeShorts);
				Log.d(TAG, "Audior recorder read " + samplesRead + " audio samples");
				
				Log.d(TAG, "Im Alive 1");
				Message bufferMsg = Message.obtain(mMainHandler, MsgIds.BUFFER_DATA, mAudioRecordWrapper.mBuffer);
				Log.d(TAG, "Im Alive 2");
				mMainHandler.sendMessage(bufferMsg);
				Log.d(TAG, "Im Alive 3");
				//Apply Filter
				//TODO: Requires refactoring, expensive operation here - perhaps its own thread?
				//TODO: Also class is inside deprecated package
				//AudioFilter rxEnergyFilter = AudioFilter.create(mAudioRecordWrapper.mBuffer, mFilter);
				Log.d(TAG, "Audio Filter: " + mAudioFilter.toString());
				Log.d(TAG, "Im Alive 4");
		    	int[] rx_energy = mAudioFilter.filter(mAudioRecordWrapper.mBuffer);
		    	Log.d(TAG, "Im Alive 5");
		    	Message filterMsg = Message.obtain(mMainHandler, MsgIds.FILTER_DATA, rx_energy);
		    	Log.d(TAG, "Im Alive 6");
		    	mMainHandler.sendMessage(filterMsg);
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
			onBufferData(msg.obj);
			break;
		case MsgIds.FILTER_DATA:
			onFilterData(msg.obj);
			break;
		}
		
		return false;
	}
	
	public void onRecordReady(){
		Log.d(TAG,"Main thread notified that Audio Recorder is Ready");
		
		
		Log.d(TAG,"Audio Supervisor sending ping to Pinger Thread");
		mPingerHandler.post(mPinger);
		
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
		
		mCallback.updateFilterData(filter_data);
    	//TODO: Remove test code
    	int zero_count = 0;
    	int noise_count = 0;
    	int small_count = 0;
    	int big_count = 0;
    	int very_big_count = 0;
    	int ZERO = 0;
    	int NOISE = 200; // mean 150-200 stddev 190-240  TODO: Check from app
    	int SMALL = 440;
    	int BIG = 680;
    	int SIGNAL_THRESH = 8192;
    	ArrayList<Integer> vb_idxs = new ArrayList<Integer>();
    	ArrayList<Integer>  b_idxs = new ArrayList<Integer>();
    	int[] signal_idxs = new int[2];
    	signal_idxs[0] = -1;
    	signal_idxs[1] = -1;
    	
    	int sum=0;
    	for (int i=0;i<filter_data.length;i++){
    		
    		int abs_data = Math.abs(filter_data[i]);
    		sum += abs_data;
    		if (abs_data>=SIGNAL_THRESH){
    			if(signal_idxs[0]<=0){
    				signal_idxs[0] = i;
    			} else {
    				signal_idxs[1] = i;
    			}
    		}
    		if (abs_data==ZERO){
    			zero_count++;
    		} else if (abs_data<=NOISE && abs_data>ZERO) {
    			noise_count++;
    		} else if (abs_data<=SMALL && abs_data>NOISE) {
    			small_count++;
    		} else if (abs_data<=BIG && abs_data>SMALL) {
    			big_count++;
    			b_idxs.add(i);
    		} else if (abs_data>BIG) {
    			very_big_count++;
    			vb_idxs.add(i);
    		}
    		//	tv.append(" " +buffer[i]);
    	}
    	
    	Log.d(TAG, "Average: " + (double)sum/filter_data.length);
    	Log.d(TAG, "Standard Dev: " + Stat.calcStanDev(filter_data.length, filter_data));
    	Log.d(TAG, "Signal Indices: " + Arrays.toString(signal_idxs));
    	Log.d(TAG, "Signal Width: " + (signal_idxs[1] - signal_idxs[0]));
    	Log.d(TAG, "Zeros: " + zero_count);
    	Log.d(TAG, "Noise: " + noise_count);
    	Log.d(TAG, "Small: " + small_count);
    	Log.d(TAG, "Big: " + big_count);
    	Log.d(TAG, "Very Big: " + very_big_count);
    	//Log.d(TAG, "Big Indexes: " + b_idxs.toString());
    	//Log.d(TAG, "Very Big Indexes: " + vb_idxs.toString());
	}
}


