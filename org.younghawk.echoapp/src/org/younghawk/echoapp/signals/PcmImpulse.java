package org.younghawk.echoapp.signals;

import android.util.Log;

public class PcmImpulse implements SignalType {
    //Maximum value the impulse will take
    private static final int Max = 32767;
    
    //Integer Array of Signal Data
    private int[] signal;

    public static class Calc{
        public static double getMaxValue(double[] data){
             double maxValue = data[0];
             for(double val: data){
                 if(val > maxValue){
                     maxValue = val;
                 }
             }
             return maxValue;
        }

        public static double getExponentDenominator(int wave_samples){
            double d8 = (double) wave_samples/8;
            double d8sq = d8*d8;
            double expDenom = d8sq*Math.E;
            return expDenom;
        }

        public static double getExponentNumerator(int t){
            double expNum = -( (float) t*t );
            return expNum;
        }

        public static double getEnvelopeExponent(int wave_samples, int t){
            double num = Calc.getExponentNumerator(t);
            double den = Calc.getExponentDenominator(wave_samples);
            double envExp = num / den;
            return envExp;
        }

        public static double getEnvelope(int wave_samples, int t){
            double envExp = Calc.getEnvelopeExponent(wave_samples, t);
            double env = 2*wave_samples*Math.exp(envExp);
            return env;
        }

        public static double getModulatingWaveform(int wave_samples, int t){
            double modWav = Math.sin(Math.PI*t/wave_samples);
            return modWav;
        }

        public static double getImpulseWaveform(int wave_samples, int t){
            double imp = getEnvelope(wave_samples, t)*getModulatingWaveform(wave_samples, t);
            return imp;
        }
    }

    //factory method
    public static PcmImpulse create(int wave_samples) {
        if (wave_samples % 2 == 1) { //i.e. duration is odd
        	Log.w("echoapp PcmImpulse", "wave samples was odd, adding additional sample to make it even, samples: " + wave_samples);
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
        double scaleFactor = Max / maxImpulseVal; 

        int[] scaledImpulseData = new int[impulseData.length];
        for (i=0;i<impulseData.length;i++){
            scaledImpulseData[i] = (int) (scaleFactor * impulseData[i]);
        }
        
        return new PcmImpulse(scaledImpulseData);
    }

   //initialization
   private PcmImpulse(int[] impulseData) {
       signal = impulseData;
   }
   
   public int[] getSignal() {
	   return signal;
   }
}

