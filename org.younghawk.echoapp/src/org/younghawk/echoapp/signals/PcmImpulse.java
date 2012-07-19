package org.younghawk.echoapp.signals;

import android.util.Log;

/**
 * Container for an impulse signal suitable for audio (pcm)
 */
public class PcmImpulse implements SignalType {
	
    //Maximum value the impulse will take
    private static final int MAX_ABS_VALUE = 32767;
    
    //Integer Array of Signal Data
    public int[] mSignal;

    /**
     * Helpers for calculating impulse waveform
     */
    private static class Calc{
    	/**
    	 * returns the maximum value within the array
    	 * @param data
    	 * @return
    	 */
        public static double getMaxValue(double[] data){
             double max_value = data[0];
             for(double val: data){
                 if(val > max_value){
                     max_value = val;
                 }
             }
             return max_value;
        }
        
        public static int getMaxValue(int[] data){
            int max_value = data[0];
            for(int val: data){
                if(val > max_value){
                    max_value = val;
                }
            }
            return max_value;
        }
        
        public static int getMinValue(int[] data){
            int min_value = data[0];
            for(int val: data){
                if(val < min_value){
                    min_value = val;
                }
            }
            return min_value;
        }
        

        /**
         * Returns the denominator of the exponent of the impulse wave function 
         * @param num_of_samples
         * @return
         */
        public static double getExponentDenominator(int num_of_samples){
            double d8 = (double) num_of_samples/8;
            double d8sq = d8*d8;
            double exp_denom = d8sq*Math.E;
            return exp_denom;
        }

        /**
         * Returns the numerator of the exponent of the impulse wave function
         * @param t
         * @return
         */
        public static double getExponentNumerator(int t){
            double exp_num = -( (float) t*t );
            return exp_num;
        }

        /**
         * Returns the full exponent of the impulse wave function 
         * @param num_of_samples
         * @param t
         * @return
         */
        public static double getEnvelopeExponent(int num_of_samples, int t){
            double num = Calc.getExponentNumerator(t);
            double den = Calc.getExponentDenominator(num_of_samples);
            double env_exp = num / den;
            return env_exp;
        }

        /**
         * Returns the envelope of the impulse.
         * This factor is what gives the impulse a "spike"
         * @param num_of_samples
         * @param t
         * @return
         */
        public static double getEnvelope(int num_of_samples, int t){
            double env_exp = Calc.getEnvelopeExponent(num_of_samples, t);
            double env = 2*num_of_samples*Math.exp(env_exp);
            return env;
        }

        /**
         * Returns the modulated wave for the impulse.
         * This factor determines the primary frequency of the impulse
         * and how may amplitude peaks it contains within the envelope
         * (Typically set for 1 peak and 1 valley per impulse) 
         * @param num_of_samples
         * @param t
         * @return
         */
        public static double getModulatingWaveform(int num_of_samples, int t){
            double mod_wav = Math.sin(Math.PI*t/num_of_samples);
            return mod_wav;
        }
        
        /**
         * Returns the final impulse waveform which is
         * envelope * modulating waveform
         * @param num_of_samples
         * @param t
         * @return
         */
        public static double getImpulseWaveform(int num_of_samples, int t){
            double imp = getEnvelope(num_of_samples, t)*getModulatingWaveform(num_of_samples, t);
            return imp;
        }
    }

    /**
     * Factory for pre-initializing waveform data
     * @param num_of_samples
     * @return
     */
    public static PcmImpulse create(int num_of_samples) {
    	//number of samples should be even to simplify waveform calculations
        if (num_of_samples % 2 == 1) { //i.e. duration is odd
        	Log.w("echoapp PcmImpulse", "wave samples was odd, adding additional sample to make it even, original samples: " + num_of_samples);
            num_of_samples += 1;
        }
        
        //organize the waveform so that it's middle is indexed to 0
        int data_start = -(num_of_samples/2);
        int data_end = num_of_samples/2;
        int current_data = data_start;
        int i = 0;
        double[] impulse_data = new double[num_of_samples+1];
        
        //calculate the waveform for each data point
        while (current_data <= data_end){
           impulse_data[i] = PcmImpulse.Calc.getImpulseWaveform(num_of_samples, current_data);
           i++;
           current_data++;
        }

        //scale the data to match the maximum value for our audio data
        double max_impulse_value = PcmImpulse.Calc.getMaxValue(impulse_data);
        double scale_factor = MAX_ABS_VALUE / max_impulse_value; 

        int[] scaled_impulse_data = new int[impulse_data.length];
        for (i=0;i<impulse_data.length;i++){
            scaled_impulse_data[i] = (int) (scale_factor * impulse_data[i]);
        }
        
        return new PcmImpulse(scaled_impulse_data);
    }

   //initialization with the waveform samples
   private PcmImpulse(int[] impulse_data) {
       mSignal = impulse_data;
   }
   
   public int[] getSignal() {
	   return mSignal;
   }
   
   //TODO: Convert variables to proper case format
   public int[] filterMask() {
	   int[] filterMask = new int[mSignal.length];
	   int maxVal = Calc.getMaxValue(mSignal);
	   int minVal = Calc.getMinValue(mSignal);
	   
	   final int MATCH_HI = 1;
	   final int MATCH_LO = -1;
	   final int NO_MATCH = 0;
	   
	   //For better filter matching use a range for peak matching
	   final double PEAK_FUDGE = 0.98;
	   
	   //Match values that are within PEAK_FUDGE
	   for (int i=0;i<mSignal.length;i++) {

		   double approx_max_dbl = maxVal * PEAK_FUDGE;
		   int approx_max = (int) Math.round(approx_max_dbl);
		   double approx_min_dbl = minVal * PEAK_FUDGE;
		   int approx_min = (int) Math.round(approx_min_dbl);
		   
		   filterMask[i] = NO_MATCH;
		   
		   if (mSignal[i] >= approx_max) {
			   filterMask[i] = MATCH_HI;
		   } 
		   if (mSignal[i] <= approx_min) {
			   filterMask[i] = MATCH_LO;
		   }
	   }
	   return filterMask;
   }
}

