package org.younghawk.echoapp.listen;

public interface RecordAudioEvents {
	public void onRecordReady();
	
	public void onRecordDone(short[] buffer);
}
