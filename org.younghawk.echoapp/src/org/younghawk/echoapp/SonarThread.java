package org.younghawk.echoapp;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class SonarThread extends Thread {
	private static final String TAG= "EchoApp SonarThread";
	//Main UI Thread Handler
	public Handler mParentHandler;
	//This thread handler
	private Handler mSonarHandler;

	//Message Handler
	//I prefer named classes to anonymous classes given no other compelling reason
	private class SonarHandler extends Handler {
		public void handleMessage(Message msg) {
			Log.i(TAG, "SonarHandler handle message method");
		}
	}
	
	//constructor
	public SonarThread(Handler parentHandler) {
		this.mParentHandler = parentHandler;
	}

	@Override
	public void run() {
		Log.i(TAG, "sonar thread running");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e){
			Log.e(TAG, "Thread interupted: " + e);
		}
	}
}
