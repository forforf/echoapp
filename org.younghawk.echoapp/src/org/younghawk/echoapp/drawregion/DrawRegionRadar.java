package org.younghawk.echoapp.drawregion;

import android.graphics.Canvas;
import android.graphics.Rect;

/**
 * This class is instantiated through the DrawRegionFactory.
 */
public class DrawRegionRadar implements DrawRegionType {

    public Rect rect; //the rectangle that bounds the object
    public float blip_radius;

    public DrawRegionRadar(Rect rect, float blip_radius){
        this.rect = rect;
        this.blip_radius = blip_radius; 
    }

    public Rect getRect(){
        return rect;
    }
    
    public void drawOnSurface(Canvas c){
        
    }

}
