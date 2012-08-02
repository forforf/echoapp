package org.younghawk.echoapp;

public class ImmutableRect {
    private int width;
    private int height;
    
    public ImmutableRect(int w, int h){
        this.width  = w;
        this.height = h;
    }

    public int width(){
        return width;
    }
    
    public int height(){
        return height;
    }
}
