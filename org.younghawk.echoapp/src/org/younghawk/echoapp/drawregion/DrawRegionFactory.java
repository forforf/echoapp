package org.younghawk.echoapp.drawregion;

import org.younghawk.echoapp.ImmutableRect;
import org.younghawk.echoapp.PanelDrawer;

import android.graphics.Rect;

/**
 * Holds the different types of regions and the data required to draw them.
 * All positions should be scaled to the surface size (surface_rect)
 */
public class DrawRegionFactory {

    /**
     * Describes the Radar Region.
     * Because the radar operates in a loop, 
     * item data (in this case the radar blip) is
     * also scaled appropriately
     * @param surface_rect
     * @return
     */
    public static DrawRegionType radarRegion(PanelDrawer panel_drawer, ImmutableRect surface_rect){
        float track_width = (float) surface_rect.width(); 
        float r = (float) surface_rect.height()/100; //circle radius
        float track_height = 2 * r;
        float top_pad = r; //padding from the top
        Rect radar_rect = new Rect(
                0,Math.round(top_pad),
                Math.round(track_width),
                Math.round(top_pad + track_height)
                );
        return new DrawRegionRadar(panel_drawer, radar_rect, r);
    }
    
    /** 
     * Region for drawing the graph bitmaps.
     * Because the graph data is drawn to bitmaps
     * only the bounding box (graph_rect) is needed
     * to perform scaled draw operations.
     * @param surface_rect
     * @return
     */
    public static DrawRegionType graphRegion(PanelDrawer panel_drawer, ImmutableRect surface_rect){
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
        return new DrawRegionGraph(panel_drawer, graph_rect);
    }
}
