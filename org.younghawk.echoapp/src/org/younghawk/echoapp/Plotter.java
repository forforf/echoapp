package org.younghawk.echoapp;

import java.util.ArrayDeque;

import android.util.Log;

public class Plotter {
    private static final String TAG = "EchoApp Plotter";
    
    //This class should be a singleton
    private static Plotter instance = null;
    
    //TODO: Move Constants to parameters
    public static final float PLOT_DWELL_TIME = (float) 4.0; //seconds (duration a pt will be on plot)
    public static final int PLOT_WIDTH = 400; //px
    public static final int PLOT_HEIGHT = 300; //px
    public static final float PX_DWELL_TIME = PLOT_DWELL_TIME / (float) PLOT_WIDTH;
    public static final int MAX_VAL = 32768;
    
    //TODO: Should come from preferences
    public static final int RAW_INPUT_RATE = 44100; //samples per second
   
    //TODO: Calculate in create?
    public static final int PTS_PER_PX = Math.round(PX_DWELL_TIME * RAW_INPUT_RATE);
    public static final int VERT_LINES_PLOT_SIZE= 4 * PLOT_WIDTH;
    public static final float[] VERT_LINES_TO_PLOT = new float[VERT_LINES_PLOT_SIZE]; 
    public static final int[] DATA_BUFFER = new int[PTS_PER_PX]; //holds any leftover incoming data
    //public final int Xoffset;
    //public final int Yoffset;
    public ArrayDeque<Float> mScaledSamples = new ArrayDeque<Float>(); //TODO: Figure out optimum instantiation size
    
    
 
    public static Plotter create() {
        if(instance!=null){
            return instance;
        } else {
            instance = new Plotter();
            return instance;
        }
 
    }

    private Plotter() {
    }
    
    //TODO: Implement logscale option
    
   public synchronized void addToQ(int[] data) {
       for(int i=0;i<data.length;i++) {
           //scale data
           mScaledSamples.add( (data[i] * PLOT_HEIGHT) / (float) MAX_VAL );
        }
    }
    
    
    public void shiftPlotLines(float x0, float y0, float x1, float y1) {
      //Log.d(TAG, "Shifting" + x0 + y0 + x1 + y1);
      for(int i=0;i<=VERT_LINES_PLOT_SIZE-8;i+=4) {
          VERT_LINES_TO_PLOT[i]   = i / 4; //VERT_LINES_TO_PLOT[i+4];
          VERT_LINES_TO_PLOT[i+1] = VERT_LINES_TO_PLOT[i+5];
          VERT_LINES_TO_PLOT[i+2] = i / 4; //VERT_LINES_TO_PLOT[i+6];
          VERT_LINES_TO_PLOT[i+3] = VERT_LINES_TO_PLOT[i+7];
      }
      VERT_LINES_TO_PLOT[VERT_LINES_PLOT_SIZE-4] = PLOT_WIDTH-1; //x0;
      VERT_LINES_TO_PLOT[VERT_LINES_PLOT_SIZE-3] = y0;
      VERT_LINES_TO_PLOT[VERT_LINES_PLOT_SIZE-2] = PLOT_WIDTH-1; //x1;
      VERT_LINES_TO_PLOT[VERT_LINES_PLOT_SIZE-1] = y1;
      
      //Log.d(TAG, "Done shifting");
    }
    
    public synchronized void pushAudioData(int[] data){
      //Log.d(TAG, Arrays.toString(data));
      Log.d(TAG, "Plotter received plot data");
      int pxs_to_fill = data.length / PTS_PER_PX;
      int left_over = data.length % PTS_PER_PX;
      Log.d(TAG, "Pxs to fill: " + pxs_to_fill);
      Log.d(TAG, "left over: " + left_over);
      if (left_over > 0) {
          Log.d(TAG, "Left overs, ignore at your peril");
      }
      float[] px_data = new float[4]; //4 data points per px width (draws vertical line)
      //fill pxs
      for (int i=0;i<pxs_to_fill;i++) {
          Log.d(TAG,"px: " + i);
          int max=0;
          int min=0;
          for (int j=0;j<PTS_PER_PX;j++){
              //Log.d(TAG, "pt w-in px: " + j);
              
              int idx = i*PTS_PER_PX + j;
              //if (data[idx]<0){
              //    Log.d(TAG, "data: " + data[idx]);
              //}
              if (data[idx]<min) {
                  px_data[0] = 0; //Overridden later (Fix) was: (float) j;
                  px_data[1] = (float) data[idx];
              }
              if (data[idx]>max) {
                  px_data[2] = 0; //Overridden later (Fix) was: (float) j;
                  px_data[3] = (float) data[idx];
              }
              
          }
          shiftPlotLines(px_data[0], px_data[1], px_data[2], px_data[3]);
          for(int k=0;k<px_data.length;k++) {
              px_data[k] = 0;
          }
      }
      
      //Log.d(TAG, "PlotLines: " + Arrays.toString(VERT_LINES_TO_PLOT));
      
    }
    
    public synchronized float[] getPlotData(){
        return VERT_LINES_TO_PLOT;
    }
    
}
