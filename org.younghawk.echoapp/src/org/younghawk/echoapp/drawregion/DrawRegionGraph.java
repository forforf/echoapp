package org.younghawk.echoapp.drawregion;

import android.graphics.Rect;

/**
 * This class is instantiated through the DrawRegionFactory.
 */
public class DrawRegionGraph implements DrawRegionType {

    public Rect rect; //the rectangle that bounds the object

    public DrawRegionGraph(Rect rect){
        this.rect = rect;
    }

    public Rect getRect(){
        return rect;
    }

}