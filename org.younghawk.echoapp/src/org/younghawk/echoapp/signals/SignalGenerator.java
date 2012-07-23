package org.younghawk.echoapp.signals;

import org.json.*;

import android.util.Log;

/**
 * Container that creates and holds waveform information
 */
public class SignalGenerator {
	
	// Public variables are used rather than getters or setters
	// as recommended by Google
	public short[] mSignal;
	public short[] mFilterMask;
	
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
		public static short[] toShort(int[] arr){
			short[] shorts = new short[arr.length];
		    for(int i=0;i<arr.length;i++) {
		    	shorts[i] = clipToShort(arr[i]);
			}
			return shorts;
		}

		/**
		 * Clips integers to Short
		 * Since integers are 4 bytes and shorts are only 2 bytes
		 * TODO: Better solution is to refactor signal classes to use shorts rather than ints (lot of work)
		 * @param x
		 * @return
		 */
		public static short clipToShort(int x) {
			if (x>32767) {
				Log.w("EchoApp", "Somehow a value > 32767 was obtained, clipping to 32767");
				x = 32767;
			} else if (x<-32768) {
				Log.w("EchoApp", "Somehow a value < -32768 was obtained, clipping to -32768");
				x = -32768;
			}
			 
			Integer intObj = Integer.valueOf(x);
			return intObj.shortValue();
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
			Log.v("EchoApp", "User instructions: " + user_instructions);
			user_instr = new JSONArray(user_instructions);
			
		} catch (JSONException e) {
			Log.e("EchoApp", "SignalGenerator failed to parse the JSON instructions array", e);
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
		int[][] filter_masks = new int[user_instr.length()][];
		
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
				
				//create the signal and filter mask accordingly
				SignalType sig_type = SignalFactory.create(waveform, num_of_samples);
				
				int[] sig = null;
				int[] sig_filter_mask = null;
				if (sig_type!=null){
					sig = sig_type.getSignal();
					sig_filter_mask = sig_type.filterMask();
				} else {
					Log.e("EchoApp", "Unable to create Signal (Waveform Type)");
				}
				
	            //iterate the signal based on # of iterations in instructions
				int[] sig_iters = null;
				if (sig!=null) {
					sig_iters = ArrayCalcs.copy(sig, iterations);
				} else {
					Log.e("EchoApp", "sig was null");
				}
				sigs[i] = sig_iters;
				
				//build filtermask basedon # of iterations
				int[] filter_mask_iters = null;
				if (sig_filter_mask!=null){
					filter_mask_iters = ArrayCalcs.copy(sig_filter_mask, iterations);	
				} else {
					Log.e("EchoApp", "filter mask was null");
				}
				filter_masks[i] = filter_mask_iters;
			}

		
		//flatten the signal and filter mask
		int[] int_signal = ArrayCalcs.flatten(sigs);
		int[] int_filter_mask = ArrayCalcs.flatten(filter_masks);
		
		//AudioTrack requires shorts so convert it
		short[] pcm_signal = ArrayCalcs.toShort(int_signal);
		short[] filter_mask_gen = ArrayCalcs.toShort(int_filter_mask);
		
		return new SignalGenerator(pcm_signal, filter_mask_gen);	
	}
	
	
	/**
	 * Create the Signal Genrator from the waveform signal
	 * @param sig
	 */
			
	private SignalGenerator(short[] sig, short[] filter_mask) {
        this.mSignal = sig;
        this.mFilterMask = filter_mask;
	}
	
}
