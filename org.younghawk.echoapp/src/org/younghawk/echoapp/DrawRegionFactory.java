package org.younghawk.echoapp;

import android.graphics.Rect;

public class DrawRegionFactory {

    public static DrawRegionType radarRegion(ImmutableRect surface_rect){
        float track_width = (float) surface_rect.width();
        float r = (float) surface_rect.height()/100; //circle radius
        float track_height = 2 * r;
        float top_pad = r; //padding from the top
        Rect radar_rect = new Rect(
                0,Math.round(top_pad),
                Math.round(track_width),
                Math.round(top_pad + track_height)
                );
        return new DrawRegionRadar(radar_rect, r);
    }
    
    public static DrawRegionType graphRegion(ImmutableRect surface_rect){
        float width = (float) surface_rect.width();
        float height = (float) surface_rect.height();
        float side_padding = surface_rect.width()/50f;
        float top_padding = surface_rect.height()/4f;
        float bot_padding = surface_rect.height()/20f;
        float graph_width = width - (side_padding*2f);
        float graph_height = height - top_padding - bot_padding;
        Rect graph_rect = new Rect(
                Math.round(side_padding),
                Math.round(top_padding),
                Math.round(side_padding + graph_width),
                Math.round(top_padding + graph_height)
                );
        return new DrawRegionGraph(graph_rect);
    }
}
