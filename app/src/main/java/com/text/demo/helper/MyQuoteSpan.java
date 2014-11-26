package com.text.demo.helper;

/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Parcel;
import android.text.Layout;
import android.text.ParcelableSpan;
import android.text.style.LeadingMarginSpan;

@SuppressWarnings("UnusedDeclaration")
public class MyQuoteSpan implements LeadingMarginSpan, ParcelableSpan {

    private final static int QUOTE_SPAN = 9;

    private int mStripeWidth = 2;
    private int mGapWidth = 30;
    private int mColor;

    public MyQuoteSpan() {
        super();
        mColor = 0xff0000ff;
    }

    public MyQuoteSpan(int color) {
        super();
        mColor = color;
    }

    public MyQuoteSpan(int color, int stripeWidth) {
        super();
        mColor = color;
        mStripeWidth = stripeWidth;
    }

    public MyQuoteSpan(int color, int stripeWidth, int gapWidth) {
        super();
        mColor = color;
        mStripeWidth = stripeWidth;
        mGapWidth = gapWidth;
    }

    public MyQuoteSpan(Parcel src) {
        mColor = src.readInt();
    }

    public int getSpanTypeId() {
        return QUOTE_SPAN;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mColor);
    }

    public int getColor() {
        return mColor;
    }

    public int getLeadingMargin(boolean first) {
        return mStripeWidth + mGapWidth;
    }

    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top, int baseline,
                                  int bottom, CharSequence text, int start, int end, boolean first,
                                  Layout layout) {

        float strokeWidth = p.getStrokeWidth();
        int color = p.getColor();

        p.setStrokeWidth(mStripeWidth);
        p.setColor(mColor);

        c.drawLine(x, top, x, bottom, p);

        p.setStrokeWidth(strokeWidth);
        p.setColor(color);
    }
}