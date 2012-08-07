package org.younghawk.echoapp;

import java.util.ArrayDeque;
import java.util.ArrayList;

import org.younghawk.echoapp.drawregion.DrawRegionGraph;
import org.younghawk.echoapp.drawregion.DrawRegionNames;

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
    public static int testCtr = 0;
    
    //public final int Xoffset;
    //public final int Yoffset;
    //TODO: Figure out optimum instantiation size
    //TODO: Refactor so it isn't static?
    public static ArrayDeque<Float> mScaledSamples = new ArrayDeque<Float>();
    public static final ArrayDeque<Float>[] PlotQ = new ArrayDeque[PLOT_WIDTH];
    public static boolean plotReady = false;
    
    //Refactor so this isn't staic
    public static float[] toCanvasPointsArray(ArrayDeque<Float>[] plot_q, int x_offset, int y_offset ) {
        ArrayList<Float> temp = new ArrayList<Float>();
        float x=0;
        float y=0;
        float x_shift = (float) x_offset;
        float y_shift = (float) y_offset + PLOT_HEIGHT/2;
        
        for (int i=0;i<PLOT_WIDTH;i++) {
            x = (float) i;
            //TODO: Evaluate performance of removing each vs convert to Array and iterating
            if (plot_q[i]!=null){
                //Log.d(TAG, "size: " + plot_q[i].size() );
                for(int j=0;j<plot_q[i].size();j++) {
                    //Log.d(TAG, "x: " + x);
                    //Log.d(TAG, "x_shift: " + x_shift);
                    temp.add(x + x_shift);
                    temp.add( plot_q[i].pollFirst() + y_shift);
                }
            } else {
                //Do anything?
            }
        }
        int canvas_pts_size = temp.size();
        Float[] temp2 = temp.toArray(new Float[canvas_pts_size]);
        
        float[] canvas_pts = new float[canvas_pts_size];
        for (int k=0;k<canvas_pts_size;k++) {
            if (temp2[k]!=null) {
                canvas_pts[k] = (float) temp2[k];
            } else {
                Log.e(TAG, "Somehow a null got into the set of canvas points");
            }
        }
        return canvas_pts;
    }
     
    //Refactor so this isn't static
    public static synchronized void fillPlotQ(){
        //shift PlotQ down 1
        for(int i=1;i<PLOT_WIDTH;i++) {
            PlotQ[i-1] = PlotQ[i];
        }

        //ArrayDeque<Float> last_element = PlotQ[PLOT_WIDTH-1];
        
        //Add new y values at end of PlotQ 
        if (PlotQ[PLOT_WIDTH-1]==null) {
            //Log.d(TAG, "Last element was null");
            PlotQ[PLOT_WIDTH-1] = new ArrayDeque();
        } else {
            //Log.d(TAG, "Last element was not null");
            PlotQ[PLOT_WIDTH-1].clear();
        }
         
        
        //testCtr++;
        
        //PlotQ[PLOT_WIDTH-1].add((float) testCtr);
        //Log.d(TAG, "Last (PlotQ) Element: " + last_element);
        //Log.d(TAG, "Last PlotQ!! Element: " + PlotQ[PLOT_WIDTH-1]);
        
        //Log.d(TAG, "PlotQ samples size: " + mScaledSamples.size());
       
        if (Plotter.mScaledSamples.size()>0) {
            //last_element = new ArrayDeque();
            //Log.d(TAG, "First sample size to grab " +  Plotter.mScaledSamples.getFirst() );
            for(int i=0;i<PTS_PER_PX;i++){
                PlotQ[PLOT_WIDTH-1].add( Plotter.mScaledSamples.removeFirst() );
            }
            //Log.d(TAG, "Next sample to grab: "+  Plotter.mScaledSamples.getFirst() );
        }
        plotReady = true;
        
        
        Log.d(TAG, "Trying to connect to drawer");
        DrawRegionGraph graphData = (DrawRegionGraph) PanelDrawer.mDrawRegionAreas.get(DrawRegionNames.GRAPH);

        if(graphData!=null){
            Log.d(TAG, "Converting data to canvas points");
            float[] canvas_pts = toCanvasPointsArray(PlotQ,0,0);
            Log.d(TAG, "Updating graph region drawer with canvas pts");
            graphData.onVectorUpdate(canvas_pts);
        } else {
            Log.w(TAG, "GRAPH Drawer was nulll");
        }
        
        //} else {
        //    Log.e(TAG, "Graph Data seems to be null: " + graphData);
        //}
         /*
            for(int i=0;i<PTS_PER_PX;i++){
                Log.d(TAG, "Presize: " + mScaledSamples.size());
                Float y = null;
                try {
                    y = mScaledSamples.removeFirst();
                } catch (Exception e) {
                    Log.w(TAG, "removeFirst() caused: " + e);
                }
                Log.d(TAG, "Postsize: " + mScaledSamples.size());
                if(last_element==null){
                    //Log.d(TAG, "PlotQ setting last element");
                    last_element = new ArrayDeque();
                }
                if(y!=null){
                    Log.d(TAG, "Added: " + y);
                    last_element.add(y);
                }
            }
            PlotQ[PLOT_WIDTH-1] = last_element;
            Log.d(TAG, "Last PlotQ Element: " + last_element);
        }
        */  
    }
 
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
