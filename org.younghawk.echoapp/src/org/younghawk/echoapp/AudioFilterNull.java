package org.younghawk.echoapp;

public class AudioFilterNull implements AudioFilterStub {
    private int[] int_data;
    
    @Override
    public int[] filter(short[] buffer_data) {
        //A non-null would probably call a method on a specific filter class
        int buffer_size = buffer_data.length;
        int_data = new int[buffer_size];
        for(int i=0;i<buffer_size;i++){
            int_data[i] = (int) buffer_data[i];
        }
        
        return int_data;
    }

}
