package org.younghawk.echoapp;

public class AudioFilterProxy implements AudioFilter{
    private AudioFilter current_filter;
 
    private static AudioFilterProxy instance = AudioFilterProxy.create();
    
    //factory
    public static AudioFilterProxy create(){
        AudioFilter init_filter = new AudioFilterNull();
        return new AudioFilterProxy(init_filter);
    }
    
    public static AudioFilterProxy getInstance(){
        return instance;
    }
    
    //constructor
    private AudioFilterProxy(AudioFilter init_filter){
        this.current_filter = init_filter;
    }
    
    //change filter
    public void setFilter(AudioFilter filter){
        synchronized (current_filter){
            current_filter = filter;
        }
    }

    @Override
    public int[] filter(short[] buffer_data) {
        return current_filter.filter(buffer_data);
    }

    @Override
    public AudioFilter.Type getType() {
        return current_filter.getType();
    }
    
    
}
