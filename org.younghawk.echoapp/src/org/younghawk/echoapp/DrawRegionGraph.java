package org.younghawk.echoapp;

import android.graphics.Rect;

public class DrawRegionGraph implements DrawRegionType {

    public Rect rect; //the rectangle that bounds the object

    public DrawRegionGraph(Rect rect){
        this.rect = rect;
    }

    public Rect getRect(){
        return rect;
    }

}