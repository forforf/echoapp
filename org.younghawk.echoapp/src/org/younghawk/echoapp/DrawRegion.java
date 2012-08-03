package org.younghawk.echoapp;

import android.graphics.Bitmap;
import android.graphics.Rect;

/**
 * This class is a bit dangerous in that it allows direct access
 * to the writable bitmap, that part is not thread safe.
 * Users writing to the bitmap will have to implement their
 * own synchronization to keep things safe.
 * Once a user finishes updating the bitmap they can
 * notify the class that it has been updated and this
 * will set the copy the unsafe writable bitmap to a thread safe
 * readable bitmap and set the flag indicating the bitmap has
 * been updated.  Bitmaps should be ARGB_8888 format
 * @author Dave2
 *
 */
public class DrawRegion {
    private Bitmap mWritableBitmap;
    private Bitmap mReadBitmap;
    private Rect mDirtyRectangle;
    public boolean isConsumed;
    
    public DrawRegion(Bitmap bmp, Rect dirty) {
        this.mWritableBitmap = bmp;
        this.mReadBitmap = Bitmap.createBitmap(bmp);
        this.mDirtyRectangle = dirty;
        this.isConsumed = false;
    }
    
    public Bitmap getUnsafeBitmap(){
        return mWritableBitmap;
    }
    
    public Rect getDirtyRect() {
        return mDirtyRectangle;
    }
    
    public void updateDrawRegion(){
        this.mReadBitmap = Bitmap.createBitmap(mWritableBitmap);
        this.isConsumed = false;
    }
    
    public Bitmap consumeBitmap(){
        this.isConsumed = true;
        return mReadBitmap;
    }
    
    public void markConsumed(){
        this.isConsumed = true;
    }
    
}
