package org.younghawk.echoapp.signals;

import org.json.*;

import android.util.Log;

/**
 * Container that creates and holds waveform information
 *
 */
public class SignalGenerator {
	
	/**
	 * Container for Array helper methods
	 *
	 */
	public static class ArrayCalcs {
		
		/**
		 * Flattens a 2D array to a 1D array
		 * Used to concatenate waveforms
		 * @param arr
		 * @return
		 */
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
		
		/**
		 * Duplicates an array the given number of iterations
		 * @param arr
		 * @param iters
		 * @return
		 */
		public static int[] copy(int[] arr, int iters){
			if (arr == null) return arr;
			int[] copies = new int[arr.length * iters];
			for (int i=0; i<iters; i++){
				System.arraycopy(arr,  0,  copies, i*arr.length, arr.length);				
			}
			return copies;
		}
		
		/**
		 * Converts an int array to a short array
		 * All values of the int array should be between -32768 to 32767
		 * for predictable behavior
		 * @param arr
		 * @return
		 */
		//TODO: Check and issue warning if int array value is greater than short
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

	/**
	 * Factory for converting the instructions into the full signal waveform
	 * @param user_instructions
	 * @param num_of_samples
	 * @return
	 */
	public static SignalGenerator create(String user_instructions, int num_of_samples)  {
		JSONArray user_instr = null;

		//parse the json instructions
		try {
			user_instr = new JSONArray(user_instructions);
		} catch (JSONException e) {
			Log.e("EchoApp", "SignalGenerator failed to parse the JSON instructions array");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//If we failed to parse the user instructions, then don't
		// create the Signal Generator
		if (user_instr == null){
			Log.e("EchoApp", "SignalGenerator could not be created");
			return null;
		}
		
		//each instruction defines a signal and iteration
		//each iteration is an array, and each signal is an array
		//so we have an array[iterations][signal data]
		int[][] sigs = new int[user_instr.length()][];
		
			for (int i=0; i<user_instr.length(); i++) {
				//get the user instructions
				JSONObject wave_instr = null;
				String waveform = null;;
				int iterations = 0;
				
				//parsing the instruction elements
				try {
					wave_instr = user_instr.getJSONObject(i);
					waveform = wave_instr.getString("waveform");
					iterations = wave_instr.getInt("iterations");
				} catch (JSONException e) {
					Log.e("EchoApp", "SignalGenerator failed to parse instruction set: " + i);
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//create the signal accordingly
				SignalType sig_type = Signal.create(waveform, num_of_samples);
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
	
	private short[] mSignal;
	
	private SignalGenerator(short[] sig) {
        this.mSignal = sig;
	}
	
	public short[] getSignal(){
		return this.mSignal;
	}
}
