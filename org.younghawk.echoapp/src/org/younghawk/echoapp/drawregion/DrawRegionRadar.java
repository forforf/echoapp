package org.younghawk.echoapp.drawregion;

import org.younghawk.echoapp.PanelDrawer;
import org.younghawk.echoapp.handlerthreadfactory.HThread;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;

/**
 * This class is instantiated through the DrawRegionFactory.
 */
public class DrawRegionRadar implements DrawRegionType {
    public static final String TAG="EchoApp DrawRegionRadar";
    public class LocalData{
        public int location_iter = 0;
        public int steps = 2;
        public boolean go_right = true;
        public Paint paint = new Paint();
    }

    public Rect rect; //the rectangle that bounds the object
    public float blip_radius;
    public final ThreadLocal<LocalData> local_iter_data = new ThreadLocal();
    private PanelDrawer mPanelDrawer;

    public DrawRegionRadar(PanelDrawer panel_drawer, Rect rect, float blip_radius){
        this.rect = rect;
        this.blip_radius = blip_radius;
        this.mPanelDrawer = panel_drawer;
    }

    public Rect getRect(){
        return rect;
    }
    
    @Override
    public void run(SurfaceHolder holder){
        Log.d(TAG, "Running radar draw in looping thread");
        
        //DrawRegionRadar radarData = (DrawRegionRadar) mDrawRegionAreas.get(DrawRegionNames.RADAR);
        //int location_iter = 0;
        //int steps = 2;
        //boolean go_right = true;
        DrawRegionRadar.LocalData default_iter_data = new LocalData();
        
        //Put the data into a threadlocal
        local_iter_data.set(default_iter_data);
        
        //Rect dirty_rect = radarData.rect;
        //float r = radarData.blip_radius;
        //Paint paint = new Paint();
        HThread thisThread = (HThread) Thread.currentThread();
        while(thisThread.running && !thisThread.isInterrupted()){

            //SurfaceHolder holder = mSurfaceHolder;

            //Rect dirty_rect = new Rect(0,top_pad, track_width ,top_pad + track_height);
            Canvas c = holder.lockCanvas(rect);
            //TODO: See if there's a way to factor this out from the thread
            try {
                if (c!=null) {
                    synchronized (holder) {
                        drawOnSurface(c);

                    }
                } //TODO: capture data on why canvas would be null

            } finally {
                // do this in a finally so that if an exception is thrown
                // during the above, we don't leave the Surface in an
                // inconsistent state
                if (c != null) {
                    holder.unlockCanvasAndPost(c);
                }
            }
            try {
                Thread.sleep(40);
            } catch (InterruptedException e) {
                Log.d(TAG, "Thread was interupted, it should be caught on the next tick");
                if (!thisThread.isInterrupted()) {
                    thisThread.running = false;
                    thisThread.interrupt();
                }
            }
        }
    }
    
    private void drawOnSurface(Canvas c){
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
