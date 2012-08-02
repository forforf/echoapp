package org.younghawk.echoapp;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class Panel extends SurfaceView implements SurfaceHolder.Callback{
    public static final String TAG = "EchoApp Panel";
    private Executor mExecutor = Executors.newFixedThreadPool(4);
    private ImmutableRect mSurfaceRect = null;
    
    private ArrayList<Runnable> mDrawList = new ArrayList<Runnable>();
    
	private CanvasThread canvasthread = null;  //deprecating with new thread mgt
	private int tester = 0;  //deprecating moving to bitmap client
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
	
	private Runnable mRadarDraw = new Runnable(){
	    @Override
	    public void run() {
	        Log.d(TAG, "Running radar draw in looping thread");
	        int tester = 0;
	        while(!Thread.currentThread().isInterrupted()){
	            SurfaceHolder holder = getHolder();
	            Rect dirty_rect = new Rect(15,45,380,55);
	            Canvas c = holder.lockCanvas(dirty_rect);
	            //TODO: See if there's a way to factor this out from the thread
	            try {
	                if (c!=null) {
	                    synchronized (holder) {
	                        paint.setColor(Color.GRAY);
	                        c.drawRect(dirty_rect, paint);
	                        
	                        paint.setColor(Color.RED);
	                        c.drawCircle(20+tester,  50,  5, paint);
	                        tester++;
	                        if (tester>350){
	                            tester=0;
	                        }                }
	                } //TODO: capture data on why canvas would be null

	            } finally {
	                // do this in a finally so that if an exception is thrown
	                // during the above, we don't leave the Surface in an
	                // inconsistent state
	                if (c != null) {
	                    holder.unlockCanvasAndPost(c);
	                }
	            }
	        }
	        try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                Log.d(TAG, "Thread was interupted, it should be caught on the next tick");
            }
	    };
	};
	
	
	
	
	//Container for graph
	//public int[] mRawGraphData = null;
	
	//Data to Plot
	//Plotter plotter = Plotter.create();
	
    public Panel(Context context, AttributeSet attrs) {
		super(context, attrs); 
	    //getHolder().addCallback(this);  //moving to PanelManager
	    //canvasthread = new CanvasThread(getHolder(), this);  //deprecating using bitmap client to post runnable
	    setFocusable(true);
	    //Send reference back to Main Activity
	    EchoApp echoApp = (EchoApp) context;  //deprecating use PanelManager (bitmap container)
	    echoApp.setPanel(this); //deprecatgin use PanelManager
	}

	public Panel(Context context) {
		 
		super(context);
		//sets Panel as the handler for surface events
		//getHolder().addCallback(this);  //moving to PanelManager
		//canvasthread = new CanvasThread(getHolder(), this); //deprecating use PanelManager
		
		setFocusable(true);
	}
	
	private void drawPaintersInList(ArrayList<Runnable> drawList){
	    for (Runnable r : drawList){
	        mExecutor.execute(r);
	    }
	}
	

/*	
	@Override
	public void onDraw(Canvas canvas) {

	    //if(DebugData.currentDebugData!=null){
	    //    paint.setColor(Color.CYAN);
	    //    canvas.drawPoints(DebugData.currentDebugData, paint);
	    //}    
	    //if(Plotter.plotReady) {
	    //    paint.setColor(Color.GREEN);
	    //    float[] audio_points = Plotter.toCanvasPointsArray(Plotter.PlotQ, 60, 400);
	    //    canvas.drawPoints(audio_points, paint);
	    //}
	    
	    //if (plotter!=null){
	    //    paint.setColor(Color.GREEN);
	    //    canvas.drawLines(plotter.getPlotData(), paint);
	    //}
		//canvas.drawBitmap(kangoo, 130, 10, null);

        paint.setColor(Color.GRAY);
        canvas.drawRect(15,45,380,55, paint);
        if (dirty_rect==null){
            dirty_rect  = new Rect(15,45,380,55);
        }
        
		paint.setColor(Color.RED);
		canvas.drawCircle(20+tester,  50,  5, paint);
		tester++;
		if (tester>350){
			tester=0;
		}
		
	}
*/

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	    mSurfaceRect = new ImmutableRect(width, height);
	    Log.d(TAG, "CREATED - Width: " + mSurfaceRect.width() + " - " + "Height: " + mSurfaceRect.height());
		
		
	}
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
	    Canvas c = holder.lockCanvas(null);
	    try {
	        Rect dangerRect = holder.getSurfaceFrame();
	        mSurfaceRect = new ImmutableRect(dangerRect.width(), dangerRect.height());
	    } finally {
	        holder.unlockCanvasAndPost(c);
	    }
	    Log.d(TAG, "CREATED - Width: " + mSurfaceRect.width() + " - " + "Height: " + mSurfaceRect.height());
	    
	    //Define what to draw
	    //FullPainter init_draw = new FullPainter(holder){
	    //        @Override
	    //        public void safeDraw(Canvas c, Paint paint){
	    //            c.drawColor(Color.BLACK);
	    //            paint.setColor(Color.CYAN);
	    //            c.drawText("Initial Full Painter",100,400, paint);
	    //        }
	    //    };
	        
	     //Try with radar   (first full screen, then try dirty)
    
	    //add it to the list    
	    
	    mDrawList.add(mInitDraw);
	    mDrawList.add(mRadarDraw);
	    drawPaintersInList(mDrawList);
	    
	    //clear the list
	    mDrawList.clear();
        
		//Workaround for when the thread is trashed by android (by pressing home, etc)
		//if (canvasthread.getState()==Thread.State.TERMINATED) { 
        //    canvasthread = new CanvasThread(getHolder(), this);
       //}
	    //canvasthread.setRunning(true);
	    //canvasthread.start();

	}
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	    mSurfaceRect = null;
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