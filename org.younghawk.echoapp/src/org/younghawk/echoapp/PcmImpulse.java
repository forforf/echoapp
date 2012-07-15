package org.younghawk.echoapp;

public class PcmImpulse implements SignalType {
    //Maximum value the impulse will take
    public static final int Max = 32767;
    
    //Integer Array of Signal Data
    public int[] signal;

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

        public static double getExponentDenominator(int duration){
            double d8 = (double) duration/8;
            double d8sq = d8*d8;
            double expDenom = d8sq*Math.E;
            return expDenom;
        }

        public static double getExponentNumerator(int t){
            double expNum = -( (float) t*t );
            return expNum;
        }

        public static double getEnvelopeExponent(int duration, int t){
            double num = Calc.getExponentNumerator(t);
            double den = Calc.getExponentDenominator(duration);
            double envExp = num / den;
            return envExp;
        }

        public static double getEnvelope(int duration, int t){
            double envExp = Calc.getEnvelopeExponent(duration, t);
            double env = 2*duration*Math.exp(envExp);
            return env;
        }

        public static double getModulatingWaveform(int duration, int t){
            double modWav = Math.sin(Math.PI*t/duration);
            return modWav;
        }

        public static double getImpulseWaveform(int duration, int t){
            double imp = getEnvelope(duration, t)*getModulatingWaveform(duration, t);
            return imp;
        }
    }

    //factory method
    public static PcmImpulse create(int duration) {
        //ToDo: Log a warning that the odd duration was incremented to be even
        if (duration % 2 == 1) { //i.e. duration is odd
            duration += 1;
        }
        int dataStart = -(duration/2);
        int dataEnd = duration/2;
        int currentData = dataStart;
        int i = 0;
        double[] impulseData = new double[duration+1];
            
        while (currentData <= dataEnd){
           impulseData[i] = PcmImpulse.Calc.getImpulseWaveform(duration, currentData);
           i++;
           currentData++;
        }

        double maxImpulseVal = PcmImpulse.Calc.getMaxValue(impulseData);
        double scaleFactor = Max / maxImpulseVal; 

        int[] scaledImpulseData = new int[impulseData.length];
        for (i=0;i<impulseData.length;i++){
            scaledImpulseData[i] = (int) (scaleFactor * impulseData[i]);
        }
        
        //int scaledImpulseData = (int) PcmImpulse
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

