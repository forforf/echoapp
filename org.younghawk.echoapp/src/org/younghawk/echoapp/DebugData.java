package org.younghawk.echoapp;

public class DebugData {
    //Will be overwritten
    public static float[] currentDebugData = new float[]{0f,3f,2f,3f,4f,3f};
    public static CollectionGrapher debugGrapher;
    
    public static synchronized void setDebugArray(CollectionGrapher cg){
        debugGrapher = cg;
        currentDebugData = cg.mCanvasPts;
    }
    
}
