package org.younghawk.echoapp;

import android.media.AudioRecord;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceView;

/**
 * Supervises and handles callbacks for 
 * audio recording, playing and filtering threads
 */
public class AudioSupervisor implements Callback {
	private static final String TAG = "EchoApp AudioSupervisor";
	//This class should be a singleton
	private static AudioSupervisor instance = null;
	
	private GlobalState gGlobal;
	
	//TODO: Migrate to executor and thread factory.
	public HandlerThread mAudioRecordThr;
	public HandlerThread mAudioBufferThr;
	public HandlerThread mPingerThr;
	private final Handler mAudioRecordHandler; //Handler for hw thread
	
	private final Handler mPingerHandler; //Handler for pinger thread
	private final Handler mMainHandler; //Handler for this thread (main thread)
	//Audio Data
	private AudioRecorder mAudioRecorder; //Facade for AudioRecord
	//private static final int SAMPPERSEC = 44100; 
	//private static final double MAX_SAMPLE_TIME = 0.1; //in seconds
	//public short[] mBuffer;  //TODO: Make sure this is uded correctly

	private PingRunner mPinger;
	private short[] mFilter;

	private AudioFilterProxy mAudioFilter;
	
	private Plotter mPlotter;
	//TODO: Undesired coupling between Supervisors
	//private PlotSupervisor mPlotSupervisor;
	//private AudioUpdatesDeadCode mCallback;

	//TODO: Change to local variable naming convention instead of instance variable naming convention
	public static AudioSupervisor create(String instructions, int num_of_samples) {
	    if(instance!=null){
	        return instance;
	    } else {
	    
	        HandlerThread mAudioRecordThr = new HandlerThread("Audio Recorder");
	        mAudioRecordThr.start();

	        //HandlerThread mAudioBufferThr = new HandlerThread("Audio Buffering");
	        //mAudioBufferThr.start();

	        HandlerThread mPingerThr = new HandlerThread("Play Audio Ping");
	        mPingerThr.start();

	        AudioRecorder audioRecorder = AudioRecorder.create(
	                GlobalState.Audio.SAMPPERSEC, 
	                GlobalState.Audio.MAX_SAMPLE_TIME);
	        
	        //AudioRecord audioRecord = audioRecordWrapper.mAudioRecord;

	        //TODO: This should get spun up in its own handler thread like the others
	        //PingRunner pinger = PingRunner.create(instructions, num_of_samples);
	        
	        //TODO: UPDATE THE FILTER CLASSES TO SUPPORT PCMFILTERING
	        //AudioFilterDead audioFilter = AudioFilterDead.create(pinger.mPcmFilterMask, audioRecordWrapper.mBufferSizeShorts);
	        AudioFilterProxy audioFilter;// = AudioFilterProxy.getInstance();
	        
	        
	        Looper arLooper = mAudioRecordThr.getLooper();
	        Handler audioHandler = null;
	        if (arLooper!=null) {
	            audioHandler = new Handler(arLooper); 
	        } else {
	            Log.e(TAG, "Audio Looper was null, was thread started?");
	        }

	        //Looper bufLooper = mAudioBufferThr.getLooper();
	        //Handler bufferHandler = new Handler(bufLooper);
	        //if (bufLooper!=null){
	        //    bufferHandler = new Handler(bufLooper);
	        //} else {
	        //    Log.e(TAG, "Buffer Looper was null, was thread started?");
	        //}

	        Looper pingLooper = mPingerThr.getLooper();
	        Handler pingerHandler = new Handler(pingLooper);
	        if (pingLooper!=null){
	            pingerHandler = new Handler(pingLooper);
	        } else {
	            Log.e(TAG, "Pinger Looper was null, was thread started?");
	        }
	        
	        instance =  new AudioSupervisor(
	                mAudioRecordThr, 
	                //mAudioBufferThr,
	                mPingerThr,
	                audioHandler, 
	                //bufferHandler,
	                pingerHandler,
	                audioRecorder);
	        
	        return instance;
	    }
	}
	private AudioSupervisor(HandlerThread audioRecThr, 
			//HandlerThread audioBufThr,
			HandlerThread pingThr,
			Handler audioHandler, 
			//Handler bufferHandler,
			Handler pingHandler,
			AudioRecorder audioRecorder) {
		
		this.mAudioRecordThr = audioRecThr;
		//this.mAudioBufferThr = audioBufThr;
		this.mPingerThr = pingThr;
		this.mAudioRecordHandler = audioHandler;
		//this.mAudioBufferHandler = bufferHandler;
		this.mPingerHandler = pingHandler;
		this.mMainHandler = new Handler(this);
		this.mAudioRecorder = audioRecorder;
		//this.mAudioRecord = audioRecorder.mAudioRecord;
		//this.mPinger = pinger;
		//this.mFilter = pinger.mPcmFilterMask;
		
		
		this.gGlobal = GlobalState.getGlobalInstance();
		this.mAudioFilter = gGlobal.getFilterProxy();
		this.mFilter = gGlobal.getEchoFilterMask();
	}
	
