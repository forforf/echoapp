package org.younghawk.echoapp;

import android.media.AudioRecord;
import android.media.AudioFormat;
import android.util.Log;

public class AudioRecordRunnable implements Runnable {
	private static final String TAG = "EchoApp AudioRecordRunnable";
	public AudioRecord mAudioRecord;
	public int mBuffersizeshorts;
	public short[] mBuffer;
	public static final int SAMPPERSEC = 44100; 
	public static final double MAX_SAMPLE_TIME = 1; //in seconds
	
	
	public static AudioRecordRunnable create() {
		int buffersizeshorts = (int) Math.round((double) SAMPPERSEC * MAX_SAMPLE_TIME); //for 16bit PCM
        int buffersizebytes = buffersizeshorts * 2;
        short[] buffer = new short[buffersizeshorts];
    	AudioRecord audioRecord = null;
        try {
	        audioRecord = new AudioRecord(
	        		android.media.MediaRecorder.AudioSource.MIC, 
	        		SAMPPERSEC, 
	        		AudioFormat.CHANNEL_IN_MONO, 
	        		AudioFormat.ENCODING_PCM_16BIT, 
	        		buffersizebytes);
        } catch (IllegalArgumentException e) {
        	Log.e(TAG, "Unable to create AudioRecord (IllegalArgumentException)");
        	//TODO: Have UI Error handler (in caller, not here)
        }
        
        //Don't construct thread if audioRecord is null
        AudioRecordRunnable audioRecordRunnable = null;
        if (audioRecord!= null) {
        	Log.v(TAG, "AudioRecord should be ready?");
        	audioRecordRunnable = new AudioRecordRunnable(audioRecord, buffer, buffersizeshorts);
        } else {
        	Log.e(TAG, "Failed to initialize AudioRecord (was null)");
        }
        
		return audioRecordRunnable;
		
	}
	
	private AudioRecordRunnable(AudioRecord audioRecord, short[] buffer, int buffersizeshorts) {
		this.mAudioRecord = audioRecord;
		this.mBuffer = buffer;
		this.mBuffersizeshorts = buffersizeshorts;
	}
	
	@Override
	public void run() {
		Log.v(TAG, "Running Audio Record Thread");
		try {
			Log.v(TAG, "Trying to start AudioRecord: " + mAudioRecord);
    		//mAudioRecord.startRecording();
    		//onRecordReady();

    		//mAudioRecord.stop();
    		//onRecordDone(mBuffer);
    		
    	} catch(Throwable t) {
    		Log.e(TAG, "Recording Failed", t);
    	}

	}
	
	
}
