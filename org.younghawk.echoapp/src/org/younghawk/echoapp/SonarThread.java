package org.younghawk.echoapp;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

//change to private and final
public class SonarThread extends Thread {
	private static final String TAG = "EchoApp SonarThread";
    public Handler handler;

	
	public SonarThread(SonarThreadListener listener){
		handler = null;
	}
	
	@Override 
	public void run() {
		super.run();
		Log.i(TAG,"Running Sonar Thread");
		try {
			Looper.prepare();
			handler = new Handler();
			Looper.loop();
			Log.i(TAG,"Exiting Sonar Thread");
		} catch (Throwable t) {
			Log.e(TAG, "halted due to error", t);
		}
	}
	
	public synchronized void ping(){
		Log.i(TAG, "Sonar Thread received ping request");
	}
}
