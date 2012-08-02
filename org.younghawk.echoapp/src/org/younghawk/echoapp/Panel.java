package org.younghawk.echoapp;

import org.younghawk.echoapp.graph.Grapher;
import org.younghawk.echoapp.signals.PcmImpulse;

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
	private CanvasThread canvasthread = null;  //deprecating with new thread mgt
	private int tester = 0;  //deprecating moving to bitmap client
	private Paint paint = new Paint(); //deprecating moving to bitmap client
	private CollectionGrapher debugArray;  //deprecating moving to bitmap client
	public Rect dirty_rect;  //deprecating moving to bitmap client
	
	//Container for graph
	//public int[] mRawGraphData = null;
	
	//Data to Plot
	//Plotter plotter = Plotter.create();
	
    public Panel(Context context, AttributeSet attrs) {
		super(context, attrs); 
	    //getHolder().addCallback(this);  //moving to PanelManager
	    canvasthread = new CanvasThread(getHolder(), this);  //deprecating using bitmap client to post runnable
	    setFocusable(true);
	    //Send reference back to Main Activity
	    EchoApp echoApp = (EchoApp) context;  //deprecating use PanelManager (bitmap container)
	    echoApp.setPanel(this); //deprecatgin use PanelManager
	}

	public Panel(Context context) {
		 
		super(context);
		//sets Panel as the handler for surface events
		//getHolder().addCallback(this);  //moving to PanelManager
		canvasthread = new CanvasThread(getHolder(), this); //deprecating use PanelManager
		
		setFocusable(true);
	}
	
	
	/*
	private float[] scaleData(int[] raw_data, int width, int height, int xoffset, int yoffset) {
		float[] scaleData = new float[raw_data.length * 2];
		int raw_data_max = PcmImpulse.Calc.getMaxValue(raw_data);
		int raw_data_min = PcmImpulse.Calc.getMinValue(raw_data);
		float scaledX = raw_data.length / (float) width;
		float scaledY = (raw_data_max - raw_data_min)/(float) height;
		for(int i=0;i<raw_data.length;i++) {
			scaleData[i*2] = (i / scaledX) + (float) xoffset;
			scaleData[i*2 + 1] = (raw_data[i]/scaledY) + (float) yoffset;
		}
		return scaleData;
	}
	*/
	
	@Override
	public void onDraw(Canvas canvas) {
	    /*
        if (mRawGraphData!=null) {
        	Log.v("EchoApp","If you see a lot of these, that's an issue");
        	canvas.drawColor(Color.BLACK);
        	paint.setColor(Color.GREEN);
        	//TODO Convert to calculated variable rather than on the fly method
        	//float[] scaledPoints = scaleData(mRawGraphData, 400, 400, 60, 260);
        	canvas.drawPoints(scaledPoints, paint);
            mRawGraphData = null;
        	//canvas.drawCircle(50, 50, 30, paint);
        } 
        */
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
	 
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		//Log.v("EchoApp", "SurfaceChanged");
		
	}
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		//Workaround for when the thread is trashed by android (by pressing home, etc)
		if (canvasthread.getState()==Thread.State.TERMINATED) { 
            canvasthread = new CanvasThread(getHolder(), this);
       }
	    canvasthread.setRunning(true);
	    canvasthread.start();

	}
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		canvasthread.setRunning(false);
		//keep waiting for the thread to close - Possible cause for long FCs
		while (retry) {
			try {
				canvasthread.join();
				retry = false;
			} catch (InterruptedException e) {
				// we will try it again and again...
			}
		}
	}
	
	public void onGraphData(Grapher graph_data){
		
	}


}   