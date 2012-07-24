package org.younghawk.echoapp;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class EchoAppPreferences extends PreferenceActivity {
	private final String TAG = "EchoApp Preferences";
	//protected Method mLoadHeaders = null;
	//protected Method mHasHeaders = null;
	/*
	public boolean isNewV11Prefs() {
		if (mHasHeaders!=null && mLoadHeaders!=null) {
			try {
				return (Boolean)mHasHeaders.invoke(this);
			} catch (IllegalArgumentException e) {
				Log.e(TAG, "Illegal Argument: " + e);
			} catch (IllegalAccessException e) {
				Log.e(TAG, "Illegal Access: " + e);
			} catch (InvocationTargetException e) {
				Log.e(TAG, "Illegal Invocation: " + e);
			}
		}
		return false;
	}
    */
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	//onBuildHeaders() is called durion super.onCreate()
    	/*
    	try {
            mLoadHeaders = getClass().getMethod("loadHeadersFromResource", int.class, List.class );
            mHasHeaders = getClass().getMethod("hasHeaders");
        } catch (NoSuchMethodException e) {
        	Log.e(TAG, "No Such Method: " + e);
        }
        */
    	Log.i(TAG, "Preferences On Create called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_echo_app_preferences);
        addPreferencesFromResource(R.xml.preferences);
        /*
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
         }
        if (!isNewV11Prefs()) {
            addPreferencesFromResource(R.xml.preferences);
        }
        */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_echo_app_preferences, menu);
        return true;
    }
    /*
    @Override
    public void onBuildHeaders(List<Header> aTarget) {
        try {
            mLoadHeaders.invoke(this,new Object[]{R.xml.pref_headers,aTarget});
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }   
    }
 
    static public class PrefsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle aSavedState) {
            super.onCreate(aSavedState);
            Context anAct = getActivity().getApplicationContext();
            int thePrefRes = anAct.getResources().getIdentifier(getArguments().getString("pref-resource"),
                    "xml",anAct.getPackageName());
            addPreferencesFromResource(thePrefRes);
        }
    }
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
   
}
