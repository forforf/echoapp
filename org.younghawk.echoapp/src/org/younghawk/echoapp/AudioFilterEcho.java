package org.younghawk.echoapp;

import android.util.Log;

public class AudioFilterEcho implements AudioFilterStub {
    private static final String TAG = "EchoApp AudioFilterEcho";
    
	private int[] mFilter;
	private int mWindowSize;
	private int mAudioBufferSize;
	private int[] mAudioSnip;
	private int[] mAudioEnergy;
	private int mIntervalEnergy;
	
	private int[] mAudioBuffer;
	private int[] mCombinedBuffer; //combines left over from previous iteration
    private int mCombinedBufferSize;
    private int mPreviousSize;
	
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
	  public static AudioFilterEcho create(short[] filter_short) {
	      int[] filter = new int[filter_short.length];
	      for(int i=0;i<filter_short.length;i++){
	          filter[i] = (int) filter_short[i];
	      }
		  return new AudioFilterEcho(filter);
	  }

	  private AudioFilterEcho(int[] filter) {
	      Log.d(TAG, "Dead Code Test -- Constructed!!");
		  this.mFilter = filter;
		  this.mWindowSize = filter.length;
		  //this.mAudioBufferSize = audio_buffer_size;
		  this.mAudioSnip = new int[filter.length];
		  //this.mAudioEnergy = new int[audio_buffer_size];
		  this.mIntervalEnergy = 0;
	  }
	  
	  @Override
	  public int[] filter(short[] audio_buffer_short) {
	      
	      mAudioBufferSize = audio_buffer_short.length;
	      mCombinedBufferSize = mAudioBufferSize + mWindowSize -1;
	      
	      //Dealing with filters bigger than the buffer size is more complicated
	      //so we're deferring it for now2
	      //TODO: Should this be AudioBufferSize or CombinedBufferSize?
	      if(mAudioBufferSize < mWindowSize + 2){
	          throw new RuntimeException("Filter size larger than audio buffer not supported yet");
	      }
	      
	      //We need the at least a filter size worth of data before we can calculate
	      //Filter energy
	      
	        //Note mAudioSnip will have its value from the previous iteration
	        //This is intentional
	      
	        int prev_snip_start = 1;
	        int prev_snip_size = mWindowSize-1;
	        if(mAudioBufferSize==mPreviousSize){
	            for(int i=0;i<mAudioBufferSize;i++){
	                mAudioBuffer[i] = (int) audio_buffer_short[i];
	            }
	            System.arraycopy(mAudioSnip, prev_snip_start, mCombinedBuffer, 0, prev_snip_size);
                System.arraycopy(mAudioBuffer, 0, mCombinedBuffer, prev_snip_size, mAudioBufferSize);
	        } else {
	            //we have to create a new one
	            mAudioBuffer = new int[mAudioBufferSize];
	            mCombinedBuffer = new int[mCombinedBufferSize];
	            for(int i=0;i<mAudioBufferSize;i++){
	                mAudioBuffer[i] = (int) audio_buffer_short[i];
	            }
	            System.arraycopy(mAudioSnip, prev_snip_start, mCombinedBuffer, 0, prev_snip_size);
                System.arraycopy(mAudioBuffer, 0, mCombinedBuffer, prev_snip_size, mAudioBufferSize);
	        }
	        mPreviousSize = mAudioBufferSize;
	        
	        
	      
		  //audio_buffer.length should equal audio_buffer_size
		  for(int i=0;i<(mCombinedBufferSize-mWindowSize+1);i++) {
			  //short[] audio_snip = Calc.audioBufferSnip(audio_buffer, mWindowSize, i);
			  
			  
			  //get filter sized portion of the audio data to an audio snip
		      //mAudioSnip data is carried over from previous iteration
		      //this is intentional
		      
			  System.arraycopy(mCombinedBuffer, i, mAudioSnip, 0, mWindowSize);
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
