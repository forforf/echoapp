package org.younghawk.echoapp.signals;

import java.util.Arrays;

import org.json.*;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class SignalGenerator {
	
	public static class ArrayCalcs {
		public static int[] flatten(int[][] arr){
			int elements_count = 0;
			for (int i=0;i<arr.length;i++){
				elements_count += arr[i].length;
			}
			
			int[] flat_array = new int[elements_count];
			
			int j=0;
			for (int i=0; i<arr.length;i++) {
				System.arraycopy(arr[i],  0, flat_array, j, arr[i].length);
				j += arr[i].length;
			}
			
			return flat_array;
		}
		
		public static int[] copy(int[] arr, int iters){
			if (arr == null) return arr;
			int[] copies = new int[arr.length * iters];
			for (int i=0; i<iters; i++){
				System.arraycopy(arr,  0,  copies, i*arr.length, arr.length);				
			}
			return copies;
		}
		
		public static short[] toShort(int[] arr){
			short[] shorts;
			if (arr.length % 2 == 0){
			    shorts = new short[arr.length];
			    for(int i=0; i<arr.length; i++) {
			    	Integer intObj = new Integer(arr[i]); 
				    shorts[i] = intObj.shortValue();
			    }
			} else {
				shorts = new short[arr.length + 1];
				for(int i=0; i<arr.length; i++) {
			    	Integer intObj = new Integer(arr[i]); 
				    shorts[i] = intObj.shortValue();
			    }
				shorts[arr.length] = 0;
				
			}
			return shorts;
		}
	
	}

	public static SignalGenerator create(String user_instructions, int wave_samples) throws JSONException {
		JSONArray user_instr;
		Log.v("SignalGenerator says", user_instructions);

		//parse the json instructions
		user_instr = new JSONArray(user_instructions);
		
		int[][] sigs = new int[user_instr.length()][];
		
		for (int i=0; i<user_instr.length(); i++) {
			JSONObject wave_instr = user_instr.getJSONObject(i);
			String waveform = wave_instr.getString("waveform");
			int iterations = wave_instr.getInt("iterations");
			Log.v("SignalGenerator instr", waveform);
			Log.v("SignalGenerator instr", "" + iterations);
			SignalType sig_type = Signal.create(waveform, wave_samples);
			Log.v("SignalGenerator sig_type","signal obj created");
            int[] sig = sig_type.getSignal();
			Log.v("SignalGenerator sig_type", Arrays.toString(sig));
			int[] sig_iters = ArrayCalcs.copy(sig, iterations);
			Log.v("SignalGenerator sig", Arrays.toString(sig_iters));
			sigs[i] = sig_iters;
		}

		int[] int_signal = ArrayCalcs.flatten(sigs);
		Log.v("SignalGenerator full_signal", Arrays.toString(int_signal));
		short[] pcm_signal = ArrayCalcs.toShort(int_signal);
		Log.v("SignalGenerator shorts", Arrays.toString(pcm_signal));
		return new SignalGenerator(pcm_signal);	
	}
	
	private short[] signal;
	
	private SignalGenerator(short[] sig) {
        signal = sig;
	}
	
	public short[] getSignal(){
		return signal;
	}
}
