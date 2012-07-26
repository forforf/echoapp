package org.younghawk.echoapp;

public class AudioFilter {
	private short[] mFilter;
	private int mWindowSize;
	private int mAudioBufferSize;
	private short[] mAudioSnip;
	private int[] mAudioEnergy;
	private int mIntervalEnergy;
	
	public static class Calc{
		
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
	  public static AudioFilter create(short[] filter, int audio_buffer_size) {
		  return new AudioFilter(filter, audio_buffer_size);
	  }

	  private AudioFilter(short[] filter, int audio_buffer_size) {
		  this.mFilter = filter;
		  this.mWindowSize = filter.length;
		  this.mAudioBufferSize = audio_buffer_size;
		  this.mAudioSnip = new short[filter.length];
		  this.mAudioEnergy = new int[audio_buffer_size];
		  this.mIntervalEnergy = 0;
	  }
	  
	  public int[] filter(short[] audio_buffer) {
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
