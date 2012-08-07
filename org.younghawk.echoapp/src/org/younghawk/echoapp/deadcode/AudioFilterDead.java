package org.younghawk.echoapp.deadcode;

import android.util.Log;

public class AudioFilterDead {
    private static final String TAG = "EchoApp AudioFilterDead";
	private short[] mFilter;
	private int mWindowSize;
	private int mAudioBufferSize;
	private short[] mAudioSnip;
	private int[] mAudioEnergy;
	private int mIntervalEnergy;
	
	//flipped to private
	private static class Calc{
		
		/**
		 * Returns a snip of audio data based on window size
		 * @param audio_buffer
		 * @param window_size
		 * @param index
		 * @return
		 */
		public static short[] audioBufferSnip(short [] audio_buffer, int window_size, int index) {
			short[] snip = new short[window_size];
			for (int i=index;i<index+window_size;i++) {
				snip[i-index] = audio_buffer[i];
			}
			return snip;
	      
	    }
	    public static int intervalEnergy(short[] audio_buffer_snip, short[] filter){
	    	int energy = 0;
	    	for (int i=0;i<filter.length;i++) {
	    		energy += audio_buffer_snip[i] * filter[i];
	    	}
	    	return energy;
	    }
	}

	  //Knowing audio buffer size allows us to pre-allocate resources, improving efficiency
	  //flipped to private
	  public static AudioFilterDead create(short[] filter, int audio_buffer_size) {
		  return new AudioFilterDead(filter, audio_buffer_size);
	  }

	  private AudioFilterDead(short[] filter, int audio_buffer_size) {
	      Log.d(TAG, "Dead Code Test -- Constructed!!");
		  this.mFilter = filter;
		  this.mWindowSize = filter.length;
		  this.mAudioBufferSize = audio_buffer_size;
		  this.mAudioSnip = new short[filter.length];
		  this.mAudioEnergy = new int[audio_buffer_size];
		  this.mIntervalEnergy = 0;
	  }
	  
	  //flipped to private
	  private int[] filter(short[] audio_buffer) {
		  //audio_buffer.length should equal audio_buffer_size
		  for(int i=0;i<(mAudioBufferSize-mWindowSize+1);i++) {
			  //short[] audio_snip = Calc.audioBufferSnip(audio_buffer, mWindowSize, i);
			  
			  
			  //get filter sized portion of the audio data to an audio snip
			  System.arraycopy(audio_buffer, i, mAudioSnip, 0, mWindowSize);
			  mIntervalEnergy = 0;
			  for(int j=0;j<mWindowSize;j++) {
				  mIntervalEnergy += mAudioSnip[j] * mFilter[j];
			  }
			 mAudioEnergy[i] = mIntervalEnergy;
			  
			  
			  //copies Calc.audioBufferSnip() to mAudioSnip
			  //System.arraycopy(Calc.audioBufferSnip(audio_buffer, mWindowSize, i), 0, mAudioSnip, 0, mWindowSize);
			  
			  //mAudioEnergy[i] = Calc.intervalEnergy(mAudioSnip, mFilter);
		  }
		  return mAudioEnergy;
	  }
}
