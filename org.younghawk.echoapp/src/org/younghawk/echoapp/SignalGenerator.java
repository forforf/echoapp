package org.younghawk.echoapp;

import org.json.*;
import android.util.Log;

public class SignalGenerator {

	public static SignalGenerator create(String user_instructions_json) {
		Log.v("SignalGenerator says", user_instructions_json);
		return new SignalGenerator();
	}
	private SignalGenerator() {
		
	}

}
