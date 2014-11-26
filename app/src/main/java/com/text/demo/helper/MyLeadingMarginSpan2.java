package com.text.demo.helper;

/*
 * Provided by @levifan
 * https://github.com/bluejamesbond/Sandbox/issues/2#issuecomment-60928630
 */

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;

public class MyLeadingMarginSpan2 implements
        android.text.style.LeadingMarginSpan.LeadingMarginSpan2 {

    private int margin;
    private int lines;

    public MyLeadingMarginSpan2(int lines, int margin) {
        this.lines = lines;
        this.margin = margin;
    }

    @Override
    public int getLeadingMargin(boolean first) {
        return margin;
    }

    @Override
    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top, int baseline,
                                  int bottom, CharSequence text, int start, int end, boolean first,
                                  Layout layout) {
    }

    @Override
    public int getLeadingMarginLineCount() {
        return lines;
    }
}
