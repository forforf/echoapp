package org.younghawk.echoapp.deadcode;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class AudioRecordRunnableDeadCode implements Runnable {
	private static final String TAG = "EchoApp AudioRecordRunnableDeadCode";
	public AudioRecord mAudioRecord;
	public int mBuffersizeshorts;
	public short[] mBuffer;
	public static final int SAMPPERSEC = 44100; 
	public static final double MAX_SAMPLE_TIME = 1; //in seconds
	public Handler handler;
	
	
	public static AudioRecordRunnableDeadCode create() {
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
        AudioRecordRunnableDeadCode audioRecordRunnableDeadCode = null;
        if (audioRecord!= null) {
        	Log.v(TAG, "AudioRecord should be ready?");
        	audioRecordRunnableDeadCode = new AudioRecordRunnableDeadCode(audioRecord, buffer, buffersizeshorts);
        } else {
        	Log.e(TAG, "Failed to initialize AudioRecord (was null)");
        }
        
		return audioRecordRunnableDeadCode;
		
	}
	
	private AudioRecordRunnableDeadCode(AudioRecord audioRecord, short[] buffer, int buffersizeshorts) {
		this.mAudioRecord = audioRecord;
		this.mBuffer = buffer;
		this.mBuffersizeshorts = buffersizeshorts;
	}
	
	@Override	
	public void run() {
		Log.v(TAG, "Running Audio Record Thread");
		try {
			Log.v(TAG, "Trying to start AudioRecord: " + mAudioRecord);
			Looper.prepare();
			handler = new Handler() {
				
			};
			Looper.loop();
    		//mAudioRecord.startRecording();
    		//onRecordReady();

    		//mAudioRecord.stop();
    		//onRecordDone(mBuffer);
    		
    	} catch(Throwable t) {
    		Log.e(TAG, "Recording Failed", t);
    	}

	}
		
}