	//TODO: Pass in filter as parameter
	public void startRecording() {
		Log.d(TAG,"Audio Supervisor starting recording, posting to Handler");
		mAudioRecordHandler.post(new Runnable(){
			@Override
			public void run() {
				Log.d(TAG, "Trying to start AudioRecord: " + mAudioRecorder + " on thread: " + Thread.currentThread().getName());
				mAudioRecorder.startRecording();
				mMainHandler.sendEmptyMessage(MsgIds.RECORD_READY);
				
				int iter = 0;
				while(iter<2000){
				    int samplesRead = mAudioRecorder.read(mAudioRecorder.mBuffer,  0, mAudioRecorder.mBufferSizeShorts);
				    Log.d(TAG, "Audior recorder read " + samplesRead + " audio samples");


				    Log.d(TAG, "Im Alive 1");
				    //Message bufferMsg = Message.obtain(mMainHandler, MsgIds.BUFFER_DATA, mAudioRecorder.mBuffer);
				    Message bufferMsg = Message.obtain(mMainHandler, MsgIds.BUFFER_DATA, mAudioRecorder.mBuffer);
				    Log.d(TAG, "Im Alive 2");
				    mMainHandler.sendMessage(bufferMsg);
				    Log.d(TAG, "Im Alive 3");
				    //Apply AudioFilterStub
				    //TODO: Requires refactoring, expensive operation here - perhaps its own thread?
				    //TODO: Also class is inside deprecated package
				    //AudioFilterDead rxEnergyFilter = AudioFilterDead.create(mAudioRecorder.mBuffer, mFilter);
				    Log.d(TAG, "Audio AudioFilterStub: " + mAudioFilter.toString());
				    Log.d(TAG, "Im Alive 4");
				    
				    //Data is filtered here!!!
				    int[] rx_energy = mAudioFilter.filter(mAudioRecorder.mBuffer);
				    
				    
				    //CollectionGrapher audioPlot = CollectionGrapher.create(50,100,350,400, rx_energy);
				    //DebugData.setDebugArray(audioPlot);
				    Log.d(TAG, "Im Alive 5");
				    Message filterMsg = Message.obtain(mMainHandler, MsgIds.FILTER_DATA, rx_energy);
				    Log.d(TAG, "Im Alive 6");
				    mMainHandler.sendMessage(filterMsg);
				    iter++;
				}
			}
		});
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case MsgIds.RECORD_READY:
			onRecordReady();
			break;
		case MsgIds.BUFFER_DATA:
		    //Buffer Data is deprecated, since it's identical
		    //to Filter data with a null filter.
		    //mPlotSupervisor.onBufferData(msg.obj);
			break;
		case MsgIds.FILTER_DATA:
			onFilterData(msg.obj);
			break;
		}
		
		return false;
	}
	
	public void shutDown() {
	    //There may be more to do, but this at least
	    //cleans up the running threads.
	    Log.d(TAG, "Shutting threads down");
	    if (mAudioRecordThr!=null){
	        mAudioRecordThr.quit();
	        mAudioRecordThr = null;
	    }

        if (mAudioBufferThr!=null) {
            mAudioBufferThr.quit();
            mAudioBufferThr = null;
        }

        if (mPingerThr!=null) {
            mPingerThr.quit();
            mPingerThr = null;
        }
        Log.d(TAG, "Threads shut down");
	}
	
	public void onRecordReady(){
		Log.d(TAG,"Main thread notified that Audio Recorder is Ready");
		
		
		
		
		//DOES THE PING
		Log.d(TAG,"Audio Supervisor sending ping to Pinger Thread --CURRENTLY DISABLED");
		//mPingerHandler.post(mPinger);
		
	}

    //IMPORTANT: In the current implementation this is called only once
    //since the buffer size = audio data size. Changing to be more flexible
    //will require this method to execute via a thread handler post, and
    //flushing and stitching buffers together would need to be handled.
    public void onBufferData(Object objBuffer){
        short[] buffer = (short[]) objBuffer;
        //Log.d(TAG, "AudioSupervisor (main thread) notified of buffer with " + buffer.length + " samples");
        //Debug graph
        //DebugData.setDebugArray( CollectionGrapher.create(100,100,250,40, buffer) );
    }
	
	public void onFilterData(Object objFilterData) {
		int[] filter_data = (int[]) objFilterData;
		Log.d(TAG,"Main thread notified with filter data with " + filter_data.length + " elements (samples).");
		
		//Lazy Load Plotter
		if(mPlotter==null){
		    mPlotter = gGlobal.getPlotter();
		}
		//mPlotSupervisor.mPlotter.addToQ(filter_data);
		mPlotter.addToQ(filter_data);
		

		//Checking if Paused (default when started)
		//If its paused we start it .. this is not robust, since we can't
		//actually ever stop it now
		//Ummmm ... this should only be done when?
		//Log.d(TAG, "Plot Supervisor: " + mPlotSupervisor);
		if(mPlotter.pauseQCheck){
		    mPlotter.startQCheck();
		}
	}
}


