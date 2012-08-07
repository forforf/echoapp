package org.younghawk.echoapp;

public class AudioFilterProxy implements AudioFilterStub{
    private AudioFilterStub current_filter;
    
    private static AudioFilterProxy instance = AudioFilterProxy.create();
    
    //factory
    public static AudioFilterProxy create(){
        AudioFilterStub init_filter = new AudioFilterNull();
        return new AudioFilterProxy(init_filter);
    }
    
    public static AudioFilterProxy getInstance(){
        return instance;
    }
    
    //constructor
    private AudioFilterProxy(AudioFilterStub init_filter){
        this.current_filter = init_filter;
    }
    
    //change filter
    public void setFilter(AudioFilterStub filter){
        synchronized (current_filter){
            current_filter = filter;
        }
    }

    @Override
    public int[] filter(short[] buffer_data) {
        return current_filter.filter(buffer_data);
    }
    
    
}
