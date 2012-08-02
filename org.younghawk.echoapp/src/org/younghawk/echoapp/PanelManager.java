package org.younghawk.echoapp;

import android.view.SurfaceHolder;

public class PanelManager {
    private static final String TAG = "EchoApp PanelManager";
    
    public SurfaceHolder mSurfaceHolder;

    //Panel Manager should only be created after Panel
    //is done initializing
    public static PanelManager create(Panel panel) {
        //Setup Surface Holder
        SurfaceHolder panelSurfaceHolder = panel.getHolder();
        
        //Setup the callbacks on panel
        panel.getHolder().addCallback(panel);
        
        
        return new PanelManager();
    }

    private PanelManager() {

    }
}
