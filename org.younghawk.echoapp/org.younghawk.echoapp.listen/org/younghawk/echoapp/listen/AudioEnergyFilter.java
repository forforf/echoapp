package org.younghawk.echoapp.listen;

public class AudioEnergyFilter {
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

	  public static AudioEnergyFilter create(short[] audio_buffer, short[] filter) {
		  int[] audioEnergy = new int[audio_buffer.length];
		  int windowsize = filter.length;   

		  for(int i=0;i<(audio_buffer.length-windowsize+1);i++) {
			  short[] audio_snip = Calc.audioBufferSnip(audio_buffer, windowsize, i);
			  audioEnergy[i] = Calc.intervalEnergy(audio_snip, filter);

		  }

		  return new AudioEnergyFilter(audio_buffer, filter, audioEnergy);
	  }

	  public short[] mAudioBuffer;
	  public short[] mFilter;
	  public int[] mAudioEnergy;

	  private AudioEnergyFilter(short[] audio_buffer, short[] filter, int[] audioEnergy) {
		  mAudioBuffer = audio_buffer;
		  mFilter = filter;
		  mAudioEnergy = audioEnergy;
	  }
	}
