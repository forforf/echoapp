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
	private AudioRecordWrapper mAudioRecordWrapper;
	private static final int SAMPPERSEC = 44100; 
	private static final double MAX_SAMPLE_TIME = 1.0; //in seconds
	//public short[] mBuffer;  //TODO: Make sure this is uded correctly
	private AudioRecord mAudioRecord;
	
	//Message Definitions
	private static final int RECORD_READY = 0;
	private static final int BUFFER_DATA = 1;


	public static AudioSupervisor create() {
		HandlerThread mAudioRecordThr = new HandlerThread("Audio Recorder");
		mAudioRecordThr.start();
        
        HandlerThread mAudioBufferThr = new HandlerThread("Audio Buffering");
        mAudioBufferThr.start();
        
        AudioRecordWrapper audioRecordWrapper = AudioRecordWrapper.create(SAMPPERSEC, MAX_SAMPLE_TIME);
        //AudioRecord audioRecord = audioRecordWrapper.mAudioRecord;
        
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
        
		return new AudioSupervisor(mAudioRecordThr, mAudioBufferThr, audioHandler, bufferHandler, audioRecordWrapper);
	}
	
	private AudioSupervisor(HandlerThread audioRecThr, HandlerThread audioBufThr, Handler audioHandler, Handler bufferHandler, AudioRecordWrapper audioRecordWrapper) {
		this.mAudioRecordThr = audioRecThr;
		this.mAudioBufferThr = audioBufThr;
		this.mAudioRecordHandler = audioHandler;
		this.mAudioBufferHandler = bufferHandler;
		this.mMainHandler = new Handler(this);
		this.mAudioRecordWrapper = audioRecordWrapper;
		this.mAudioRecord = audioRecordWrapper.mAudioRecord;
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
		}
		
		return true;
	}
	
	public void onRecordReady(){
		Log.d(TAG,"Main thread notified that Audio Recorder is Ready");
	}
	
	public void onBufferData(Object objBuffer){
		short[] buffer = (short[]) objBuffer;
		Log.d(TAG, "Main thread notified of buffer with " + buffer.length + " samples");
	}
}


