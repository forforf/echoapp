package org.younghawk.echoapp.deadcode;

import org.younghawk.echoapp.deadcode.PainterDeadCode.TYPES;

public interface DirtyPainterDeadCode extends PainterDeadCode {
    public static PainterDeadCode.TYPES type = TYPES.DIRTY;
    public int width();
    public int height();
}
