package org.younghawk.echoapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Panel holds the surface view for the graphics
 * The goal is for this class to focus on the surface
 * and its callbacks, and that the class PanelDrawer is
 * used for drawing to the view 
 */
public class Panel extends SurfaceView implements SurfaceHolder.Callback{
    public static final String TAG = "EchoApp Panel";
    
    private GlobalState gGlobal;
    
    //Reference to the main drawing class for this panel
    public PanelDrawer mPanelDrawer;
   
    //Reference to the current instance
    
    //TODO: Refactor Audio threads away from this
    public static boolean mStopRunningThreads = false; //deprecated
      
    
    //Used to hold the dimensions of the surface view.
    //Immutable since we shouldn't be messing with the view 
    //dimensions.
    public ImmutableRect mSurfaceRect = null;
    

    public Panel(Context context, AttributeSet attrs) {
		super(context, attrs); 
	    setupPanel();
	}

	public Panel(Context context) {
		super(context);
		setupPanel();
	}
	
	public void setupPanel(){
	    setFocusable(true);
	    Log.d(TAG, "Panel Created");
	    getHolder().addCallback(this);
	}
	
	
	
	
	
	

	//Our activity has notified us that the views are ready
	//So let's setup view dependent stuff
	public void viewsReady(){
	    Log.d(TAG, "Views Ready, create PanelDrawer");
	    //Now that the views are ready we can create a panelDrawer
	    //mPanelDrawer = PanelDrawer.create(this);
	}

	//Setup surface dependent stuff - called when surface is created or changed
	//TODO: Refactor away
	//public void onSurfaceReady(SurfaceHolder holder){
	//    Log.d(TAG, "Surface is now ready!");
	//    if (holder!=null && mSurfaceRect!=null){
	//        mPanelDrawer.onSurfaceReady();
	//    } else {
	//        throw new Error("Calling surface ready when surface is NOT ready");
	//   }   
	//}


	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	    mStopRunningThreads = false; //deprecated
	    //mSurfaceRect = new ImmutableRect(width, height);
	    gGlobal.setSurfaceRect( new ImmutableRect(width, height) );
	    Log.d(TAG, "CHANGED - Width: " + gGlobal.getSurfaceRect().width() + " - " + "Height: " + gGlobal.getSurfaceRect().height());
	    //onSurfaceReady(holder); //?
	  //Global Refactor
        if(gGlobal==null){
            gGlobal = GlobalState.getGlobalInstance();
        }
        gGlobal.onPanelReady(this);
 	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
	    if(gGlobal==null){
	        gGlobal = GlobalState.getGlobalInstance();
	    }
	    Log.d(TAG, "Surface Created!");
	    mStopRunningThreads = false; //deprecated
	    Canvas c = holder.lockCanvas(null);
	    try {
	        Rect dangerRect = holder.getSurfaceFrame();
	        //mSurfaceRect = new ImmutableRect(dangerRect.width(), dangerRect.height());
	        gGlobal.setSurfaceRect( new ImmutableRect(dangerRect.width(), dangerRect.height()) );
	    } finally {
	        holder.unlockCanvasAndPost(c);
	    }
	    Log.d(TAG, "CREATED - Width: " + gGlobal.getSurfaceRect().width() + " - " + "Height: " + gGlobal.getSurfaceRect().height());
	    //onSurfaceReady(holder); //mPnael.onSurfaceReady
	    
	  //Global Refactor
        if(gGlobal==null){
            gGlobal = GlobalState.getGlobalInstance();
        }
        gGlobal.onPanelReady(this);
	    
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	    
	    Log.d(TAG, "Surfaced destroyed, notifying panel drawer");
	    mPanelDrawer.onSurfaceDestroyed();
	  //invalidate any surface related data
	    mSurfaceRect = null;
	    
	}
	
	



}   