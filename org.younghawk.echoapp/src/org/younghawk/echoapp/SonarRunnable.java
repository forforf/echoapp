package org.younghawk.echoapp;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class SonarRunnable implements Runnable {
	private static final String TAG= "EchoApp SonarThread";
	//Main UI Thread Handler
	public Handler mParentHandler;
	//This thread handler
	public Handler mHandler;

	//Message Handler
	//I prefer named classes to anonymous classes given no other compelling reason
	//public class SonarHandler extends Handler {
	//	public void handleMessage(Message msg) {
	//		Log.i(TAG, "SonarHandler handle message method");
	//	}
	//}
	
	//factory method and message handler definition
	public static SonarRunnable create(Handler parentHandler){
		Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				Log.i(TAG, "Sonar handled message method");
			}
		};
		return new SonarRunnable(parentHandler, handler);
	}
	//constructor
	private SonarRunnable(Handler parentHandler, Handler myHandler) {
		this.mParentHandler = parentHandler;
		this.mHandler = myHandler;
	}

	@Override
	public void run() {
		Log.i(TAG, "sonar message handler starting");
		Looper.prepare();

		Looper.loop();
	}
	
	public Handler getHandler(){
		return mHandler;
	}
}


