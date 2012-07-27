package org.younghawk.echoapp;

public class SignalStats {
    public int max;
    public int min;
    public int sum;
    public double mean;
    public double std_dev;
    
    public static SignalStats create(int[] signal){
        int size = signal.length;
        int _max = signal[0];
        int _min = signal[0];
        int _sum = 0;
        double _mean;
        double _std_dev;
        
        for(int i=0;i<size;i++) {
            _sum += signal[i];
            if (signal[i] > _max) {
                _max = signal[i];
            }
            if (signal[i] < _min) {
                _min = signal[i];
            }
        }
        
        _mean = _sum / (double)size;
        _std_dev = Stat.calcStanDev(size, signal);
    
        return new SignalStats(_max, _min, _sum, _mean, _std_dev);
    }
    
    private SignalStats(int _max, int _min, int _sum, double _mean, double _std_dev) {
        max = _max;
        min = _min;
        sum = _sum;
        mean = _mean;
        std_dev = _std_dev;
    }
    
    
    public String toString() {
        return "Max: " + max + "  Min: " + min + "  Sum: " + sum + " Mean: " + mean + "Std Dev: " + std_dev;
    }
    
}
