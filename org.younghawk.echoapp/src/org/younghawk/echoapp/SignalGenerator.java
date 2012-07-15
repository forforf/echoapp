package org.younghawk.echoapp;

import org.json.*;

import android.util.Log;

public class SignalGenerator {

	public static SignalGenerator create(String user_instructions, int wave_samples) throws JSONException {
		JSONArray user_instr;
		Log.v("SignalGenerator says", user_instructions);

		//parse the json instructions
		user_instr = new JSONArray(user_instructions);
		
		
		for (int i=0; i<user_instr.length(); i++) {
			JSONObject wave_instr = user_instr.getJSONObject(i);
			String waveform = wave_instr.getString("waveform");
			int iterations = wave_instr.getInt("iterations");
			Log.v("SignalGenerator instr", waveform);
			Log.v("SignalGenerator instr", "" + iterations);
			SignalType sig_type = Signal.create(waveform, wave_samples);
			Log.v("SignalGenerator sig_type","signal obj created");
			
		}

		return new SignalGenerator();	
		
	}
	private SignalGenerator() {
		
	}

}
