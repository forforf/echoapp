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
	private Handler mAudioRecordHandler; //Handler for hw thread
	private Handler mAudioBufferHandler; //Handler for data thread
	private Handler mMainHandler; //Handler for this thread (main thread)
	//Audio Data
	private AudioRecord mAudioRecord;
	private static final int SAMPPERSEC = 44100; 
	private static final double MAX_SAMPLE_TIME = 1.0; //in seconds
	public short[] mBuffer;  //TODO: Make sure this is uded correctly
	
	//Message Definitions
	private static final int RECORD_READY = 0;


	public static AudioSupervisor create() {
		HandlerThread mAudioRecordThr = new HandlerThread("Audio Recorder");
		mAudioRecordThr.start();
        
        HandlerThread mAudioBufferThr = new HandlerThread("Audio Buffering");
        mAudioBufferThr.start();
        
        AudioRecord audioRecord = AudioRecordFactory.create(SAMPPERSEC, MAX_SAMPLE_TIME);
        
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
        
		return new AudioSupervisor(mAudioRecordThr, mAudioBufferThr, audioHandler, bufferHandler, audioRecord);
	}
	
	private AudioSupervisor(HandlerThread audioRecThr, HandlerThread audioBufThr, Handler audioHandler, Handler bufferHandler, AudioRecord audioRecord) {
		this.mAudioRecordThr = audioRecThr;
		this.mAudioBufferThr = audioBufThr;
		this.mAudioRecordHandler = audioHandler;
		this.mAudioBufferHandler = bufferHandler;
		this.mMainHandler = new Handler(this);
		this.mAudioRecord = audioRecord;
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
			}
		});
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case RECORD_READY:
			onRecordReady();
			break;
		}
		
		return true;
	}
	
	public void onRecordReady(){
		Log.d(TAG,"Main thread notified that Audio Recorder is Ready");
	}
}


