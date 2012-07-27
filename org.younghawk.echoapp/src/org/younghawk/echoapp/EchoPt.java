package org.younghawk.echoapp;

public class EchoPt {
    public int idx;
    public int val;
    public EchoPt(int _idx, int _val){
        idx = _idx;
        val = _val;
    }
    
    public String toString(){
        return "idx: " + idx + "  val: " + val;
    }

}
