package org.younghawk.echoapp;

public interface DirtyPainter extends Painter {
    public static Painter.TYPES type = TYPES.DIRTY;
    public int width();
    public int height();
}
