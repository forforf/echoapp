/*
 * Copyright(c) 2012 David Martin
 * 
 * License is TBD
 * 
 */

package org.younghawk.echoapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;


/**
 * Main Activity for the Echo App
 */
public class EchoApp extends Activity {
	private static final String TAG = "EchoApp";
    
	//App Controller
	private GlobalState gGlobal;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Log.d(TAG, "Views created, setup App");
        gGlobal = GlobalState.getGlobalInstance();
        gGlobal.onEchoAppReady(this);    	
        
        final CheckBox checkBox = (CheckBox) findViewById(R.id.checkBox1);
        checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    gGlobal.echoFilterOn();
                } else {
                    gGlobal.echoFilterOff();
                }
                
            }
            
        });
    }
    
    
    
    
    public void onPause() {
        super.onPause();
        gGlobal.pauseApp();
    }
    
    //TODO: Fix so startButton starts audio, ping button sends signal
    
    //Potentially Helpful code for this
    /*
    Button b = new Button(this);
    b.setOnClickListener(new OnClickListener() {

           public void onClick(View v) {
                         // Perform action on click
                     }

    });
    b.setText(""+ i);
    b.setTag("button"+i);
    b.setWidth(30);
    b.setHeight(20);
    
    public void startButton(View view){
        Log.d(TAG, "Start Button Pressed");
    }
    */
    
    /**
     * Handles pingButton presses (click handler defined in layout)
     *   Retrieves the information needed to build a signal from 
     *   resource files and kicks off a thread that generates the
     *   signal that will be the echo-location "ping".
     * @param view
     */
    public void pingButton(View view) {
    	Log.d(TAG, "Ping Button Pressed");
    	gGlobal.startAudioRecording();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //We don't need the menu inflater because our preference settings are very simple
        //getMenuInflater().inflate(R.menu.activity_echo_app_preferences, menu);
        startActivity(new Intent(this, EchoAppPreferences.class));
        return true;
    }
    

}