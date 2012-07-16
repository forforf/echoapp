package org.younghawk.echoapp.signals;

import org.json.*;

public class SignalGenerator {
	
	//helper methods
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
			    	Integer intObj = Integer.valueOf(arr[i]); 
				    shorts[i] = intObj.shortValue();
			    }
			} else {
				shorts = new short[arr.length + 1];
				for(int i=0; i<arr.length; i++) {
			    	Integer intObj = Integer.valueOf(arr[i]); 
				    shorts[i] = intObj.shortValue();
			    }
				shorts[arr.length] = 0;
				
			}
			return shorts;
		}
	
	}

	public static SignalGenerator create(String user_instructions, int wave_samples) throws JSONException {
		JSONArray user_instr;

		//parse the json instructions
		user_instr = new JSONArray(user_instructions);

		//each instruction defines a signal and iteration
		//each iteration is an array, and each signal is an array
		//so we have an array[iterations][signal data]
		int[][] sigs = new int[user_instr.length()][];
		
		for (int i=0; i<user_instr.length(); i++) {
			//get the user instructions
			JSONObject wave_instr = user_instr.getJSONObject(i);
			String waveform = wave_instr.getString("waveform");
			int iterations = wave_instr.getInt("iterations");
			
			//create the signal accordingly
			SignalType sig_type = Signal.create(waveform, wave_samples);
            int[] sig = sig_type.getSignal();
            
            //iterate the signal based on # of iterations in instructions
			int[] sig_iters = ArrayCalcs.copy(sig, iterations);
			sigs[i] = sig_iters;
		}

		//flatten the signal
		int[] int_signal = ArrayCalcs.flatten(sigs);
		
		//AudioTrack requires shorts so convert it
		short[] pcm_signal = ArrayCalcs.toShort(int_signal);
		
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
