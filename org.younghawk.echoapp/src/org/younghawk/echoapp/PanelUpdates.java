package org.younghawk.echoapp;

import android.view.SurfaceHolder;

public interface PanelUpdates {
   public void panelCreated(SurfaceHolder holder, int w, int h);
   public  void panelChanged(SurfaceHolder holder, int format, int w, int h);
   public void panelNotAvailable(SurfaceHolder holder);

}
