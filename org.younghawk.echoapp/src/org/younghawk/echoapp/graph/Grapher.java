package org.younghawk.echoapp.graph;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Grapher {
	public static Grapher create(Canvas canvas) {
		Paint paint = new Paint();
		paint.setColor(Color.BLUE);
		canvas.drawCircle(100,50,25,paint);
		return new Grapher(canvas);
	}
	
	public Canvas mCanvas;
	
	private Grapher(Canvas canvas) {
	    this.mCanvas = canvas;	
	}

}
