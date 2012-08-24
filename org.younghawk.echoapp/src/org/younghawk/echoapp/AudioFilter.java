package org.younghawk.echoapp;

public interface AudioFilter {
    public enum Type {
        NULL, ECHO
    }
    
    int[] filter(short[] buffer_data);
    Type getType();
}
