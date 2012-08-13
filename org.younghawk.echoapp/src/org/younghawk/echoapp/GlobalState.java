package org.younghawk.echoapp;

import java.util.concurrent.ConcurrentHashMap;

import org.younghawk.echoapp.drawregion.DrawRegionNames;
import org.younghawk.echoapp.drawregion.DrawRegionType;
import org.younghawk.echoapp.handlerthreadfactory.HThread;
import org.younghawk.echoapp.handlerthreadfactory.HandlerThreadExecutor;

import android.app.Application;

public class GlobalState extends Application {
    //All instance variables are lazy loaded unless otherwise noted
    
    //Centralized Executor
    //mainly so we can stop all application threads easily
    private HandlerThreadExecutor gExecutor;
    
    //Draw Regions
    //The Surface is divided into different independently operating regions
    //gDrawRegionAreas define the rectangles of the Area
    //gDrawRegionHThreads holds the thread (and handler) responsible for that region
    private ConcurrentHashMap<DrawRegionNames, DrawRegionType> gDrawRegionAreas = new ConcurrentHashMap<DrawRegionNames, DrawRegionType>();
    private ConcurrentHashMap<DrawRegionNames, HThread> gDrawRegionHThreads = new ConcurrentHashMap<DrawRegionNames, HThread>();

    
    
    //Executor Methods
    public HThread getHandlerThread(String name){
        if(gExecutor==null){
            gExecutor = new HandlerThreadExecutor("EchoApp");
        }
        return gExecutor.execute(null, name);
    }
    
    public void stopAllThreads(){
        if(gExecutor!=null){
            gExecutor.stopThreads();    
        }
    }

    //Draw Region Methods
    public DrawRegionType getRegionArea(DrawRegionNames reg_name){
        DrawRegionType reg_type = null;
        if(gDrawRegionAreas!=null){
            reg_type = gDrawRegionAreas.get(reg_name);
        }
        return reg_type;
    }
}
