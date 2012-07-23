package org.younghawk.echoapp;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class Sonar implements Runnable {
	private static final String TAG= "EchoApp SonarThread";

	//This thread handler
	public final Handler mHandler;
	

	//Message Handler
	//I prefer named classes to anonymous classes given no other compelling reason
	//public class SonarHandler extends Handler {
	//	public void handleMessage(Message msg) {
	//		Log.i(TAG, "SonarHandler handle message method");
	//	}
	//}
	
	//factory method and message handler definition
	public static Sonar create(){
		Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				Log.i(TAG, "Sonar handled message method");
			}
		};
		return new Sonar(handler);
	}
	//constructor
	private Sonar(Handler myHandler) {
		this.mHandler = myHandler;
	}

	@Override
	public void run() {
		Log.i(TAG, "sonar thread starting");
		Looper.prepare();

		Looper.loop();
	}
	
	
	public void onPing(){
		Log.i(TAG, "On Ping method called");
	}
	public Handler getHandler(){
		return mHandler;
	}
}


