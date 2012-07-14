package org.younghawk.echoapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class EchoApp extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
    public void pingButton(View view) {
    	Log.v("pingButton", "Ping Button Pressed");
    }
    
   

}