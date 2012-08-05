package org.younghawk.echoapp;

import java.util.ArrayList;

import org.younghawk.echoapp.handlerthreadfactory.HandlerThreadExecutor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class Panel extends SurfaceView implements SurfaceHolder.Callback{
    public static final String TAG = "EchoApp Panel";
    public PanelDrawer mPanelDrawer;
    
    //public static ExecutorService mExecutor = Executors.newFixedThreadPool(4); //deprecated
    public static boolean mStopRunningThreads = false; //deprecated
    
    //TODO: Each BitmapProxy gets a handler.
    
    public static Handler mSvHandler = new HandlerThreadExecutor().execute(null).handler;  //deprecated
    
    
    
    public ImmutableRect mSurfaceRect = null;
    
    private ArrayList<Runnable> mDrawList = new ArrayList<Runnable>();
    
    //Region for drawing audio data
    public static BitmapProxy mAudioDataRegion;
    
	private CanvasThread canvasthread = null;  //deprecating with new thread mgt
	private Paint paint = new Paint(); //deprecating moving to bitmap client
	private CollectionGrapher debugArray;  //deprecating moving to bitmap client
	public Rect dirty_rect;  //deprecating moving to bitmap clientA
	
	private Runnable mInitDraw = new Runnable(){
	    @Override
	    public void run() {
	        Log.d(TAG, "Running initial draw in thread");
	        SurfaceHolder holder = getHolder();
	        Canvas c = holder.lockCanvas(null);
	        //TODO: See if there's a way to factor this out from the thread
	        try {
	            if (c!=null) {
	                synchronized (holder) {
	                    c.drawColor(Color.BLACK);
	                    paint.setColor(Color.CYAN);
	                    c.drawText("Initial Full Painter",100,400, paint);
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
	};
	
	/*
	private Runnable mRadarDraw = new Runnable(){
	    @Override
	    public void run() {
	        Log.d(TAG, "Running radar draw in looping thread");
	        int location_iter = 0;
	        int steps = 2;
            int track_width = mSurfaceRect.width();
            int r = 5; //circle radius
            int track_height = 2 * r;
            int top_pad = 5; //padding from the top
            boolean go_right = true;
            
	        while(!mStopRunningThreads && !Thread.currentThread().isInterrupted()){
	            //Log.d(TAG, "" + Thread.currentThread().isInterrupted());
	            SurfaceHolder holder = getHolder();

	            Rect dirty_rect = new Rect(0,top_pad, track_width ,top_pad + track_height);
	            Canvas c = holder.lockCanvas(dirty_rect);
	            //TODO: See if there's a way to factor this out from the thread
	            try {
	                if (c!=null) {
	                    synchronized (holder) {
	                        paint.setColor(Color.GRAY);
	                        c.drawRect(dirty_rect, paint);

	                        paint.setColor(Color.RED);
	                        c.drawCircle(location_iter, top_pad + r ,  r, paint);
	                        if(go_right){
	                            location_iter+=steps;
	                        } else {
	                            location_iter-=steps;
	                        }
	                        if (location_iter>track_width){
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
	                    mStopRunningThreads = true;
	                }
	            }
	        }
	        
	    };
	};
	*/
	
	private Runnable mAudioDataRegionDraw = new Runnable(){
	    @Override
	    public void run() {
	        Log.d(TAG, "Drawing Current Audio Region");
	        SurfaceHolder holder = getHolder();

	        Rect dirty_rect = mAudioDataRegion.getDirtyRect();
	        Canvas c = holder.lockCanvas(dirty_rect);
	        //TODO: See if there's a way to factor this out from the thread
	        try {
	            if (c!=null) {
	                synchronized (holder) {
	                    //paint.setColor(Color.GRAY);
	                    //c.drawRect(dirty_rect, paint);
	                    c.drawBitmap(mAudioDataRegion.consumeBitmap(), dirty_rect.left, dirty_rect.top, null);
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
	};
	
	

	
    public Panel(Context context, AttributeSet attrs) {
		super(context, attrs); 
	    //getHolder().addCallback(this);  //moving to PanelDrawer
	    //canvasthread = new CanvasThread(getHolder(), this);  //deprecating using bitmap client to post runnable
	    setFocusable(true);
	    //Send reference back to Main Activity
	    EchoApp echoApp = (EchoApp) context;  //deprecating use PanelDrawer (bitmap container)
	    echoApp.setPanel(this); //deprecatgin use PanelDrawer
	}

	public Panel(Context context) {
		 
		super(context);
		//sets Panel as the handler for surface events
		//getHolder().addCallback(this);  //moving to PanelDrawer
		//canvasthread = new CanvasThread(getHolder(), this); //deprecating use PanelDrawer
		
		setFocusable(true);
	}
	
	private void drawPaintersInList(ArrayList<Runnable> drawList){
	    for (Runnable r : drawList){
	        //mExecutor.execute(r);
	        mSvHandler.post(r);
	    }
	}
	
	//Setup view dependent stuff
	public void viewsReady(){
	    Log.d(TAG, "Views Ready, create PanelDrawer");
	    //Activity has created views
	    mPanelDrawer = PanelDrawer.create(this);
	}

	//Setup surface dependent stuff
	public void onSurfaceReady(SurfaceHolder holder){
	    Log.d(TAG, "Surface is now ready!");
	    if (holder!=null && mSurfaceRect!=null){
	        mPanelDrawer.onSurfaceReady();
	    } else {
	        throw new Error("Calling surface ready when surface is NOT ready");
	    }
	    mPanelDrawer.testTestBitmapDraw();
	    
	}


	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	    mStopRunningThreads = false;
	    mSurfaceRect = new ImmutableRect(width, height);
	    Log.d(TAG, "CHANGED - Width: " + mSurfaceRect.width() + " - " + "Height: " + mSurfaceRect.height());
	    //onSurfaceReady(holder); ?
 	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
	    mStopRunningThreads = false;
	    Canvas c = holder.lockCanvas(null);
	    try {
	        Rect dangerRect = holder.getSurfaceFrame();
	        mSurfaceRect = new ImmutableRect(dangerRect.width(), dangerRect.height());
	    } finally {
	        holder.unlockCanvasAndPost(c);
	    }
	    Log.d(TAG, "CREATED - Width: " + mSurfaceRect.width() + " - " + "Height: " + mSurfaceRect.height());
	    onSurfaceReady(holder);
	    
	   //deprecated 
	  //Setting the drawable regions
	  //Audio Region Setup
	    /*
	  int side_padding = 8;
	  int top_padding =100;
	  int bot_padding = 20;
	  int ar_width = mSurfaceRect.width() - (side_padding*2);
	  int ar_height = mSurfaceRect.height() - top_padding - bot_padding;
      Bitmap ar_bmp = Bitmap.createBitmap(ar_width, ar_height, Bitmap.Config.ARGB_8888 );
      Canvas ar_c = new Canvas(ar_bmp);
      ar_c.drawColor(Color.DKGRAY);
      Rect ar_rect = new Rect(side_padding, top_padding, side_padding + ar_width, top_padding + ar_height);
      mAudioDataRegion = new BitmapProxy(ar_bmp, ar_rect);
    
	    //add it to the list    
	    mDrawList.add(mInitDraw);
	    mDrawList.add(mRadarDraw);
	    mDrawList.add(mAudioDataRegionDraw);
	    drawPaintersInList(mDrawList);
	    
	    //clear the list
	    mDrawList.clear();
        
	    
        
		//Workaround for when the thread is trashed by android (by pressing home, etc)
		//if (canvasthread.getState()==Thread.State.TERMINATED) { 
        //    canvasthread = new CanvasThread(getHolder(), this);
       //}
	    //canvasthread.setRunning(true);
	    //canvasthread.start();
*/
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	    mSurfaceRect = null;
	    Log.d(TAG, "Surfaced destroyed, shutting down threads");
	    mStopRunningThreads = true;
	    //TODO: INvestigate using #submit and cancelling the future.
	    
		//boolean retry = true;
		//canvasthread.setRunning(false);
		//keep waiting for the thread to close - Possible cause for long FCs
		//while (retry) {
		//	try {
		//		canvasthread.join();
		//		retry = false;
		//	} catch (InterruptedException e) {
		//		// we will try it again and again...
		//	}
		//}
	}
	



}   