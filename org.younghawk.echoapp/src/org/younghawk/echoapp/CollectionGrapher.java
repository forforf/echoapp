package org.younghawk.echoapp;

import java.util.ArrayList;
import java.util.Collection;

import android.util.Log;

/**
 * Create an array of canvas points ready to be plotted given Array items
 * TODO: Selector for pos/neg values or just positive
 */


public class CollectionGrapher {
    public static final String TAG = "EchoApp Grapher";
    public float   mMaxVal;
    public float   mMinVal;
    public float[] mCanvasPts;
    
    static class CalcResponse {
        public final ArrayList<Float> mCalcpts;
        public final Float mCalcMax;
        public final Float mCalcMin;
        
        public CalcResponse(ArrayList<Float> canvas_pts_list, float max, float min) {
            this.mCalcpts = canvas_pts_list;
            this.mCalcMax = max;
            this.mCalcMin = min;
        }
    }
    
    static class Calc{
        public CalcResponse mCalcCanvasData;
        private final CalcResponse toCanvasList( Float[] arr){
            ArrayList<Float> canvas_pts_list = new ArrayList<Float>();
            int arr_size = arr.length;
            Float max = null;
            Float min = null;
            for(int i=0; i<arr_size;i++) {
                if(arr[i] == null){
                    continue;
                }
                
                canvas_pts_list.add( (float) i );
                float valf = arr[i].floatValue();
                //while we're iterating anyway, calculate max and min
                //max
                if(max==null) {
                    max = valf;
                }
                if(valf>max) {
                    max = valf;
                }
                //min
                if(min==null) {
                    min = valf;
                }
                if(valf<min){
                    min = valf;
                }
                canvas_pts_list.add( valf );
                /*
                if(arr[i] instanceof Number){
                    Number val = (Number) arr[i];
                    canvas_pts_list.add( (float) i );
                    float valf = val.floatValue();
                    //while we're iterating anyway, calculate max and min
                    //max
                    if(max==null) {
                        max = valf;
                    }
                    if(valf>max) {
                        max = valf;
                    }
                    //min
                    if(min==null) {
                        min = valf;
                    }
                    if(valf<min){
                        min = valf;
                    }
                    canvas_pts_list.add( valf );


                } else {  //don't care if it's not numeric
                    continue;
                }
                */
            }
            return new CalcResponse(canvas_pts_list, max, min);
        }
        public Calc(Float[] arr){
            this.mCalcCanvasData = toCanvasList(arr);
        }
    }

    
    public static CollectionGrapher create(Number x_offset, Number y_offset, Number width, Number height, Object arr_thing) {
        float xo;
        float yo;
        float w;
        float h;
        CalcResponse canvas_data;
        float[] canvas_pts;
        
        //Take care of nulls
        if (x_offset==null) { x_offset = 0; };
        if (y_offset==null) { y_offset = 0; };
        if (width==null)    { width = 1;  };
        if (height==null)   { height = 1; };
        if (arr_thing==null)      { arr_thing = new Float[0]; };
        
        //Convert numerics to floats
        xo = x_offset.floatValue();
        yo = y_offset.floatValue();
        w = width.floatValue();
        h = height.floatValue();
        
        //Minimum width and height is 1
        if (w<1)  { w = 1;  };
        if (h<1)  { h = 1; };

        //Handle Collections
        ArrayList<Float> canvas_pts_list = null;
        Float max = null;
        Log.d(TAG, "Class Name of arr thing: " + arr_thing.getClass().isArray());
        Log.d(TAG, "Arr thing instance of Object[]: " + (arr_thing instanceof Object[]) );
        
        if (arr_thing instanceof Collection) {
            //Not the most efficient, but fairly safe
            //Collection coll = (Collection) arr_thing;
            //Object[] arr_coll = coll.toArray();
            //canvas_data = (new Calc(arr_coll)).mCalcCanvasData;
            //canvas_pts_list = canvas_data.mCalcpts;
            //max = canvas_data.mCalcMax;
        } 
        //Handle float[]
        else if (arr_thing instanceof float[] )
        { 
            Log.d(TAG, "Handling float Array!!");
            //cast to a common type (Float works for this class)
            float[] arr_f = (float[]) arr_thing;
            int arr_size = arr_f.length; 
            Float[] arr_F = new Float[arr_size];
            for (int i=0;i<arr_size;i++) {
                arr_F[i] = (Float) arr_f[i];
            }
            Log.d(TAG, "Ok so far");
            canvas_data = (new Calc(arr_F)).mCalcCanvasData;
            canvas_pts_list = canvas_data.mCalcpts;
            max = canvas_data.mCalcMax;
        }
        //Handle short[]
        else if (arr_thing instanceof short[] )
        { 
            Log.d(TAG, "Handling short Array!!");
            //cast to a common type (Float works for this class)
            short[] arr_sh = (short[]) arr_thing;
            int arr_size = arr_sh.length; 
            Float[] arr_F = new Float[arr_size];
            for (int i=0;i<arr_size;i++) {
                arr_F[i] = Float.valueOf(arr_sh[i]);
            }
            Log.d(TAG, "Ok so far");
            canvas_data = (new Calc(arr_F)).mCalcCanvasData;
            canvas_pts_list = canvas_data.mCalcpts;
            max = canvas_data.mCalcMax;
        }
        //we were passed something we can't iterate over
        else
        {
            Log.e(TAG, "Cannot iterate over object of " + arr_thing.getClass().toString());
            //canvas_pts_list should be empty if nothing to iterate over
            canvas_pts_list = new ArrayList<Float>();
        }
        
        //Max and Min should be calculated now
        //TODO Throw error if they are null
        //float range = mMaxVal - mMinVal;
   
        int canvas_pts_size = canvas_pts_list.size();
        canvas_pts = new float[canvas_pts_size];
        int i=0;
        for(Float pt: canvas_pts_list){
            //scale x
            if(i%2==0){
                canvas_pts[i] = (w/canvas_pts_size)*i + xo;
            }
            //scale y
            if(i%2==1){
                canvas_pts[i] = (max - pt) * h + yo;
            }
            
            //canvas_pts[i] = pt;
            i++;
        }
        return new CollectionGrapher(canvas_pts);
    }

    private CollectionGrapher(float[] canvas_pts) {
        this.mCanvasPts = canvas_pts;
    }
}
