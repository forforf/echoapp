package org.younghawk.echoapp;

import java.util.concurrent.ConcurrentHashMap;

import org.younghawk.echoapp.handlerthreadfactory.HThread;
import org.younghawk.echoapp.handlerthreadfactory.HandlerThreadExecutor;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;

//TODO: Rename to PanelDraw?
public class PanelDrawer {
    private static final String TAG = "EchoApp PanelDrawer";
  //This class should be a singleton
    private static PanelDrawer instance = null;
    
    private Panel mPanel;
    
    public SurfaceHolder mSurfaceHolder;
    public ImmutableRect mSurfaceRect;
    
    public ConcurrentHashMap<DrawRegionNames, DrawRegionType> mDrawRegionAreas = new ConcurrentHashMap<DrawRegionNames, DrawRegionType>();
    public ConcurrentHashMap<DrawRegionNames, Handler> mDrawRegionHandlers = new ConcurrentHashMap<DrawRegionNames, Handler>();
    
    private static HandlerThreadExecutor mExecutor = new HandlerThreadExecutor();
    
    //TODO: Is there better way to handle scaling bitmaps??
    private Bitmap mOrigBitmap;
    private Bitmap mScaledBitmap;
    
    //private Executor executor = Executors.newFixedThreadPool(2);
    //private Paint paint = new Paint();

    //Panel Manager should only be created after Panel
    //is done initializing
    public static PanelDrawer create(Panel panel) {
        if(instance!=null){
            return instance;
        } else {

            //Setup Surface Holder
            SurfaceHolder panelSurfaceHolder = panel.getHolder();
            
            //TODO: Where best to get region data?
            //mDrawRegionDefinitions.put()

            //Setup the callbacks on panel
            panel.getHolder().addCallback(panel);
            
            

            instance = new PanelDrawer(panel, panelSurfaceHolder);
            //return new PanelDrawer(panelSurfaceHolder);
            return instance;
        }
    }
    private PanelDrawer(Panel panel, SurfaceHolder holder) {
        Log.d(TAG, "Constructing PanelDrawer");
        this.mPanel = panel;
        this.mSurfaceHolder = holder;
    }
    
    public void onSurfaceReady() {
        Log.d(TAG, "Surface Ready tasks");
        this.mSurfaceRect = mPanel.mSurfaceRect;
        this.mDrawRegionAreas.put(DrawRegionNames.RADAR, DrawRegionFactory.radarRegion(mSurfaceRect));
        this.mDrawRegionAreas.put(DrawRegionNames.GRAPH, DrawRegionFactory.graphRegion(mSurfaceRect));
        HThread t1 = mExecutor.execute(null);
        HThread t2 = mExecutor.execute(null);
        if(t1.handler!=null){
            this.mDrawRegionHandlers.put(DrawRegionNames.RADAR, t1.handler);
        } else {
            Log.e(TAG, "Unable to create radar handler");
            throw new Error(TAG + "Unable to create radar handler");
        }
        if(t2.handler!=null){
            this.mDrawRegionHandlers.put(DrawRegionNames.GRAPH, t2.handler);
        } else {
            Log.e(TAG, "Unable to create graph handler");
            throw new Error(TAG + "Unable to create graph handler");
        }
        
        mDrawRegionHandlers.get(DrawRegionNames.RADAR).post(
                new Runnable(){
                    @Override
                    public void run() {
                        Log.d(TAG, "Running radar draw in looping thread");
                        int location_iter = 0;
                        int steps = 2;
                        boolean go_right = true;

                        DrawRegionRadar radarData = (DrawRegionRadar) mDrawRegionAreas.get(DrawRegionNames.RADAR);
                        Rect dirty_rect = radarData.rect;
                        float r = radarData.blip_radius;
                        Paint paint = new Paint();

                        while(!Thread.currentThread().isInterrupted()){

                            SurfaceHolder holder = mSurfaceHolder;

                            //Rect dirty_rect = new Rect(0,top_pad, track_width ,top_pad + track_height);
                            Canvas c = holder.lockCanvas(dirty_rect);
                            //TODO: See if there's a way to factor this out from the thread
                            try {
                                if (c!=null) {
                                    synchronized (holder) {
                                        paint.setColor(Color.GRAY);
                                        c.drawRect(dirty_rect, paint);

                                        paint.setColor(Color.RED);
                                        c.drawCircle(location_iter, dirty_rect.top + r ,  r, paint);
                                        if(go_right){
                                            location_iter+=steps;
                                        } else {
                                            location_iter-=steps;
                                        }
                                        if (location_iter>dirty_rect.width()){
                                            go_right = false;
                                        }
                                        if (location_iter<0){
                                            go_right = true;
                                        }
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
                                if (!Thread.currentThread().isInterrupted()) {
                                    Thread.currentThread().interrupt();
                                }
                            }
                        }

                    };
                });

        
    }
    
    public void testTestBitmapDraw(){
        mExecutor.execute(new Runnable(){
            int[] myColors = new int[]{
                    Color.BLUE,
                    Color.CYAN,
                    Color.GREEN,
                    Color.YELLOW,
                    Color.RED,
                    Color.LTGRAY
            };
            Bitmap bitmap = Bitmap.createBitmap(100,100, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bitmap);
            @Override
            public void run(){
                for(int i=0;i<1000;i++){
                    for(int color: myColors){
                        c.drawColor(color);
                        onBitmapUpdate(bitmap);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            Log.e(TAG, "Test thread interupted: " + e);
                        }
                    }
                }
            }
        });
        
        
    }

    private Bitmap scaleBitmap(Bitmap orig_bitmap, Rect scale_rect){
        Bitmap scaled_bitmap = orig_bitmap;
        if (scaled_bitmap.getWidth()!=scale_rect.width() && scaled_bitmap.getHeight()!= scale_rect.height()){
            scaled_bitmap = Bitmap.createScaledBitmap(orig_bitmap, scale_rect.width(), scale_rect.height(), false);
        }
        return scaled_bitmap;
    }
    
    public void onBitmapUpdate(Bitmap bitmap) {
        mOrigBitmap = bitmap;
        DrawRegionGraph graphData = (DrawRegionGraph) mDrawRegionAreas.get(DrawRegionNames.GRAPH);
        Rect dirty_rect = graphData.rect;
        mScaledBitmap = scaleBitmap(bitmap, dirty_rect);
        drawScaledBitmap();
    }
    
    private void drawScaledBitmap(){
        mDrawRegionHandlers.get(DrawRegionNames.GRAPH).post( new Runnable(){
            @Override
            public void run() {
                SurfaceHolder holder = mSurfaceHolder;
                DrawRegionGraph graphData = (DrawRegionGraph) mDrawRegionAreas.get(DrawRegionNames.GRAPH);
                Rect dirty_rect = graphData.rect;
                Log.d(TAG, "Drawing Current Audio Region");
                
                Canvas c = holder.lockCanvas(dirty_rect);
                //TODO: See if there's a way to factor this out from the thread
                try {
                    if (c!=null) {
                        synchronized (holder) {
                            //paint.setColor(Color.GRAY);
                            //c.drawRect(dirty_rect, paint);
                            c.drawBitmap(mScaledBitmap, dirty_rect.left, dirty_rect.top, null);
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
            };
        });
    }

    private void defaultBitmap(){
        //BitmapConfig conf = Bitmap.Config.ARGB_8888; // see other conf types
        //Bitmap bmp = Bitmap.createBitmap(w, h, conf); // this creates a MUTABLE bitmap
        //Canvas temp = new Canvas(bmp);
    }
    
}
