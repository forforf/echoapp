package org.younghawk.echoapp;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;
//import org.younghawk.echoapp.R;

public class CanvasThread extends Thread {
    private SurfaceHolder _surfaceHolder;
    private Panel _panel;
    private boolean _run = false;

    public CanvasThread(SurfaceHolder surfaceHolder, Panel panel) {
        _surfaceHolder = surfaceHolder;
        _panel = panel;
    }

    //Used to flag the thread whether it should be running or not
    public void setRunning(boolean run) {
        _run = run;
    }

    @Override
    public void run() {
        Canvas c;
        while (_run) {
            c = null;
            try {
            	Thread.sleep(20);
            	Rect dirty_rect = null;
            	if (_panel!=null) {
            	    dirty_rect = _panel.dirty_rect;
            	}
                c = _surfaceHolder.lockCanvas(dirty_rect);
                
                //This is the point in the thread that we call the onDraw method in our Panel class
                if (c!=null) {
	                synchronized (_surfaceHolder) {
	                	//Log.v("EchoApp", "" + _panel);
	                	if(_panel!=null){
	                       // _panel.onDraw(c);
	                	} else {
	                		Log.e("EchoApp", "Panel unexpectedly went null");
	                	}
	                }
                } //TODO: capture data on why canvas would be null
            } catch (InterruptedException e) {
            	_run = false;
            	Log.w("EchoApp", "Thread interupted");
            } finally {
                // do this in a finally so that if an exception is thrown
                // during the above, we don't leave the Surface in an
                // inconsistent state
                if (c != null) {
                    _surfaceHolder.unlockCanvasAndPost(c);
                }
            }
        }
    }
}

