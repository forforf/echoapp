package org.younghawk.echoapp;

import java.util.concurrent.ConcurrentHashMap;

import org.younghawk.echoapp.drawregion.DrawRegionFactory;
import org.younghawk.echoapp.drawregion.DrawRegionNames;
import org.younghawk.echoapp.drawregion.DrawRegionType;
import org.younghawk.echoapp.handlerthreadfactory.HThread;
import org.younghawk.echoapp.handlerthreadfactory.HandlerThreadExecutor;

import android.app.Application;
import android.util.Log;

public class GlobalState extends Application {
    private static final String TAG="EchoApp GlobalState";
    
    //All instance variables are lazy loaded unless otherwise noted
    
    //Centralized Executor
    //mainly so we can stop all application threads easily
    private HandlerThreadExecutor gExecutor;
    
    //Draw Regions
    //The Surface is divided into different independently operating regions
    //gDrawRegionAreas define the rectangles of the Area
    //gDrawRegionHThreads holds the thread (and handler) responsible for that region
    private ConcurrentHashMap<DrawRegionNames, DrawRegionType> gDrawRegionAreas; 
    private ConcurrentHashMap<DrawRegionNames, HThread> gDrawRegionHThreads = new ConcurrentHashMap<DrawRegionNames, HThread>();
    
    //Reference to this instance
    private static GlobalState gGlobal;
    
    
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
    
    //Return this (so we don't need chains of context passing)
    public static GlobalState getGlobalInstance(){
        if(gGlobal==null){
            Log.e(TAG, "Unable to return Global instance");
        }
        return gGlobal;
    }
    
    //Constructor
    public GlobalState(){
        super();
        gGlobal = this;
    }

    //Draw Region Methods
    public DrawRegionType getRegionArea(DrawRegionNames reg_name){
        DrawRegionType reg_type = null;
        if(gDrawRegionAreas!=null){
            reg_type = gDrawRegionAreas.get(reg_name);
        }
        return reg_type;
    }
    
    //TODO: Change to support drawing interface, rather than concrete PanelDrawer
    public void setRegionArea(DrawRegionNames reg_name, PanelDrawer panel_drawer){
        //Lazy Load
        if(gDrawRegionAreas==null){
            gDrawRegionAreas = new ConcurrentHashMap<DrawRegionNames, DrawRegionType>();
        }
        
        //set Region Area
        DrawRegionType draw_reg_area=null;
        switch (reg_name) {
        case RADAR:
            draw_reg_area = DrawRegionFactory.radarRegion(panel_drawer);
            break;
        case GRAPH:
            draw_reg_area = DrawRegionFactory.graphRegion(panel_drawer);
            break;
        }
        gDrawRegionAreas.put(reg_name, draw_reg_area); 
    }
    
    public HThread getRegionThread(DrawRegionNames reg_name){
        HThread thr = null;
        if(gDrawRegionHThreads!=null){
            thr = gDrawRegionHThreads.get(reg_name);
        }
        return thr;
    }
    public void setRegionThread(DrawRegionNames reg_name){
        //Lazy Load
        if(gDrawRegionHThreads==null){
            gDrawRegionHThreads = new ConcurrentHashMap<DrawRegionNames, HThread>();
        }
        
        //set Region Thread
        switch(reg_name) {
        case RADAR:
            //If the radar drawing thread doesn't exist create it
            if(gDrawRegionHThreads.containsKey(DrawRegionNames.RADAR)){ //contains key
                if(!gDrawRegionHThreads.get(DrawRegionNames.RADAR).isAlive()){ //but not alive
                    gDrawRegionHThreads.put(DrawRegionNames.RADAR, getHandlerThread("radarHandler-reborn"));  //create thread  
                } //if it's alive then we're ok
            } else { //doesn't contain key (so can't be alive)      
                gDrawRegionHThreads.put(DrawRegionNames.RADAR, getHandlerThread("radarHandler")); 
            }
            break;
        case GRAPH:
          //If the graph drawing thread doesn't exist create it
            if(gDrawRegionHThreads.containsKey(DrawRegionNames.GRAPH)){ //contains key
                if(!gDrawRegionHThreads.get(DrawRegionNames.GRAPH).isAlive()){ //but not alive
                    gDrawRegionHThreads.put(DrawRegionNames.GRAPH, getHandlerThread("graphHandler-reborn"));  //create thread  
                } //if it's alive then we're ok
            } else { //doesn't contain key (so can't be alive)      
                gDrawRegionHThreads.put(DrawRegionNames.GRAPH, getHandlerThread("graphHandler")); 
            }
            break;
        }
    }
}