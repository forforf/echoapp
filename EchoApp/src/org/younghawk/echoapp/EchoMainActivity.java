package org.younghawk.echoapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

public class EchoMainActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_echo_main);
        //TextView text = new TextView(this);
        //text.setText("Hello Echo World, from Dave");
        //setContentView(text);
    }

    /**
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_echo_main, menu);
        return true;
    }

    **/    
}
