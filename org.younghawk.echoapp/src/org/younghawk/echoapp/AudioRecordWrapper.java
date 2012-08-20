package org.younghawk.echoapp;

import android.media.AudioRecord;

import android.util.Log;

public class AudioRecordWrapper {
	
	private final static int SOURCE = android.media.MediaRecorder.AudioSource.MIC;
	private final static int FORMAT = android.media.AudioFormat.CHANNEL_IN_MONO;
	private final static int ENCODING = android.media.AudioFormat.ENCODING_PCM_16BIT;
	public AudioRecord mAudioRecord;
	public short[] mBuffer;
	public int mBufferSizeShorts;

	
	public static AudioRecordWrapper create(int samp_per_sec, double max_sample_time) {
	    //TODO: Put in check for minimum buffer size
		int buffersizeshorts = (int) Math.round((double) samp_per_sec * max_sample_time); //for 16bit PCM
		int buffersizebytes = buffersizeshorts * 2;
		short[] buffer = new short[buffersizeshorts];
		AudioRecord audioRecord = null;
		try {
			audioRecord = new AudioRecord(
					SOURCE, 
					samp_per_sec, 
					FORMAT, 
					ENCODING, 
					buffersizebytes);
		} catch (IllegalArgumentException e) {
			Log.e("EchoApp", "Unable to create AudioRecord (IllegalArgumentException)");
			//TODO: Have UI Error handler (in caller, not here)
		}
		
		return new AudioRecordWrapper(audioRecord, buffer, buffersizeshorts);
	}
	
	private AudioRecordWrapper(AudioRecord audioRecord, short[] buffer, int buffersizeshorts) {
		this.mAudioRecord = audioRecord;
		this.mBuffer = buffer;
		this.mBufferSizeShorts = buffersizeshorts;
	}
}
