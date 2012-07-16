package org.younghawk.echoapp.signals;

import android.util.Log;

/**
 * Container for an impulse signal suitable for audio (pcm)
 */
public class PcmImpulse implements SignalType {
	
    //Maximum value the impulse will take
    private static final int MAX_ABS_VALUE = 32767;
    
    //Integer Array of Signal Data
    private int[] mSignal;

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

        /**
         * Returns the denominator of the exponent of the impulse wave function 
         * @param wave_samples
         * @return
         */
        public static double getExponentDenominator(int wave_samples){
            double d8 = (double) wave_samples/8;
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
         * @param wave_samples
         * @param t
         * @return
         */
        public static double getEnvelopeExponent(int wave_samples, int t){
            double num = Calc.getExponentNumerator(t);
            double den = Calc.getExponentDenominator(wave_samples);
            double env_exp = num / den;
            return env_exp;
        }

        /**
         * Returns the envelope of the impulse.
         * This factor is what gives the impulse a "spike"
         * @param wave_samples
         * @param t
         * @return
         */
        public static double getEnvelope(int wave_samples, int t){
            double env_exp = Calc.getEnvelopeExponent(wave_samples, t);
            double env = 2*wave_samples*Math.exp(env_exp);
            return env;
        }

        /**
         * Returns the modulated wave for the impulse.
         * This factor determines the primary frequency of the impulse
         * and how may amplitude peaks it contains within the envelope
         * (Typically set for 1 peak and 1 valley per impulse) 
         * @param wave_samples
         * @param t
         * @return
         */
        public static double getModulatingWaveform(int wave_samples, int t){
            double mod_wav = Math.sin(Math.PI*t/wave_samples);
            return mod_wav;
        }
        
        /**
         * Returns the final impulse waveform which is
         * envelope * modulating waveform
         * @param wave_samples
         * @param t
         * @return
         */
        public static double getImpulseWaveform(int wave_samples, int t){
            double imp = getEnvelope(wave_samples, t)*getModulatingWaveform(wave_samples, t);
            return imp;
        }
    }

    /**
     * Factory for pre-initializing waveform data
     * @param wave_samples
     * @return
     */
    public static PcmImpulse create(int wave_samples) {
        if (wave_samples % 2 == 1) { //i.e. duration is odd
        	Log.w("echoapp PcmImpulse", "wave samples was odd, adding additional sample to make it even, original samples: " + wave_samples);
            wave_samples += 1;
        }
        int dataStart = -(wave_samples/2);
        int dataEnd = wave_samples/2;
        int currentData = dataStart;
        int i = 0;
        double[] impulseData = new double[wave_samples+1];
            
        while (currentData <= dataEnd){
           impulseData[i] = PcmImpulse.Calc.getImpulseWaveform(wave_samples, currentData);
           i++;
           currentData++;
        }

        double maxImpulseVal = PcmImpulse.Calc.getMaxValue(impulseData);
        double scaleFactor = MAX_ABS_VALUE / maxImpulseVal; 

        int[] scaledImpulseData = new int[impulseData.length];
        for (i=0;i<impulseData.length;i++){
            scaledImpulseData[i] = (int) (scaleFactor * impulseData[i]);
        }
        
        return new PcmImpulse(scaledImpulseData);
    }

   //initialization
   private PcmImpulse(int[] impulseData) {
       mSignal = impulseData;
   }
   
   public int[] getSignal() {
	   return mSignal;
   }
}

