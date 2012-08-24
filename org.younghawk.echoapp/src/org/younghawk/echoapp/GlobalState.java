package org.younghawk.echoapp;

import java.util.concurrent.ConcurrentHashMap;

import org.younghawk.echoapp.drawregion.DrawRegionFactory;
import org.younghawk.echoapp.drawregion.DrawRegionNames;
import org.younghawk.echoapp.drawregion.DrawRegionRadar;
import org.younghawk.echoapp.drawregion.DrawRegionType;
import org.younghawk.echoapp.drawregion.ScrollingBitmap;
import org.younghawk.echoapp.handlerthreadfactory.HThread;
import org.younghawk.echoapp.handlerthreadfactory.HandlerThreadExecutor;
import org.younghawk.echoapp.signals.SignalGenerator;

import android.app.Activity;
import android.app.Application;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GlobalState extends Application {
    private static final String TAG="EchoApp GlobalState";
    
    
    public enum ControlButtonState {
        START, PING
    }
    
    public Handler mMainHandler = new Handler();
    
    //TODO: Move to shared preferences
    //Audio Constants
    public class Audio {
        public static final int SAMPPERSEC = 44100; 
        //TODO: Ensure sample time doesn't go lower than the min buffer allowed
        public static final double MAX_SAMPLE_TIME = 0.1; //seconds
        
        
    }
    
    
    //All instance variables are lazy loaded unless otherwise noted
    
    //Reference to Surface View -- Created automatically with application
    private EchoApp mEchoApp;
    private Panel mPanel;
    private PanelDrawer mPanelDrawer;
    private AudioSupervisor mAudioSupervisor;
    private Plotter mPlotter;
    private ScrollingBitmap mScrollingBitmap;
    private AudioFilterProxy mFilterProxy;
    private SignalGenerator mSigGen;
    private short[] mEchoFilterMask;
    private AudioFilter mEchoFilter;
    private AudioFilter mNullFilter;
    private PingRunner mPinger;
    private CaptureAudio mCaptureAudio;
    
    //Shared Variables
    private ImmutableRect mFullSurfaceRect;
    private ControlButtonState mControlButtonState;
    
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
    
    //Return this (so we don't need chains of context passing)
    public static GlobalState getGlobalInstance(){
        if(gGlobal==null){
            Log.e(TAG, "Unable to return Global instance");
        }
        return gGlobal;
    }
    
    //Application Create NOT Activity Create
    @Override
    public void onCreate(){
        super.onCreate();
        gGlobal = this;
    }
    
    public void pauseApp() {
        if (mAudioSupervisor!=null){
            mAudioSupervisor.shutDown();
            mAudioSupervisor = null;
        }
    }
    
    //Clearing house for cross-component events
    public void onEchoAppReady(Activity act){
        mEchoApp = (EchoApp) act;
        Log.d(TAG, "Echo App Ready");
        mControlButtonState = GlobalState.ControlButtonState.START;
    }
    
    
    //TODO: Refactot the duplication with signal generator
    public void startAudioRecording(){
        if(mAudioSupervisor==null){
            mAudioSupervisor = AudioSupervisor.create();
        }
        mAudioSupervisor.startRecording();
    }
    
    public void onPanelReady(SurfaceView panel){
        mPanel = (Panel) panel;
        Log.d(TAG, "Panel Ready");
        readySurfaceRegions();
    }
    
    //Set up the regions to draw on surface
    public void readySurfaceRegions(){
        //if(mPanelDrawer == null){
        //    mPanelDrawer = PanelDrawer.create(mPanel);
        //}
        Log.d(TAG, "Surface Ready tasks");
        
        setRegionArea(DrawRegionNames.RADAR, mPanelDrawer);
        setRegionArea(DrawRegionNames.GRAPH, mPanelDrawer);
        
        setRegionThread(DrawRegionNames.RADAR);
        setRegionThread(DrawRegionNames.GRAPH);
        
        getRegionThread(DrawRegionNames.RADAR).handler.post(
                new Runnable(){
                    @Override
                    public void run() {
                        DrawRegionRadar radarData = (DrawRegionRadar) getRegionArea(DrawRegionNames.RADAR);
                        radarData.run( mPanel.getHolder() );
                    };
                });
        
    }
    
    
    //Shared Variable Setter/Getters
    public void setSurfaceRect(ImmutableRect surf_rect){
        if(mFullSurfaceRect!=surf_rect){
            //TODO: Update/notifiy surf_rect dependencies
            mFullSurfaceRect = surf_rect;
        }
    }
    
    public ImmutableRect getSurfaceRect(){
        return mFullSurfaceRect;
    }
    
    public SurfaceHolder getMainHolder(){
        SurfaceHolder holder = null;
        if(mPanel!=null){
            holder = mPanel.getHolder();
        }
        return holder;
    }
    
    public Plotter getPlotter(){
        if(mPlotter==null){
            mPlotter = Plotter.create();
        }
        return mPlotter;
    }
    
    public ScrollingBitmap getScrollingBitmap(){
        //TODO: Enforce single Scrolling Bitmap object?
        return ScrollingBitmap.create();
    }
    
    public AudioFilterProxy getFilterProxy(){
        if(mFilterProxy==null){
            mFilterProxy = AudioFilterProxy.create();
        }
        return mFilterProxy;
    }
    
    public SignalGenerator getSigGen(){
        if(mSigGen == null){
            mSigGen =  SignalGenerator.create(
                    getString(R.string.signal_instructions),
                    getResources().getInteger(R.integer.samples_per_wav)
                    );
        }
        
        return mSigGen;
    }
    
    public short[] getEchoFilterMask(){
        if(mEchoFilterMask==null){
            mEchoFilterMask = getSigGen().mFilterMask;
        }
        
        return mEchoFilterMask;
    }
    
    public AudioFilter getEchoFilter(){
        if(mEchoFilter==null){
            mEchoFilter = AudioFilterEcho.create( getEchoFilterMask() );
        }
        return mEchoFilter;
    }
    
    public AudioFilter getNullFilter(){
        if(mNullFilter==null){
            mNullFilter = new AudioFilterNull();
        }
        return mNullFilter;
    }
    
    public GlobalState.ControlButtonState getControlButtonState(){
        if(mControlButtonState==null){
            mControlButtonState = ControlButtonState.START;
        }
        return mControlButtonState;
    }
    
    public void setControlButtonState(GlobalState.ControlButtonState btn_state){
        mControlButtonState = btn_state;
    }
    
    public CaptureAudio getCaptureAudio(){
        //Created when sending ping
        if(mCaptureAudio==null){
            mCaptureAudio = CaptureAudio.create(mMainHandler, Audio.SAMPPERSEC);
        }
        return mCaptureAudio;
    }
    
    public void sendPing(){
        //synchronized( getCaptureAudio() ){
        //    getCaptureAudio().start();
        //}
        getExecutor().execute(getPinger(), "Pinger");
    }
    
    
    //TODO: Where should private methods go?
    private PingRunner getPinger(){
        if(mPinger==null){
            mPinger = PingRunner.create();
        }
        return mPinger;
    }
    
    //Apply Audio Filter Methods
    public void echoFilterOn(){
        Log.d(TAG, "Echo Filter On");
        getFilterProxy().setFilter( getEchoFilter() );
    }
    
    public void echoFilterOff(){
        Log.d(TAG, "Echo Filter Off");
        getFilterProxy().setFilter( getNullFilter() );
    }
    
    //Executor Methods
    public HandlerThreadExecutor getExecutor(){
        if(gExecutor==null){
            gExecutor = new HandlerThreadExecutor("EchoApp");
        }
        return gExecutor;
    }
    
    public HThread getHandlerThread(String name){
        return getExecutor().execute(null, name);
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
            draw_reg_area = DrawRegionFactory.radarRegion(panel_drawer, mFullSurfaceRect);
            break;
        case GRAPH:
            draw_reg_area = DrawRegionFactory.graphRegion(panel_drawer, mFullSurfaceRect);
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
