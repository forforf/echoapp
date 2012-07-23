package org.younghawk.echoapp.listen;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.Log;

public class ListenThread implements Runnable {
	public AudioRecord mAudioRecord;
	public RecordAudioEvents mCallback;
	//public int buffersizebytes;
	public int mBuffersizeshorts;
	//public static final int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
	//public static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;  
	public short[] mBuffer;
	public static final int SAMPPERSEC = 44100; 
	public static final double MAX_SAMPLE_TIME = 1; //in seconds
	
	public static ListenThread create(RecordAudioEvents callback) {
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
        	Log.e("EchoApp", "Unable to create AudioRecord (IllegalArgumentException)");
        	//TODO: Have UI Error handler (in caller, not here)
        }
        
        //Don't construct thread if audioRecord is null
        ListenThread listenThread = null;
        if (audioRecord!= null) {
        	Log.v("EchoApp", "AudioRecord should be ready?");
        	listenThread = new ListenThread(audioRecord, buffer, buffersizeshorts, callback);
        } else {
        	Log.e("EchoApp", "Failed to initialize AudioRecord (was null)");
        }
		return listenThread;
	}
	
	private ListenThread(AudioRecord audioRecord, short[] buffer, int buffersizeshorts, RecordAudioEvents callback) {
		this.mAudioRecord = audioRecord;
		this.mBuffer = buffer;
		this.mBuffersizeshorts = buffersizeshorts;
		this.mCallback = callback;
	}
	
	@Override
	public void run() {
		Log.v("EchoApp", "Running Listen Thread");
		try {
			Log.v("EchoApp", "Trying to start AudioRecord: " + mAudioRecord);
    		mAudioRecord.startRecording();
    		onRecordReady();

    		mAudioRecord.stop();
    		onRecordDone(mBuffer);
    		
    	} catch(Throwable t) {
    		Log.e("EchoApp", "Recording Failed", t);
    	}

	}
	
	public void onRecordReady() {
		Log.v("EchoApp", "Ready to Record");
		mCallback.onRecordReady();
		int samplesRead = mAudioRecord.read(mBuffer,  0, mBuffersizeshorts);
		Log.v("EchoApp", "Number of samples read: " + samplesRead + "  buffer size(shorts): " + mBuffersizeshorts);
		Log.v("EchoApp", "Buffer data size: " + mBuffer.length);
	}
	
	public void onRecordDone(short[] buffer) {
    	Log.v("EchoApp", "buffer size: " + buffer.length);
       	//Log.v("AudioRecord", Arrays.toString(buffer));

    	mCallback.onRecordDone(buffer);
	}

}
