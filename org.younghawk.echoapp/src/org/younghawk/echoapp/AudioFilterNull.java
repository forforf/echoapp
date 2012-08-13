package org.younghawk.echoapp;

import android.util.Log;

public class AudioFilterNull implements AudioFilterStub {
    private int[] int_data;
    private int buffer_size;
    private int prev_size;
    
    
    
    @Override
    public int[] filter(short[] buffer_data) {
        //A non-null would probably call a method on a specific filter class
        
        //If the buffer size is the same, we reuse the array
        buffer_size = buffer_data.length;
        if(buffer_size==prev_size){
            for(int i=0;i<buffer_size;i++){
                int_data[i] = (int) buffer_data[i];
            }
        } else {
            //we have to create a new one
            buffer_size = buffer_data.length;
            int_data = new int[buffer_size];
            for(int i=0;i<buffer_size;i++){
                int_data[i] = (int) buffer_data[i];
            }
        }
        prev_size = buffer_size;
        
        return int_data;
    }

}
