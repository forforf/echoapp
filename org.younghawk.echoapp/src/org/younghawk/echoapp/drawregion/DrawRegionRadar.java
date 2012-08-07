package org.younghawk.echoapp.drawregion;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * This class is instantiated through the DrawRegionFactory.
 */
public class DrawRegionRadar implements DrawRegionType {
    public class LocalData{
        public int location_iter = 0;
        public int steps = 2;
        public boolean go_right = true;
        public Paint paint = new Paint();
    }

    public Rect rect; //the rectangle that bounds the object
    public float blip_radius;
    public final ThreadLocal<LocalData> local_iter_data = new ThreadLocal();

    public DrawRegionRadar(Rect rect, float blip_radius){
        this.rect = rect;
        this.blip_radius = blip_radius; 
    }

    public Rect getRect(){
        return rect;
    }
    
    public void drawOnSurface(Canvas c){
        //threadlocal data accessible with thread
        LocalData local_data = this.local_iter_data.get();
        
        Paint paint = local_data.paint;
        paint.setColor(Color.GRAY);
        c.drawRect(rect, paint);

        paint.setColor(Color.RED);
        c.drawCircle(local_data.location_iter, rect.top + blip_radius ,  blip_radius, paint);
        if(local_data.go_right){
            local_data.location_iter+=local_data.steps;
        } else {
            local_data.location_iter-=local_data.steps;
        }
        if (local_data.location_iter>rect.width()){
            local_data.go_right = false;
        }
        if (local_data.location_iter<0){
            local_data.go_right = true;
        }
    }

}
